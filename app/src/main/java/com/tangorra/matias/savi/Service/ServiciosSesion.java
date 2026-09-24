package com.tangorra.matias.savi.Service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import com.google.firebase.database.FirebaseDatabase;

/** Arranca y detiene todo lo que depende de tener una sesion iniciada. */
public final class ServiciosSesion {

    private static final String TAG = "ServiciosSesion";

    private ServiciosSesion() {
    }

    /** Debe llamarse con la app en primer plano (Android 8+ no permite iniciar servicios desde segundo plano). */
    public static void iniciar(Context context) {
        try {
            context.startService(new Intent(context, AlertaService.class));
            context.startService(new Intent(context, NotificacionService.class));
        } catch (IllegalStateException e) {
            Log.w(TAG, "No se pudieron iniciar los servicios en segundo plano", e);
        }
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(SaviMessagingService::registrarToken);
    }

    public static void detener(Context context) {
        context.stopService(new Intent(context, AlertaService.class));
        context.stopService(new Intent(context, NotificacionService.class));
        if (SesionManager.haySesion()) {
            // El dispositivo deja de recibir push para este usuario
            FirebaseUtils.db().getReference(FirebaseUtils.dbUsuario)
                    .child(SesionManager.getUsuario().getId()).child("fcmToken").removeValue();
        }
    }
}
