package com.tangorra.matias.savi.Service;

import android.app.Activity;
import android.app.Dialog;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificacionService extends IntentService {

    private static final String TAG = "NotificacionService";

    private DatabaseReference dbNotificacion = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbNotificacion);
    private ChildEventListener listenerNotificaciones = getListenerNotificaciones();

    private Location location = new Location(String.valueOf(this));

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
                    showNotification(notificacion.getTitle(), notificacion.getContenido());
                    marcarVisto(notificacion, SesionManager.getUsuario().getId());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Notificacion notificacion = dataSnapshot.getValue(Notificacion.class);
                if (condicionNotificacion(notificacion)){
                    showNotification(notificacion.getTitle(), notificacion.getContenido());
                    marcarVisto(notificacion, SesionManager.getUsuario().getId());
                }
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


    void showNotification(String title, String content) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private Boolean condicionNotificacion(Notificacion notificacion){
        if ((notificacion.getCreadoBy() != null) && (notificacion.getCreadoBy().equals(SesionManager.getUsuario().getId()))){
            return false;
        }
        if (!vistoUsuario(notificacion.getVistoPor(),SesionManager.getUsuario().getId()) && validaRangoNotificacion(notificacion)){
            return true;
        }
        return false;
    }

    private boolean validaRangoNotificacion(Notificacion notificacion) {
        return true;
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
