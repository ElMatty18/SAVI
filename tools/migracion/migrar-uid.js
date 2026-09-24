#!/usr/bin/env node
/**
 * Migra la base al modelo indexado por uid de Firebase Auth.
 *
 *   /Usuario/{pushId}            -> /Usuario/{uid}           (sin clave, glosa ni grupo)
 *   /Familia/{f}/{pushId}: id    -> /Familia/{f}/{uid}: uid
 *   alertas: creadoById, dirigidaId y respuestas (lista -> mapa por uid)
 *   notificaciones: creadoBy y vistoPor (lista -> mapa por uid)
 *   Storage: Fotos/{pushId}      -> Fotos/{uid}
 *
 * Por defecto solo muestra lo que haria. Siempre guarda un backup JSON antes de escribir.
 *
 * Uso:
 *   export GOOGLE_APPLICATION_CREDENTIALS=/ruta/service-account.json
 *   node migracion/migrar-uid.js              # simulacion
 *   node migracion/migrar-uid.js --aplicar    # escribe los cambios
 *   node migracion/migrar-uid.js --aplicar --sin-fotos
 */
const fs = require("node:fs");
const path = require("node:path");
const {initializeApp} = require("firebase-admin/app");
const {getAuth} = require("firebase-admin/auth");
const {getDatabase} = require("firebase-admin/database");
const {getStorage} = require("firebase-admin/storage");

const PROYECTO = process.env.GCLOUD_PROJECT || "savitesis";

function comoLista(valor) {
  if (!valor) return [];
  return Array.isArray(valor) ? valor : Object.values(valor);
}

/** Calcula el mapa pushId -> uid buscando cada mail en Firebase Auth. */
async function mapearIds(usuarios, auth, log) {
  const mapa = {};
  const sinCuenta = [];
  const porUid = {};
  for (const [clave, usuario] of Object.entries(usuarios)) {
    if (!usuario || !usuario.mail) {
      sinCuenta.push(clave);
      continue;
    }
    try {
      const cuenta = await auth.getUserByEmail(usuario.mail.trim());
      mapa[clave] = cuenta.uid;
      (porUid[cuenta.uid] = porUid[cuenta.uid] || []).push(clave);
    } catch (e) {
      if (e.code !== "auth/user-not-found") throw e;
      sinCuenta.push(clave);
    }
  }
  // Un mismo mail registrado dos veces: se conserva el registro mas completo
  const descartados = [];
  for (const [uid, claves] of Object.entries(porUid)) {
    if (claves.length < 2) continue;
    const completitud = (c) => Object.keys(usuarios[c] || {}).length + (c === uid ? 1000 : 0);
    claves.sort((a, b) => completitud(b) - completitud(a));
    for (const c of claves.slice(1)) {
      log(`  ! ${usuarios[c].mail}: registro duplicado ${c}, se conserva ${claves[0]}`);
      descartados.push(c);
    }
  }
  return {mapa, sinCuenta, descartados};
}

/** Arma el update multi-ruta. Es una funcion pura para poder testearla. */
function planificar(raiz, mapa, descartados = []) {
  const uid = (id) => (id && mapa[id]) || id;
  const cambios = {};

  for (const [clave, usuario] of Object.entries(raiz.Usuario || {})) {
    const nuevoId = mapa[clave];
    if (!nuevoId || descartados.includes(clave)) {
      if (descartados.includes(clave)) cambios[`Usuario/${clave}`] = null;
      continue;
    }
    const limpio = {...usuario, id: nuevoId};
    delete limpio.clave;
    delete limpio.glosa;
    delete limpio.grupo;
    if (nuevoId !== clave) cambios[`Usuario/${clave}`] = null;
    cambios[`Usuario/${nuevoId}`] = limpio;
  }

  for (const [idFamilia, integrantes] of Object.entries(raiz.Familia || {})) {
    for (const [clave, idUsuario] of Object.entries(integrantes || {})) {
      const nuevo = uid(idUsuario);
      if (clave === nuevo && idUsuario === nuevo) continue;
      cambios[`Familia/${idFamilia}/${clave}`] = null;
      cambios[`Familia/${idFamilia}/${nuevo}`] = nuevo;
    }
  }

  for (const [idGrupo, grupo] of Object.entries(raiz.Grupo || {})) {
    for (const [idAlerta, alerta] of Object.entries((grupo && grupo.alertas) || {})) {
      const base = `Grupo/${idGrupo}/alertas/${idAlerta}`;
      if (alerta.creadoById && uid(alerta.creadoById) !== alerta.creadoById) {
        cambios[`${base}/creadoById`] = uid(alerta.creadoById);
      }
      if (alerta.dirigidaId && uid(alerta.dirigidaId) !== alerta.dirigidaId) {
        cambios[`${base}/dirigidaId`] = uid(alerta.dirigidaId);
      }
      if (alerta.respuestas) {
        const respuestas = {};
        for (const r of comoLista(alerta.respuestas)) {
          if (!r || !r.idUsuario) continue;
          const nuevo = uid(r.idUsuario);
          respuestas[nuevo] = {...r, idUsuario: nuevo};
        }
        cambios[`${base}/respuestas`] = respuestas;
      }
    }
  }

  for (const [idNotificacion, notificacion] of Object.entries(raiz.Notificacion || {})) {
    const base = `Notificacion/${idNotificacion}`;
    if (notificacion.creadoBy && uid(notificacion.creadoBy) !== notificacion.creadoBy) {
      cambios[`${base}/creadoBy`] = uid(notificacion.creadoBy);
    }
    if (notificacion.vistoPor) {
      const vistos = {};
      for (const id of comoLista(notificacion.vistoPor)) {
        if (id) vistos[uid(id)] = uid(id);
      }
      cambios[`${base}/vistoPor`] = vistos;
    }
  }

  return cambios;
}

async function migrarFotos(bucket, mapa, log) {
  let copiadas = 0;
  for (const [viejo, nuevo] of Object.entries(mapa)) {
    if (viejo === nuevo) continue;
    const origen = bucket.file(`Fotos/${viejo}`);
    const [existe] = await origen.exists();
    if (!existe) continue;
    await origen.copy(bucket.file(`Fotos/${nuevo}`));
    copiadas++;
  }
  log(`  Fotos copiadas: ${copiadas} (las originales no se borran)`);
}

async function migrar(app, {aplicar = false, fotos = true, dirBackup = process.cwd(), log = console.log} = {}) {
  const db = getDatabase(app);
  const raiz = (await db.ref().get()).val() || {};

  const backup = path.join(dirBackup, `backup-${PROYECTO}-${new Date().toISOString().replace(/[:.]/g, "-")}.json`);
  fs.writeFileSync(backup, JSON.stringify(raiz, null, 2));
  log(`Backup: ${backup}`);

  const {mapa, sinCuenta, descartados} = await mapearIds(raiz.Usuario || {}, getAuth(app), log);
  const cambios = planificar(raiz, mapa, descartados);

  log(`Usuarios a migrar: ${Object.entries(mapa).filter(([a, b]) => a !== b).length}`);
  log(`Usuarios ya migrados: ${Object.entries(mapa).filter(([a, b]) => a === b).length}`);
  if (sinCuenta.length) log(`  ! Sin cuenta en Auth (se dejan como estan): ${sinCuenta.join(", ")}`);
  log(`Rutas a escribir: ${Object.keys(cambios).length}`);

  if (!aplicar) {
    log("Simulacion: no se escribio nada. Usar --aplicar para migrar.");
    return {mapa, cambios, sinCuenta};
  }
  if (Object.keys(cambios).length) {
    await db.ref().update(cambios);
  }
  log("Base migrada.");
  if (fotos) {
    await migrarFotos(getStorage(app).bucket(), mapa, log);
  }
  return {mapa, cambios, sinCuenta};
}

module.exports = {migrar, planificar};

if (require.main === module) {
  const args = process.argv.slice(2);
  const app = initializeApp({
    projectId: PROYECTO,
    databaseURL: process.env.DATABASE_URL || `https://${PROYECTO}.firebaseio.com`,
    storageBucket: process.env.STORAGE_BUCKET || `${PROYECTO}.appspot.com`,
  });
  migrar(app, {aplicar: args.includes("--aplicar"), fotos: !args.includes("--sin-fotos")})
      .then(() => process.exit(0))
      .catch((e) => {
        console.error(e);
        process.exit(1);
      });
}
