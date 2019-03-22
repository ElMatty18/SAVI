package com.tangorra.matias.savi.View;

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
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.R;

import java.util.ArrayList;

public class PopUpNotificaciones extends AppCompatActivity {

    private DatabaseReference dbGrupoVecinal;

    private TextView alarmaMuestra;
    private AdaptadorAlertas adapAlarmas;
    final ArrayList<Alerta> alertas =new ArrayList<Alerta>();

    private ListView listAlarmas;

    private Context popNotificaciones;

    private Button dispararNotificacion;


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


        dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(SesionManager.getGrupo().getNombre());

        dbGrupoVecinal.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Alerta alerta = dataSnapshot.getValue(Alerta.class);
                showNotification(alerta.getAlarma(), alerta.getCasa());
                System.out.println("Previous Post ID: " + prevChildKey);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Alerta alerta = dataSnapshot.getValue(Alerta.class);
                showNotification("FINALIZO "+ alerta.getAlarma(), alerta.getCasa());
            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        dispararNotificacion=findViewById(R.id.btn_dispararNotificacion);
        dispararNotificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification("SAVI", "Notificacion");
            }
        });

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
