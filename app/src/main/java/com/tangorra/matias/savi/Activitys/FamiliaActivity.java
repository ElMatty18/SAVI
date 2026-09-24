package com.tangorra.matias.savi.Activitys;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tangorra.matias.savi.Adaptadores.AdaptadorUsuarios;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;
import java.util.Map;
import java.util.HashMap;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.View.PopUpViewQRPersona;

import java.util.ArrayList;
import java.util.List;

public class FamiliaActivity extends AppCompatActivity {

    private ListView listViewPersonas;

    private AdaptadorUsuarios adaptadorFamilia;

    private Button scanFamiiar;
    private Button verCodigoPersona;
    private Button cancelar;
    private Button salirFamilia;

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ValueEventListener usuarioListener = getUsuarioListener();
    private ValueEventListener usuarioListenerFamiliar = getUsuarioListenerFamiliar();

    private DatabaseReference dbFamilias = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbFamilia);
    private ValueEventListener familiaListener = getFamiliaListener();
    private ValueEventListener familiaBorrarListener = getBorrarFamiliaListener();

    private ArrayList<Usuario> listFamiliares = new ArrayList<Usuario>();

    private DatabaseReference refFamilia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familia);
        final Activity activity = this;

        listViewPersonas = (ListView) findViewById(R.id.listViewFamilia);

        scanFamiiar = findViewById(R.id.addFamilia);
        verCodigoPersona = findViewById(R.id.btnCodigoPersona);
        salirFamilia = findViewById(R.id.btnSalirGrupoFamiliar);

        cancelar = findViewById(R.id.btnVolverMenu);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent volverMenu = new Intent(FamiliaActivity.this, MenuPrincipalActivity.class);
                startActivity(volverMenu);
            }
        });

        scanFamiiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirScan(activity);
            }
        });

        verCodigoPersona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamiliaActivity.this, PopUpViewQRPersona.class));
            }
        });

        cargarFamilia();

        popUpEliminarFamilia();


    }

    private void popUpEliminarFamilia() {
        final AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Eliminar");
        dialogo1.setMessage("¿ Desea cancelar su suscripción al grupo familiar ?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                aceptarBorrado();
            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                dialogo1.cancel();
            }
        });

        salirFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo1.show();
            }
        });
    }

    private void aceptarBorrado() {
        //eliminar persona actual del grupo familiar
        dbUsuarios.child(SesionManager.getUsuario().getId()).child("idFamilia").setValue(null);
        dbFamilias.child(SesionManager.getUsuario().getIdFamilia()).addListenerForSingleValueEvent(familiaBorrarListener);
    }

    private void getVistaFamilia() {
        adaptadorFamilia = new AdaptadorUsuarios(this, getFamiliares());
        listViewPersonas.setAdapter(adaptadorFamilia);
    }

    private void cargarFamilia() {
        if (SesionManager.getUsuario().getIdFamilia() != null) {
            if (refFamilia != null) {
                refFamilia.removeEventListener(familiaListener);
            }
            refFamilia = dbFamilias.child(SesionManager.getUsuario().getIdFamilia());
            refFamilia.addValueEventListener(familiaListener);
        }
    }

    private void abrirScan(Activity activity) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("SAVI ScanQR - Familia");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onBackPressed() {
        listViewPersonas.setAdapter(null);
        Intent menu = new Intent(FamiliaActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }

    private ArrayList<Usuario> getFamiliares() {
        return listFamiliares;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Se cancelo la busqueda", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                dbUsuarios.orderByChild("id").equalTo(result.getContents()).limitToFirst(1).addListenerForSingleValueEvent(usuarioListener);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static void agregarAFamilia(Map<String, Object> cambios, String idFamilia, String idUsuario) {
        cambios.put(FirebaseUtils.dbFamilia + "/" + idFamilia + "/" + idUsuario, idUsuario);
        cambios.put(FirebaseUtils.dbUsuario + "/" + idUsuario + "/idFamilia", idFamilia);
    }

    @NonNull
    private ValueEventListener getUsuarioListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = null;
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    usuario = imageSnapshot.getValue(Usuario.class);
                }
                Usuario yo = SesionManager.getUsuario();
                if (usuario == null || usuario.getId() == null || usuario.getId().equals(yo.getId())) {
                    Toast.makeText(getApplicationContext(), StringUtils.userNotFound, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Se agrega " + usuario.getNombre() + " " + usuario.getApellido(), Toast.LENGTH_LONG).show();

                // Una sola escritura atomica. Las entradas de /Familia usan el uid como clave, y del otro
                // usuario solo se modifica idFamilia (antes se reescribia su registro completo).
                Map<String, Object> cambios = new HashMap<>();
                String idFamilia;
                if (yo.getIdFamilia() == null && usuario.getIdFamilia() == null) {
                    //se crea un grupo nuevo, con los dos usuario
                    idFamilia = dbFamilias.push().getKey();
                    agregarAFamilia(cambios, idFamilia, yo.getId());
                    agregarAFamilia(cambios, idFamilia, usuario.getId());
                    yo.setIdFamilia(idFamilia);
                } else if (yo.getIdFamilia() != null) {
                    //agrego a la familia de u1 el usuario u2
                    idFamilia = yo.getIdFamilia();
                    agregarAFamilia(cambios, idFamilia, usuario.getId());
                } else {
                    //agrego a la familia de u2 el usuario u1
                    idFamilia = usuario.getIdFamilia();
                    agregarAFamilia(cambios, idFamilia, yo.getId());
                    yo.setIdFamilia(idFamilia);
                }
                FirebaseDatabase.getInstance().getReference().updateChildren(cambios);

                cargarFamilia();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @NonNull
    private ValueEventListener getUsuarioListenerFamiliar() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    Usuario usuario = imageSnapshot.getValue(Usuario.class);
                    listFamiliares.add(usuario);
                }
                getVistaFamilia();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
    };


    @NonNull
    private ValueEventListener getBorrarFamiliaListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> idsFamilia = new ArrayList<String>();
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    String value = imageSnapshot.getValue(String.class);
                    String key = imageSnapshot.getKey();
                    if (value.equals(SesionManager.getUsuario().getId())){
                        dbFamilias.child(SesionManager.getUsuario().getIdFamilia()).child(key).removeValue();
                        listViewPersonas.setAdapter(null);
                        SesionManager.getUsuario().setIdFamilia(null);
                        onBackPressed();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void cargarDatosFamilia(List<String> idsFamilia){
        //inicializo familiares antes de cargarlos a todos
        listFamiliares = new ArrayList<Usuario>();
        dbUsuarios.removeEventListener(usuarioListenerFamiliar);
        for (String idFamiliar: idsFamilia) {
            dbUsuarios.orderByChild("id").equalTo(idFamiliar).limitToFirst(1).addListenerForSingleValueEvent(usuarioListenerFamiliar);
        }
        getVistaFamilia();
    }


    @Override
    protected void onDestroy() {
        // El listener se registra sobre el nodo de la familia: hay que quitarlo de esa misma referencia
        if (refFamilia != null) {
            refFamilia.removeEventListener(familiaListener);
        }
        super.onDestroy();
    }
}