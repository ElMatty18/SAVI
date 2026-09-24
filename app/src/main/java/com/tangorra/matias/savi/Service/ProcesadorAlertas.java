package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.data.AlertaRepositorio;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.tangorra.matias.savi.ui.inicio.InicioActivity;
import com.tangorra.matias.savi.ui.alertas.DetalleAlertaActivity;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Configuracion;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.Notificador;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Decide que hacer ante una alerta nueva. Lo usan tanto el servicio que escucha la base
 * (app abierta) como los mensajes push de FCM (app cerrada).
 */
public final class ProcesadorAlertas {

    // Una misma alerta puede llegar por el listener y por FCM: se procesa una sola vez.
    private static final Set<String> procesadas = new HashSet<>();

    private ProcesadorAlertas() {
    }

    public static synchronized void procesar(Context context, Alerta alerta, Usuario usuario, String idGrupo) {
        if (!PoliticaAlertas.requiereRespuesta(alerta, usuario)) {
            return;
        }
        if (alerta.getId() != null && !procesadas.add(alerta.getId())) {
            return;
        }

        boolean respuestaAutomatica = tieneConfiguracionActiva(usuario);
        if (respuestaAutomatica) {
            responderAutomaticamente(usuario, alerta, idGrupo);
        } else if (appEnPrimerPlano()) {
            context.startActivity(intentRespuesta(context, alerta, idGrupo, false));
        }

        PoliticaAlertas.Aviso aviso = PoliticaAlertas.ajustarPorConfiguracion(
                PoliticaAlertas.aviso(alerta, usuario), usuario.getConfiguracion(), new java.util.Date());
        if (aviso != null) {
            Intent destino = respuestaAutomatica
                    ? new Intent(context, InicioActivity.class)
                    : intentRespuesta(context, alerta, idGrupo, aviso.modo == PoliticaAlertas.Modo.SONORA);
            PendingIntent accion = PendingIntent.getActivity(context, alerta.getId() != null ? alerta.getId().hashCode() : 0,
                    destino, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notificador.mostrar(context, Notificador.canal(aviso.modo), alerta.getId(),
                    alerta.getAlarma(),
                    textoNotificacion(alerta, usuario),
                    accion,
                    !respuestaAutomatica && aviso.modo == PoliticaAlertas.Modo.SONORA);
        }
    }

    /** "En tu casa · la emitio Ana Garcia" */
    static String textoNotificacion(Alerta alerta, Usuario usuario) {
        String donde;
        if (PoliticaAlertas.esDirigidaA(alerta, usuario)) {
            donde = "En tu casa";
        } else if (alerta.getDirigida() == null || StringUtils.ALL_USERS.equals(alerta.getDirigida())) {
            donde = "En el barrio";
        } else {
            donde = "En la casa de " + alerta.getDirigida().trim();
        }
        String quien = alerta.getCreadoBy() != null ? StringUtils.getTextoFormateado(alerta.getCreadoBy()).trim() : null;
        return quien == null || quien.isEmpty() ? donde : donde + " · la emitió " + quien;
    }

    private static Intent intentRespuesta(Context context, Alerta alerta, String idGrupo, boolean urgente) {
        Intent intent = urgente
                ? DetalleAlertaActivity.intentUrgente(context, idGrupo, alerta.getId())
                : DetalleAlertaActivity.intent(context, idGrupo, alerta.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private static boolean appEnPrimerPlano() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private static boolean tieneConfiguracionActiva(Usuario usuario) {
        return usuario.getConfiguracion() != null && usuario.getConfiguracion().vigente(new java.util.Date());
    }

    private static void responderAutomaticamente(Usuario usuario, Alerta alerta, String idGrupo) {
        Configuracion configuracion = usuario.getConfiguracion();
        RespuestaAlerta respuesta = AlertaRepositorio.nuevaRespuesta(usuario, alerta.getId());
        respuesta.setRespuestaAutomatica(configuracion.getConfiguracionSeleccionada());
        if (configuracion.getMensaje() != null) {
            respuesta.setMensajeAutomatica(configuracion.getMensaje());
        }
        AlertaRepositorio.responder(idGrupo, alerta.getId(), respuesta, null);
    }
}
