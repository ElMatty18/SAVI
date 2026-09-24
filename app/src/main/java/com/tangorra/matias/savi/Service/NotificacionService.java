package com.tangorra.matias.savi.Service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Activitys.MenuPrincipalActivity;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.Notificador;


/**
 * Escucha las notificaciones del barrio mientras la app esta activa.
 * Con la app cerrada llegan por FCM (ver SaviMessagingService y functions/).
 */
public class NotificacionService extends Service {

    private static final String TAG = "NotificacionService";

    private final DatabaseReference dbNotificacion = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbNotificacion);
    private final ChildEventListener listenerNotificaciones = getListenerNotificaciones();
    private boolean escuchando;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!SesionManager.haySesion()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!escuchando) {
            dbNotificacion.addChildEventListener(listenerNotificaciones);
            escuchando = true;
            Log.i(TAG, "Escuchando notificaciones");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        dbNotificacion.removeEventListener(listenerNotificaciones);
        escuchando = false;
        super.onDestroy();
    }

    public static void procesar(Context context, Notificacion notificacion) {
        Usuario usuario = SesionManager.getUsuario();
        if (!PoliticaNotificaciones.debeNotificar(notificacion, usuario)) {
            return;
        }
        PendingIntent accion = PendingIntent.getActivity(context, 0,
                new Intent(context, MenuPrincipalActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notificador.mostrar(context, Notificador.CANAL_NOTIFICACIONES, notificacion.getId(),
                "Notificacion! " + notificacion.getTitle(), notificacion.getContenido(), accion, false);
        marcarVisto(notificacion.getId(), usuario.getId());
    }

    private static void marcarVisto(String idNotificacion, String idUsuario) {
        if (idNotificacion == null) {
            return;
        }
        // Cada usuario marca solo su propia entrada: no hace falta transaccion
        FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbNotificacion).child(idNotificacion)
                .child("vistoPor").child(idUsuario).setValue(idUsuario);
    }

    @NonNull
    private ChildEventListener getListenerNotificaciones() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                procesar(NotificacionService.this, dataSnapshot.getValue(Notificacion.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Listener de notificaciones cancelado: " + databaseError.getMessage());
            }
        };
    }
}
