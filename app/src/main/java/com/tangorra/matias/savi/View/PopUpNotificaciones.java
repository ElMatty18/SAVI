package com.tangorra.matias.savi.View;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.ValueEventListener;
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


    private ValueEventListener notificacionesListener;

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

        dbNotificaciones = FirebaseUtils.db().getReference(FirebaseUtils.dbNotificacion);

        notificacionesListener = dbNotificaciones.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notificaciones.clear();
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    Notificacion notificacion = imageSnapshot.getValue(Notificacion.class);
                    notificaciones.add(notificacion);
                }
                adaptadorNotificacion = new AdaptadorNotificacion(popNotificaciones, notificaciones);
                listNotifaciones = findViewById(R.id.listNotificaciones);

                listNotifaciones.setAdapter(adaptadorNotificacion);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


    }

    @Override
    protected void onDestroy() {
        if (notificacionesListener != null) {
            dbNotificaciones.removeEventListener(notificacionesListener);
        }
        super.onDestroy();
    }
}
