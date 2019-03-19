package com.tangorra.matias.savi.Service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Activitys.MainActivity;
import com.tangorra.matias.savi.Entidades.Alarma;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class EventIntentService extends IntentService {

    private static final String TAG = "EventIntentService";

    private DatabaseReference dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId());
    private ChildEventListener listenerAlertas = getListenerAlertas();

    public EventIntentService() {
        super("EventIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        String dataString = workIntent.getDataString();
        Log.i(TAG, "The service is on");

        dbGrupoVecinal.child("alarmas").addChildEventListener(listenerAlertas);

    }




    @NonNull
    private ChildEventListener getListenerAlertas() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Alarma alarma = dataSnapshot.getValue(Alarma.class);
                    if (!vistoUsuario(alarma.getVistoPor(), SesionManager.getUsuario().getId())
                            && (alarma.getId() != null) && (alarma.getCasa() != null)){
                        showNotification(alarma.getAlarma(), alarma.getCasa());
                        marcarVisto(alarma, SesionManager.getUsuario().getId());
                    }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Alarma alarma = dataSnapshot.getValue(Alarma.class);
                if (!vistoUsuario(alarma.getVistoPor(), SesionManager.getUsuario().getId())
                        && (alarma.getId() != null) && (alarma.getCasa() != null)){
                    showNotification(alarma.getAlarma(), alarma.getCasa());
                    marcarVisto(alarma, SesionManager.getUsuario().getId());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Alarma alarma = dataSnapshot.getValue(Alarma.class);
                //showNotification(alarma.getAlarma(), alarma.getCasa());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    private void marcarVisto(Alarma alarma, String id) {
        if (alarma.getVistoPor() == null){
            alarma.setVistoPor(new ArrayList<String>());
        }
        alarma.getVistoPor().add(id);
        if (alarma.getId() != null){
            dbGrupoVecinal.child("alarmas").child(alarma.getId()).setValue(alarma);
        }

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
}
