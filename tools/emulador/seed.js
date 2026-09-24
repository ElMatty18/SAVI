/**
 * Carga datos de demo en los emuladores de Auth y Realtime Database.
 * Se ejecuta solo con `npm run emulador`; nunca apunta a produccion.
 *
 * Usuarios (clave Demo1234 para todos):
 *   ana@demo.com   grupo "Barrio Demo", familia con beto
 *   beto@demo.com  grupo "Barrio Demo", familia con ana
 *   caro@demo.com  grupo "Barrio Demo", sin familia
 *   dani@demo.com  sin grupo ni familia (para probar el alta)
 */
const {initializeApp} = require("firebase-admin/app");
const {getAuth} = require("firebase-admin/auth");
const {getDatabase} = require("firebase-admin/database");

if (!process.env.FIREBASE_DATABASE_EMULATOR_HOST || !process.env.FIREBASE_AUTH_EMULATOR_HOST) {
  console.error("seed.js solo corre contra los emuladores (usar npm run emulador)");
  process.exit(1);
}

const PROYECTO = process.env.GCLOUD_PROJECT || "demo-savi";
const app = initializeApp({projectId: PROYECTO, databaseURL: `http://${process.env.FIREBASE_DATABASE_EMULATOR_HOST}?ns=${PROYECTO}-default-rtdb`});

// La app guarda java.util.Date con el formato de bean que usa el SDK de Firebase para Android
function fecha(d) {
  return {
    date: d.getDate(), day: d.getDay(), hours: d.getHours(), minutes: d.getMinutes(),
    month: d.getMonth(), seconds: d.getSeconds(), time: d.getTime(),
    timezoneOffset: d.getTimezoneOffset(), year: d.getFullYear() - 1900,
  };
}
const haceMinutos = (m) => fecha(new Date(Date.now() - m * 60000));

const GRUPO = "grupo-demo";
const FAMILIA = "familia-demo";
// Palermo, Buenos Aires
const BASE = {lat: -34.5889, lng: -58.4306};
const cerca = (dLat, dLng) => ({lat: BASE.lat + dLat, lng: BASE.lng + dLng});

const PERSONAS = [
  {mail: "ana@demo.com", nombre: "ana", apellido: "garcia", casa: cerca(0.001, 0.001), familia: true, grupo: true},
  {mail: "beto@demo.com", nombre: "beto", apellido: "lopez", casa: cerca(-0.002, 0.0015), familia: true, grupo: true},
  {mail: "caro@demo.com", nombre: "carolina", apellido: "diaz", casa: cerca(0.0025, -0.001), grupo: true},
  {mail: "dani@demo.com", nombre: "daniel", apellido: "ruiz", casa: cerca(0.03, 0.02)},
];

async function main() {
  const auth = getAuth(app);
  const uids = {};
  for (const p of PERSONAS) {
    try {
      uids[p.mail] = (await auth.getUserByEmail(p.mail)).uid;
    } catch {
      uids[p.mail] = (await auth.createUser({email: p.mail, password: "Demo1234"})).uid;
    }
  }
  const uid = (mail) => uids[mail];
  const glosa = (p) => `${p.nombre} ${p.apellido}`.replace(/\b\w/g, (c) => c.toUpperCase()) + " ";

  const Usuario = {};
  for (const p of PERSONAS) {
    Usuario[uid(p.mail)] = {
      id: uid(p.mail), mail: p.mail, nombre: p.nombre, apellido: p.apellido,
      dni: "30123456", celular: "1155551234", fijo: "1145671234",
      perfil: {domicilio: p.casa},
      ...(p.grupo ? {idGrupo: GRUPO} : {}),
      ...(p.familia ? {idFamilia: FAMILIA} : {}),
    };
  }
  const [ana, beto, caro] = PERSONAS;

  const alertas = {
    "alerta-1": {
      id: "alerta-1", alarma: "Sospecha de robo", estado: "activa",
      creadoBy: glosa(beto), creadoById: uid(beto.mail), dirigida: glosa(ana), dirigidaId: uid(ana.mail),
      creacion: haceMinutos(12),
      respuestas: {[uid(caro.mail)]: {idUsuario: uid(caro.mail), nombreUsuario: caro.nombre, apellidoUsuario: caro.apellido,
        idAlarma: "alerta-1", respuesta: "confirma", creacion: haceMinutos(10)}},
    },
    "alerta-2": {
      id: "alerta-2", alarma: "Auto mal estacionado", estado: "activa",
      creadoBy: glosa(ana), creadoById: uid(ana.mail), dirigida: "TODOS",
      creacion: haceMinutos(90),
    },
    "alerta-3": {
      id: "alerta-3", alarma: "Principio de fuego", estado: "desactiva",
      creadoBy: glosa(caro), creadoById: uid(caro.mail), dirigida: glosa(beto), dirigidaId: uid(beto.mail),
      creacion: haceMinutos(60 * 26),
      respuestas: {[uid(beto.mail)]: {idUsuario: uid(beto.mail), nombreUsuario: beto.nombre, apellidoUsuario: beto.apellido,
        idAlarma: "alerta-3", respuesta: "cancela", creacion: haceMinutos(60 * 26 - 3)}},
    },
  };

  await getDatabase(app).ref().set({
    Usuario,
    Grupo: {[GRUPO]: {id: GRUPO, nombre: "Barrio Demo", maxUsuarios: 20, maxRango: 500, ...BASE, alertas}},
    Familia: {[FAMILIA]: {[uid(ana.mail)]: uid(ana.mail), [uid(beto.mail)]: uid(beto.mail)}},
    Notificacion: {
      "notif-1": {id: "notif-1", title: "Corte de luz", contenido: "Manana de 9 a 13 por obras en la cuadra",
        creadoBy: uid(caro.mail), rango: 1000, ...cerca(0.001, 0)},
    },
  });

  console.log("Datos de demo cargados. Usuarios: " + PERSONAS.map((p) => p.mail).join(", ") + " (clave Demo1234)");
}

main().then(() => process.exit(0)).catch((e) => {
  console.error(e);
  process.exit(1);
});
