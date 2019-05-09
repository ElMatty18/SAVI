package com.tangorra.matias.savi.Service;

import android.app.Activity;
import android.app.Dialog;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Activitys.MainActivity;
import com.tangorra.matias.savi.Activitys.MenuPrincipalActivity;
import com.tangorra.matias.savi.Activitys.RespuestaAlertaActivity;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Configuracion;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.MapsUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificacionService extends IntentService {

    private static final String TAG = "NotificacionService";

    private DatabaseReference dbNotificacion = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbNotificacion);
    private ChildEventListener listenerNotificaciones = getListenerNotificaciones();



    public NotificacionService() {
        super("NotificacionService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        String dataString = workIntent.getDataString();
        Log.i(TAG, "The service is on");

        dbNotificacion.addChildEventListener(listenerNotificaciones);




    }

    @NonNull
    private ChildEventListener getListenerNotificaciones() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Notificacion notificacion = dataSnapshot.getValue(Notificacion.class);
                if (condicionNotificacion(notificacion)){
                    notification(notificacion, "Notificacion ", false, false);
                    marcarVisto(notificacion, SesionManager.getUsuario().getId());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private boolean vistoUsuario(List<String> vistoPor, String id) {
        if (vistoPor == null){
            return false;
        }
        for (String idUsuario: vistoPor) {
            if (idUsuario.equals(id)){
                return true;
            }
        }
        return false;
    }


    public void notification(Notificacion notificacion, String nivel, boolean sonora, boolean vibracion){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.zxing_beep);
        mp.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            //notificationChannel.setSound();
            notificationChannel.enableVibration(vibracion);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Intent stopped = new Intent(this,MainActivity.class);
        stopped.setAction("test");
        if (stopped.getAction().equals(stopped)) {
            mp.stop();
        }

        PendingIntent actionPendingIntent = PendingIntent.getActivity(this,1,stopped,PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_casa)
                .setContentTitle(nivel + "! " + notificacion.getTitle())
                .setContentText(notificacion.getContenido());

        if (sonora){
            notificationBuilder.setSound(Uri.parse("android.resource://"+getPackageName()+"/" + R.raw.zxing_beep));
        }

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

    private Boolean condicionNotificacion(Notificacion notificacion){
        if ((notificacion.getCreadoBy() != null) && (notificacion.getCreadoBy().equals(SesionManager.getUsuario().getId()))){
            return false;
        }
        if (!vistoUsuario(notificacion.getVistoPor(),SesionManager.getUsuario().getId()) && validaRangoNotificacion(notificacion) && validaRangoNotificacionAlterno(notificacion)){
            return true;
        }
        return false;
    }

    private boolean validaRangoNotificacion(Notificacion notificacion) {
        try {
            Double distacia = MapsUtils.distanciaCoord(notificacion.getLat(),notificacion.getLng(), SesionManager.getUsuario().getPerfil().getDomicilio().getLat(),SesionManager.getUsuario().getPerfil().getDomicilio().getLng());
            if (distacia < notificacion.getRango()){
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            return  false;
        }

    }

    private boolean validaRangoNotificacionAlterno(Notificacion notificacion) {
        try {
            Double distacia = MapsUtils.distanciaCoord(notificacion.getLat(),notificacion.getLng(), SesionManager.getUsuario().getPerfil().getDomicilioAlterno().getLat(),SesionManager.getUsuario().getPerfil().getDomicilioAlterno().getLng());
            if (distacia < notificacion.getRango()){
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            return  false;
        }
    }

    private void marcarVisto(Notificacion notificacion, String id) {
        if (notificacion.getVistoPor() == null){
            notificacion.setVistoPor(new ArrayList<String>());
        }
        notificacion.getVistoPor().add(id);
        if (notificacion.getId() != null){
            dbNotificacion.child(notificacion.getId()).setValue(notificacion);
        }
    }




}
