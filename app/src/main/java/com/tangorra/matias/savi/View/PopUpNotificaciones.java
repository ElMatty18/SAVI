package com.tangorra.matias.savi.View;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Activitys.MainActivity;
import com.tangorra.matias.savi.Adaptadores.AdaptadorAlertas;
import com.tangorra.matias.savi.Adaptadores.AdaptadorNotificacion;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;

public class PopUpNotificaciones extends AppCompatActivity {

    private DatabaseReference dbNotificaciones;

    private AdaptadorNotificacion adaptadorNotificacion;

    final ArrayList<Notificacion> notificaciones =new ArrayList<Notificacion>();

    private ListView listNotifaciones;

    private Context popNotificaciones;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up_notificaciones);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int )(width*.9),(int )(heigth*.7) );

        getSupportActionBar().hide();

        popNotificaciones = this;


        dbNotificaciones = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbNotificacion);

        dbNotificaciones.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    Notificacion notification = dataSnapshot.getValue(Notificacion.class);
                    if (condicionNotificacion(notification)){
                        addNotification(notification);
                    }
                }

                crearVistaNotificaciones();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });



    }

    private void crearVistaNotificaciones() {
        Notificacion nueva = new Notificacion();
        nueva.setTitle("laalala");
        nueva.setContenido("tipin");
        notificaciones.add(nueva);

        adaptadorNotificacion = new AdaptadorNotificacion(popNotificaciones, notificaciones);
        listNotifaciones = findViewById(R.id.listNotificaciones);

        listNotifaciones.setAdapter(adaptadorNotificacion);
    }

    private void addNotification(Notificacion notificacion) {
        notificaciones.add(notificacion);
    }

    private boolean condicionNotificacion(Notificacion notificacion) {
        return true;
    }



}
