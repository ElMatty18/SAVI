package com.tangorra.matias.savi.View;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Adaptadores.AdaptadorAlertas;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;

public class PopUpFamiliarAlertas extends AppCompatActivity {

    private DatabaseReference dbGrupoVecinal;
    private ValueEventListener alarmasFamiliar = getAlarmasFamiliarListener();

    private Usuario usuario;


    private TextView alarmaMuestra;
    private AdaptadorAlertas adapAlarmas;
    final ArrayList<Alerta> alertas =new ArrayList<Alerta>();

    private ListView listAlarmas;

    private Context popAlarmasFamilia;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up_alarmas_familia);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int )(width*.9),(int )(heigth*.7) );

        getSupportActionBar().hide();

        popAlarmasFamilia = this;

        usuario = (Usuario) getIntent().getSerializableExtra("usuarioSelecc");

        //consulta a base de datos
        if (usuario.getIdGrupo() != null){
            dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(usuario.getIdGrupo()).child("alertas");
            dbGrupoVecinal.addValueEventListener(alarmasFamiliar);
        }
    }


    @NonNull
    private ValueEventListener getAlarmasFamiliarListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                alertas.clear();
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    Alerta alerta = imageSnapshot.getValue(Alerta.class);
                    alertas.add(alerta);
                }
                adapAlarmas = new AdaptadorAlertas(popAlarmasFamilia, alertas);
                listAlarmas = findViewById(R.id.listHistorialAlarmas);

                listAlarmas.setAdapter(adapAlarmas);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
    }



}