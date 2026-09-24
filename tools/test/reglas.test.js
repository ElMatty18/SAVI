// Tests de database.rules.json contra el emulador de Realtime Database.
// Correr con: cd tools && npm test
const {test, before, after, beforeEach} = require("node:test");
const fs = require("node:fs");
const path = require("node:path");
const {initializeTestEnvironment, assertFails, assertSucceeds} = require("@firebase/rules-unit-testing");
const {ref, get, set, update, remove} = require("firebase/database");

let env;

const GRUPO = "g1";
const FAMILIA = "f1";

before(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-savi",
    database: {
      rules: fs.readFileSync(path.join(__dirname, "../../database.rules.json"), "utf8"),
      host: "127.0.0.1",
      port: 9000,
    },
  });
});

after(() => env.cleanup());

beforeEach(async () => {
  await env.clearDatabase();
  await env.withSecurityRulesDisabled((ctx) => set(ref(ctx.database()), {
    Usuario: {
      ana: {id: "ana", mail: "ana@barrio.com", idGrupo: GRUPO, idFamilia: FAMILIA},
      beto: {id: "beto", mail: "beto@barrio.com", idGrupo: GRUPO},
      caro: {id: "caro", mail: "caro@barrio.com", idGrupo: "otro"},
    },
    Grupo: {
      [GRUPO]: {
        id: GRUPO, nombre: "Barrio",
        alertas: {
          a1: {id: "a1", alarma: "Agresion", estado: "activa", creadoById: "ana", dirigidaId: "beto"},
        },
      },
    },
    Familia: {[FAMILIA]: {ana: "ana"}},
    Notificacion: {n1: {id: "n1", creadoBy: "ana", title: "Corte de luz"}},
  }));
});

const db = (uid) => (uid ? env.authenticatedContext(uid) : env.unauthenticatedContext()).database();

test("sin autenticacion no se puede leer nada", async () => {
  await assertFails(get(ref(db(null), "Usuario")));
  await assertFails(get(ref(db(null), "Grupo")));
  await assertFails(get(ref(db(null), "/")));
});

test("un usuario autenticado lee perfiles pero no la raiz", async () => {
  await assertSucceeds(get(ref(db("beto"), "Usuario")));
  await assertFails(get(ref(db("beto"), "/")));
});

test("cada usuario escribe solo su propio registro", async () => {
  await assertSucceeds(update(ref(db("ana"), "Usuario/ana"), {nombre: "Ana"}));
  await assertFails(update(ref(db("beto"), "Usuario/ana"), {nombre: "Hackeada"}));
  await assertFails(set(ref(db("beto"), "Usuario/ana"), {id: "ana"}));
});

test("el registro nuevo debe usar el uid como id", async () => {
  await assertSucceeds(set(ref(db("dani"), "Usuario/dani"), {id: "dani", mail: "dani@barrio.com"}));
  await assertFails(set(ref(db("eli"), "Usuario/eli"), {id: "otro", mail: "eli@barrio.com"}));
});

test("la clave no se puede volver a guardar", async () => {
  await assertFails(update(ref(db("ana"), "Usuario/ana"), {clave: "Secreta123"}));
});

test("un familiar puede sumar a otro usuario a su familia", async () => {
  await assertSucceeds(update(ref(db("ana")), {
    [`Familia/${FAMILIA}/beto`]: "beto",
    "Usuario/beto/idFamilia": FAMILIA,
  }));
});

test("dos usuarios sin familia crean una nueva en una sola escritura", async () => {
  await assertSucceeds(update(ref(db("beto")), {
    "Familia/f2/beto": "beto",
    "Familia/f2/caro": "caro",
    "Usuario/beto/idFamilia": "f2",
    "Usuario/caro/idFamilia": "f2",
  }));
});

test("no se puede meter a alguien en una familia ajena", async () => {
  await assertFails(update(ref(db("caro")), {"Usuario/beto/idFamilia": FAMILIA}));
  await assertFails(set(ref(db("caro"), `Familia/${FAMILIA}/beto`), "beto"));
});

test("las entradas de familia deben ser uid -> uid", async () => {
  await assertFails(set(ref(db("beto"), `Familia/${FAMILIA}/beto`), "otro"));
});

test("un integrante crea alertas firmadas con su uid", async () => {
  const alerta = {id: "a2", alarma: "Principio de fuego", estado: "activa", creadoById: "beto"};
  await assertSucceeds(set(ref(db("beto"), `Grupo/${GRUPO}/alertas/a2`), alerta));
  await assertFails(set(ref(db("beto"), `Grupo/${GRUPO}/alertas/a3`), {...alerta, id: "a3", creadoById: "ana"}));
});

test("alguien de otro grupo no puede crear alertas", async () => {
  await assertFails(set(ref(db("caro"), `Grupo/${GRUPO}/alertas/a2`),
      {id: "a2", alarma: "Agresion", estado: "activa", creadoById: "caro"}));
});

test("no se puede borrar ni pisar un grupo o una alerta", async () => {
  await assertFails(remove(ref(db("ana"), `Grupo/${GRUPO}`)));
  await assertFails(set(ref(db("ana"), `Grupo/${GRUPO}/alertas/a1`),
      {id: "a1", alarma: "x", estado: "activa", creadoById: "ana"}));
});

test("cada vecino escribe solo su propia respuesta", async () => {
  await assertSucceeds(set(ref(db("beto"), `Grupo/${GRUPO}/alertas/a1/respuestas/beto`),
      {idUsuario: "beto", respuesta: "confirma"}));
  await assertFails(set(ref(db("beto"), `Grupo/${GRUPO}/alertas/a1/respuestas/ana`),
      {idUsuario: "ana", respuesta: "cancela"}));
  await assertFails(set(ref(db("caro"), `Grupo/${GRUPO}/alertas/a1/respuestas/caro`),
      {idUsuario: "caro", respuesta: "confirma"}));
});

test("solo el destinatario o el creador cambian el estado", async () => {
  await assertSucceeds(update(ref(db("beto"), `Grupo/${GRUPO}/alertas/a1`), {
    "respuestas/beto": {idUsuario: "beto", respuesta: "confirma"},
    "estado": "atencion",
  }));
  await assertSucceeds(set(ref(db("ana"), `Grupo/${GRUPO}/alertas/a1/estado`), "desactiva"));
  await env.withSecurityRulesDisabled((ctx) =>
    set(ref(ctx.database(), `Usuario/caro/idGrupo`), GRUPO));
  await assertFails(set(ref(db("caro"), `Grupo/${GRUPO}/alertas/a1/estado`), "desactiva"));
});

test("notificaciones: se crean firmadas y cada uno marca solo su visto", async () => {
  await assertSucceeds(set(ref(db("beto"), "Notificacion/n2"), {id: "n2", creadoBy: "beto", title: "Hola"}));
  await assertFails(set(ref(db("beto"), "Notificacion/n3"), {id: "n3", creadoBy: "ana", title: "Falsa"}));
  await assertSucceeds(set(ref(db("beto"), "Notificacion/n1/vistoPor/beto"), "beto"));
  await assertFails(set(ref(db("beto"), "Notificacion/n1/vistoPor/caro"), "caro"));
  await assertFails(remove(ref(db("ana"), "Notificacion/n1")));
});
