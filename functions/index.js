/**
 * Envia un push (FCM, solo datos) a cada dispositivo cuando se crea una alerta o una notificacion.
 * La app lee el contenido de la base y decide como avisar (ver ProcesadorAlertas en Android),
 * asi las reglas de negocio quedan en un solo lugar.
 */
const {onValueCreated} = require("firebase-functions/v2/database");
const {logger} = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const INSTANCIA = "savitesis";

/** Tokens de los usuarios que cumplen el filtro, sin incluir al creador. */
async function tokensDe(query, idExcluido) {
  const snap = await query.get();
  const destinos = [];
  snap.forEach((hijo) => {
    const usuario = hijo.val();
    if (usuario && usuario.fcmToken && usuario.id !== idExcluido) {
      destinos.push({token: usuario.fcmToken, ref: hijo.ref});
    }
  });
  return destinos;
}

async function enviar(destinos, data) {
  if (destinos.length === 0) {
    return;
  }
  const respuesta = await admin.messaging().sendEachForMulticast({
    tokens: destinos.map((d) => d.token),
    data,
    android: {priority: "high"},
  });
  // Borra los tokens de dispositivos que desinstalaron la app o cerraron sesion
  const limpiezas = [];
  respuesta.responses.forEach((r, i) => {
    const codigo = r.error && r.error.code;
    if (codigo === "messaging/registration-token-not-registered" ||
        codigo === "messaging/invalid-registration-token") {
      limpiezas.push(destinos[i].ref.child("fcmToken").remove());
    }
  });
  await Promise.all(limpiezas);
  logger.info(`Push ${data.tipo}: ${respuesta.successCount} ok, ${respuesta.failureCount} con error`);
}

exports.pushAlerta = onValueCreated(
    {ref: "/Grupo/{idGrupo}/alertas/{idAlerta}", instance: INSTANCIA},
    async (event) => {
      const {idGrupo, idAlerta} = event.params;
      const alerta = event.data.val() || {};
      const usuarios = admin.database().ref("Usuario").orderByChild("idGrupo").equalTo(idGrupo);
      await enviar(await tokensDe(usuarios, alerta.creadoById), {tipo: "alerta", idGrupo, idAlerta});
    });

exports.pushNotificacion = onValueCreated(
    {ref: "/Notificacion/{idNotificacion}", instance: INSTANCIA},
    async (event) => {
      const {idNotificacion} = event.params;
      const notificacion = event.data.val() || {};
      // El filtro por distancia al domicilio lo hace la app (PoliticaNotificaciones)
      const usuarios = admin.database().ref("Usuario").orderByChild("fcmToken").startAt("");
      await enviar(await tokensDe(usuarios, notificacion.creadoBy), {tipo: "notificacion", idNotificacion});
    });
