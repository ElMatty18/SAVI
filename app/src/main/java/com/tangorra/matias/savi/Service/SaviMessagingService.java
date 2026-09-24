package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.data.AlertaRepositorio;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.Map;

/**
 * Recibe los push que envian las Cloud Functions (functions/index.js) cuando se crea una alerta
 * o una notificacion. Solo trae ids: el contenido se lee de la base y se procesa igual que con la app abierta.
 */
public class SaviMessagingService extends FirebaseMessagingService {

    private static final String TAG = "SaviMessaging";

    public static final String TIPO_ALERTA = "alerta";
    public static final String TIPO_NOTIFICACION = "notificacion";

    @Override
    public void onNewToken(@NonNull String token) {
        registrarToken(token);
    }

    public static void registrarToken(String token) {
        Usuario usuario = SesionManager.getUsuario();
        if (token == null || usuario == null || usuario.getId() == null) {
            return;
        }
        usuario.setFcmToken(token);
        FirebaseUtils.db().getReference(FirebaseUtils.dbUsuario)
                .child(usuario.getId()).child("fcmToken").setValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        final Usuario usuario = SesionManager.getUsuario();
        if (data.isEmpty() || usuario == null || usuario.getId() == null) {
            return;
        }
        String tipo = data.get("tipo");
        if (TIPO_ALERTA.equals(tipo)) {
            final String idGrupo = data.get("idGrupo");
            String idAlerta = data.get("idAlerta");
            if (idGrupo == null || idAlerta == null) {
                return;
            }
            AlertaRepositorio.alertas(idGrupo).child(idAlerta).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ProcesadorAlertas.procesar(getApplicationContext(), snapshot.getValue(Alerta.class), usuario, idGrupo);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "No se pudo leer la alerta: " + error.getMessage());
                }
            });
        } else if (TIPO_NOTIFICACION.equals(tipo)) {
            String idNotificacion = data.get("idNotificacion");
            if (idNotificacion == null) {
                return;
            }
            FirebaseUtils.db().getReference(FirebaseUtils.dbNotificacion).child(idNotificacion)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            NotificacionService.procesar(getApplicationContext(), snapshot.getValue(Notificacion.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w(TAG, "No se pudo leer la notificacion: " + error.getMessage());
                        }
                    });
        }
    }
}
