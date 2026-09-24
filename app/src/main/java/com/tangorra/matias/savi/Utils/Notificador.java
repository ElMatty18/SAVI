package com.tangorra.matias.savi.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.PoliticaAlertas;

/**
 * Canales y notificaciones de la app. El sonido y la vibracion de un canal no se pueden
 * cambiar una vez creado, por eso hay un canal por cada modo de aviso.
 */
public final class Notificador {

    public static final String CANAL_ALERTA_SONORA = "alertas_sonoras";
    public static final String CANAL_ALERTA_VIBRACION = "alertas_vibracion";
    public static final String CANAL_ALERTA_SILENCIOSA = "alertas_silenciosas";
    public static final String CANAL_NOTIFICACIONES = "notificaciones";

    private static final long[] PATRON_VIBRACION = {0, 1000, 500, 1000};

    private Notificador() {
    }

    public static void crearCanales(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        AudioAttributes audio = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel sonora = new NotificationChannel(CANAL_ALERTA_SONORA, "Alertas urgentes", NotificationManager.IMPORTANCE_HIGH);
        sonora.setDescription("Alertas vecinales que requieren atencion inmediata");
        sonora.enableLights(true);
        sonora.setLightColor(Color.RED);
        sonora.enableVibration(true);
        sonora.setVibrationPattern(PATRON_VIBRACION);
        sonora.setSound(sonido(context), audio);
        manager.createNotificationChannel(sonora);

        NotificationChannel vibracion = new NotificationChannel(CANAL_ALERTA_VIBRACION, "Alertas con vibracion", NotificationManager.IMPORTANCE_HIGH);
        vibracion.setDescription("Alertas vecinales sin sonido");
        vibracion.enableVibration(true);
        vibracion.setVibrationPattern(PATRON_VIBRACION);
        vibracion.setSound(null, null);
        manager.createNotificationChannel(vibracion);

        NotificationChannel silenciosa = new NotificationChannel(CANAL_ALERTA_SILENCIOSA, "Alertas informativas", NotificationManager.IMPORTANCE_DEFAULT);
        silenciosa.setDescription("Alertas vecinales de baja prioridad");
        silenciosa.enableVibration(false);
        silenciosa.setSound(null, null);
        manager.createNotificationChannel(silenciosa);

        NotificationChannel notificaciones = new NotificationChannel(CANAL_NOTIFICACIONES, "Notificaciones del barrio", NotificationManager.IMPORTANCE_DEFAULT);
        notificaciones.enableVibration(false);
        notificaciones.setSound(null, null);
        manager.createNotificationChannel(notificaciones);
    }

    public static String canal(PoliticaAlertas.Modo modo) {
        switch (modo) {
            case SONORA:
                return CANAL_ALERTA_SONORA;
            case VIBRACION:
                return CANAL_ALERTA_VIBRACION;
            default:
                return CANAL_ALERTA_SILENCIOSA;
        }
    }

    /**
     * @param id identificador estable (p.ej. el id de la alerta) para no pisar otras notificaciones.
     * @param pantallaCompleta abre la pantalla de respuesta sobre la pantalla bloqueada (alertas urgentes).
     */
    public static void mostrar(Context context, String canal, String id, String titulo, String texto,
                               PendingIntent accion, boolean pantallaCompleta) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canal)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_casa)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        // Compatibilidad con Android < 8, donde el canal no define sonido ni vibracion
        if (CANAL_ALERTA_SONORA.equals(canal)) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH).setSound(sonido(context)).setVibrate(PATRON_VIBRACION);
        } else if (CANAL_ALERTA_VIBRACION.equals(canal)) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH).setVibrate(PATRON_VIBRACION);
        }

        if (accion != null) {
            builder.setContentIntent(accion);
            if (pantallaCompleta) {
                builder.setFullScreenIntent(accion, true);
            }
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (manager.areNotificationsEnabled()) {
            try {
                manager.notify(id != null ? id.hashCode() : (int) System.currentTimeMillis(), builder.build());
            } catch (SecurityException e) {
                // Falta el permiso POST_NOTIFICATIONS (Android 13+)
            }
        }
    }

    private static Uri sonido(Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.zxing_beep);
    }
}
