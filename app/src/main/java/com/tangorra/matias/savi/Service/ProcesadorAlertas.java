package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.data.AlertaRepositorio;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.tangorra.matias.savi.Activitys.MenuPrincipalActivity;
import com.tangorra.matias.savi.Activitys.RespuestaAlertaActivity;
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
            context.startActivity(intentRespuesta(context, alerta));
        }

        PoliticaAlertas.Aviso aviso = PoliticaAlertas.aviso(alerta, usuario);
        if (aviso != null) {
            Intent destino = respuestaAutomatica
                    ? new Intent(context, MenuPrincipalActivity.class)
                    : intentRespuesta(context, alerta);
            PendingIntent accion = PendingIntent.getActivity(context, alerta.getId() != null ? alerta.getId().hashCode() : 0,
                    destino, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notificador.mostrar(context, Notificador.canal(aviso.modo), alerta.getId(),
                    aviso.nivel + "! " + alerta.getAlarma(),
                    "Dirigida -> " + alerta.getDirigida(),
                    accion,
                    !respuestaAutomatica && aviso.modo == PoliticaAlertas.Modo.SONORA);
        }
    }

    private static Intent intentRespuesta(Context context, Alerta alerta) {
        Intent intent = new Intent(context, RespuestaAlertaActivity.class);
        intent.putExtra(StringUtils.parametroAlerta, alerta);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private static boolean appEnPrimerPlano() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private static boolean tieneConfiguracionActiva(Usuario usuario) {
        return usuario.getConfiguracion() != null && usuario.getConfiguracion().isConfiguracionActiva();
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
