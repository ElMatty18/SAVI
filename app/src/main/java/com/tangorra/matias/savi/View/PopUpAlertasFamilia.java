package com.tangorra.matias.savi.View;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Adaptadores.AdaptadorAlertas;
import com.tangorra.matias.savi.Adaptadores.AdaptadorCombinado;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopUpAlertasFamilia extends AppCompatActivity {

    private DatabaseReference dbGrupoVecinal;
    private ValueEventListener alarmasFamiliar = getAlarmasFamiliarListener();

    private Usuario usuario;

    private TextView alarmaMuestra;
    private AdaptadorAlertas adapAlarmas;
    final ArrayList<Alerta> alertas =new ArrayList<Alerta>();

    private ListView listAlarmas;

    private Context popAlarmasFamilia;
    private DatabaseReference dbFamilias = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbFamilia);
    private ValueEventListener familiaListener = getFamiliaListener();

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ArrayList<Usuario> listFamiliares = new ArrayList<Usuario>();
    private ValueEventListener usuarioListenerFamiliar = getUsuarioListenerFamiliar();


    private ExpandableListView expandableListView;
    private AdaptadorCombinado adaptadorCombinado;
    private Map<Alerta, ArrayList<Alerta>> mapChild;


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

        expandableListView = findViewById(R.id.listHistorialAlarmasFamiliaExpandible);
        mapChild = new HashMap<>();

        alertas.clear();
        cargarFamilia();


    }

    private void cargarFamilia() {
        if (SesionManager.getUsuario().getIdFamilia() != null) {
            dbFamilias.child(SesionManager.getUsuario().getIdFamilia()).addValueEventListener(familiaListener);
        }
    }


    @NonNull
    private ValueEventListener getAlarmasFamiliarListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    Alerta alerta = imageSnapshot.getValue(Alerta.class);
                    alertas.add(alerta);
                    ArrayList<Alerta> alertasDetalle = new ArrayList<Alerta>();
                    alertasDetalle.add(alerta);
                    mapChild.put(alerta, alertasDetalle);
                }

                if (alertas.size()>0 && !mapChild.isEmpty()){
                    adaptadorCombinado = new AdaptadorCombinado(popAlarmasFamilia, alertas, mapChild);
                    expandableListView.setAdapter(adaptadorCombinado);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
    }


    @NonNull
    private ValueEventListener getFamiliaListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> idsFamilia = new ArrayList<String>();
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    String idUsuario = imageSnapshot.getValue(String.class);
                    idsFamilia.add(idUsuario);
                }
                cargarDatosFamilia(idsFamilia);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }


    private void cargarDatosFamilia(List<String> idsFamilia){
        listFamiliares = new ArrayList<Usuario>();
        for (String idFamiliar: idsFamilia) {
            dbUsuarios.orderByChild("id").equalTo(idFamiliar).limitToFirst(1).addValueEventListener(usuarioListenerFamiliar);
        }

    }


    @NonNull
    private ValueEventListener getUsuarioListenerFamiliar() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    Usuario usuarioFamiliar = imageSnapshot.getValue(Usuario.class);
                    listFamiliares.add(usuarioFamiliar);

                    if (usuarioFamiliar != null && usuarioFamiliar.getIdGrupo() != null){
                        dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(usuarioFamiliar.getIdGrupo()).child("alertas");
                        dbGrupoVecinal.addValueEventListener(alarmasFamiliar);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }



}
