package com.tangorra.matias.savi.View;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PopUpAlertasGrupo extends AppCompatActivity {

    private DatabaseReference dbGrupoVecinal;
    final ArrayList<Alerta> alertas =new ArrayList<Alerta>();

    private Context popAlarmas;

    private ExpandableListView expandableListView;
    private AdaptadorCombinado adaptadorCombinado;
    private Map<Alerta, ArrayList<Alerta>> mapChild;


    private ValueEventListener alertasListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop_up_alarmas_grupo);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int )(width*.9),(int )(heigth*.8) );

        getSupportActionBar().hide();

        popAlarmas=this;

        //consulta a base de datos
        if (SesionManager.getGrupo() != null && SesionManager.getGrupo().getId() != null){
            dbGrupoVecinal = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo).child(SesionManager.getGrupo().getId()).child("alertas");

            alertasListener = dbGrupoVecinal.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    alertas.clear();
                    for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                        Alerta alerta = imageSnapshot.getValue(Alerta.class);
                        alertas.add(alerta);
                        ArrayList<Alerta> alertasDetalle = new ArrayList<Alerta>();
                        alertasDetalle.add(alerta);
                        mapChild.put(alerta, alertasDetalle);
                    }

                    if (alertas.size()>0 && !mapChild.isEmpty()){
                        adaptadorCombinado = new AdaptadorCombinado(popAlarmas, alertas, mapChild);
                        expandableListView.setAdapter(adaptadorCombinado);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }


        expandableListView = findViewById(R.id.listHistorialAlarmasExpandible);
        mapChild = new HashMap<>();



    }

    @Override
    protected void onDestroy() {
        if (dbGrupoVecinal != null && alertasListener != null) {
            dbGrupoVecinal.removeEventListener(alertasListener);
        }
        super.onDestroy();
    }
}
