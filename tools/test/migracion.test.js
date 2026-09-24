// Test de migrar-uid.js contra los emuladores de Auth y Realtime Database.
const {test, before, after} = require("node:test");
const assert = require("node:assert/strict");
const os = require("node:os");
const fs = require("node:fs");
const {initializeApp, deleteApp} = require("firebase-admin/app");
const {getAuth} = require("firebase-admin/auth");
const {getDatabase} = require("firebase-admin/database");
const {migrar, planificar} = require("../migracion/migrar-uid");

process.env.FIREBASE_DATABASE_EMULATOR_HOST = process.env.FIREBASE_DATABASE_EMULATOR_HOST || "127.0.0.1:9000";
process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || "127.0.0.1:9099";

const app = initializeApp({projectId: "demo-savi", databaseURL: "http://127.0.0.1:9000?ns=demo-savi"}, "migracion");

// Datos con el formato que dejaba la version anterior de la app
const LEGADO = {
  Usuario: {
    "-Lana": {id: "-Lana", mail: "ana@barrio.com", clave: "Ana12345", glosa: "ana perez", nombre: "ana", idGrupo: "g1", idFamilia: "f1"},
    "-Lbeto": {id: "-Lbeto", mail: "beto@barrio.com", clave: "Beto1234", idGrupo: "g1", idFamilia: "f1"},
    "-Lfantasma": {id: "-Lfantasma", mail: "nadie@barrio.com"},
  },
  Familia: {f1: {"-Kx1": "-Lana", "-Kx2": "-Lbeto"}},
  Grupo: {
    g1: {
      id: "g1",
      alertas: {
        a1: {
          id: "a1", alarma: "Agresion", estado: "activa", creadoById: "-Lana", dirigidaId: "-Lbeto",
          respuestas: [{idUsuario: "-Lbeto", respuesta: "confirma"}],
        },
      },
    },
  },
  Notificacion: {n1: {id: "n1", creadoBy: "-Lana", vistoPor: ["-Lbeto"]}},
};

let uidAna;
let uidBeto;

before(async () => {
  const auth = getAuth(app);
  for (const u of (await auth.listUsers()).users) await auth.deleteUser(u.uid);
  uidAna = (await auth.createUser({email: "ana@barrio.com"})).uid;
  uidBeto = (await auth.createUser({email: "beto@barrio.com"})).uid;
  await getDatabase(app).ref().set(LEGADO);
});

// Cierra la conexion a la base para que el proceso termine
after(() => deleteApp(app));

test("la simulacion no escribe nada", async () => {
  const dir = fs.mkdtempSync(os.tmpdir() + "/savi-");
  await migrar(app, {aplicar: false, fotos: false, dirBackup: dir, log: () => {}});
  const usuarios = (await getDatabase(app).ref("Usuario").get()).val();
  assert.ok(usuarios["-Lana"]);
  assert.equal(fs.readdirSync(dir).length, 1, "debe dejar un backup");
});

test("migra usuarios, familia, alertas y notificaciones al uid", async () => {
  const log = [];
  await migrar(app, {aplicar: true, fotos: false, dirBackup: fs.mkdtempSync(os.tmpdir() + "/savi-"), log: (m) => log.push(m)});
  const raiz = (await getDatabase(app).ref().get()).val();

  assert.equal(raiz.Usuario["-Lana"], undefined);
  assert.equal(raiz.Usuario[uidAna].id, uidAna);
  assert.equal(raiz.Usuario[uidAna].nombre, "ana");
  assert.equal(raiz.Usuario[uidAna].clave, undefined, "la clave se elimina");
  assert.equal(raiz.Usuario[uidAna].glosa, undefined);
  assert.ok(raiz.Usuario["-Lfantasma"], "los usuarios sin cuenta en Auth se dejan como estan");
  assert.ok(log.some((l) => l.includes("-Lfantasma")));

  assert.deepEqual(raiz.Familia.f1, {[uidAna]: uidAna, [uidBeto]: uidBeto});

  const alerta = raiz.Grupo.g1.alertas.a1;
  assert.equal(alerta.creadoById, uidAna);
  assert.equal(alerta.dirigidaId, uidBeto);
  assert.deepEqual(alerta.respuestas, {[uidBeto]: {idUsuario: uidBeto, respuesta: "confirma"}});

  assert.equal(raiz.Notificacion.n1.creadoBy, uidAna);
  assert.deepEqual(raiz.Notificacion.n1.vistoPor, {[uidBeto]: uidBeto});
});

test("correrla de nuevo no cambia nada", async () => {
  const antes = (await getDatabase(app).ref().get()).val();
  const {mapa} = await migrar(app, {aplicar: true, fotos: false, dirBackup: fs.mkdtempSync(os.tmpdir() + "/savi-"), log: () => {}});
  const despues = (await getDatabase(app).ref().get()).val();
  assert.deepEqual(despues, antes);
  assert.equal(mapa[uidAna], uidAna);
});

test("con mails duplicados conserva el registro mas completo", () => {
  const raiz = {Usuario: {
    "-Lvacio": {id: "-Lvacio", mail: "x@barrio.com"},
    "-Llleno": {id: "-Llleno", mail: "x@barrio.com", nombre: "X", dni: "1"},
  }};
  const cambios = planificar(raiz, {"-Lvacio": "uidX", "-Llleno": "uidX"}, ["-Lvacio"]);
  assert.equal(cambios["Usuario/-Lvacio"], null);
  assert.equal(cambios["Usuario/uidX"].nombre, "X");
});
