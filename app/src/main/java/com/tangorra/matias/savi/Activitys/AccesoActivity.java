package com.tangorra.matias.savi.Activitys;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.ServiciosSesion;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.Utils.Validaciones;

import java.util.ArrayList;

public class AccesoActivity extends AppCompatActivity {

    private Boolean internet=true;

    private EditText txtEmail;
    private EditText txtClave;
    private ProgressDialog progressDialog;
    private Button registrarse, entrar;

    private static final String PREF_USUARIO = "usuario";
    // Versiones anteriores guardaban la clave en texto plano: se borra al iniciar.
    private static final String PREF_CLAVE_LEGACY = "clave";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ValueEventListener usuarioListener = getUsuarioListener();
    private ValueEventListener integrantesListener = getIntegrantesListener();

    private DatabaseReference dbGrupo = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo);
    private ValueEventListener grupoListener = getGrupoListener();

    private Usuario usuario = new Usuario();
    private Grupo grupo = new Grupo();

    private ArrayList<Usuario> listIntegrantes = new ArrayList<Usuario>();

    private Context context;

    private SharedPreferences sharedPreferences;

    private LinearLayout removeLastAccess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceso);

        context = this;

        sharedPreferences = getSharedPreferences("archivoSP", Context.MODE_PRIVATE);

        txtEmail = findViewById(R.id.txt_usuario);
        txtClave= findViewById(R.id.txt_clave);


        removeLastAccess = findViewById(R.id.removeLastAccess);
        loadLastAcces();
        removeLastAccess();

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        entrar = findViewById(R.id.btn_entrar);
        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logearUsuario();
            }
        });

        registrarse = findViewById(R.id.btn_registrar);
        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        continuarSesion();
    }

    // Firebase Auth persiste la sesion: si ya hay un usuario autenticado no hace falta pedir la clave.
    private void continuarSesion() {
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            openDialogo(StringUtils.userLogin);
            recuperarDatosUsuario(mAuth.getCurrentUser().getEmail());
        }
    }

    private void removeLastAccess() {
        removeLastAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPreferences(Context.MODE_PRIVATE).edit()
                        .remove(PREF_USUARIO)
                        .remove(PREF_CLAVE_LEGACY)
                        .apply();
                mAuth.signOut();

                txtEmail.setText("");
                txtClave.setText("");
                removeLastAccess.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loadLastAcces() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(PREF_CLAVE_LEGACY).apply();

        String usuario = sharedPreferences.getString(PREF_USUARIO,"");
        txtEmail.setText(usuario);

        if (usuario.equals("")){
            removeLastAccess.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public  void onBackPressed(){

    }

    private void logearUsuario(){
        if (formValido(false)){
            final String email = txtEmail.getText().toString().trim();
            final String pass = txtClave.getText().toString();

            if (internet){
                openDialogo(StringUtils.userLogin);
                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            closeDialogo();
                            Toast.makeText(getApplicationContext(),StringUtils.checkData, Toast.LENGTH_LONG).show();
                        } else {
                            recuperarDatosUsuario(email);
                        }
                    }
                });

            }
        }

    }

    private void openDialogo(String s) {
        progressDialog.setMessage(s);
        progressDialog.show();
    }

    private void closeDialogo() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void goToMenu(String email) {
        Intent intent = new Intent(AccesoActivity.this, MenuPrincipalActivity.class);
        intent.putExtra(MenuPrincipalActivity.usuario, email);
        startActivity(intent);
        finish();
    }


    private void goToDatosUsuario(String email) {
        Intent intent = new Intent(AccesoActivity.this, PerfilActivity.class);
        intent.putExtra(MenuPrincipalActivity.usuario, email);
        startActivity(intent);
        finish();
    }

    private void recuperarDatosUsuario(String email) {
        dbUsuarios.orderByChild("mail").equalTo(email).limitToFirst(1).addListenerForSingleValueEvent(usuarioListener);
    }

    private void recuperarDatosGrupoUsuario(String idGrupo) {
        if (idGrupo == null){
            SesionManager.setGrupo(new Grupo());
            ingresar(usuario.getMail());
            return;
        }
        dbGrupo.orderByChild("id").equalTo(idGrupo).limitToFirst(1).addListenerForSingleValueEvent(grupoListener);
    }

    @NonNull
    private ValueEventListener getUsuarioListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario encontrado = null;
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    encontrado = imageSnapshot.getValue(Usuario.class);
                }
                if (encontrado == null){
                    errorIngreso(StringUtils.userNotFound);
                    return;
                }
                usuario = encontrado;
                if (usuario.getNombre() != null && usuario.getApellido() != null){
                    Toast.makeText(getApplicationContext(),StringUtils.welcome + StringUtils.getTextoFormateado(usuario.getGlosa()), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),StringUtils.welcomeFirst, Toast.LENGTH_LONG).show();
                }
                recuperarDatosGrupoUsuario(usuario.getIdGrupo());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                errorIngreso(databaseError.getMessage());
            }
        };
    }


    @NonNull
    private ValueEventListener getGrupoListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    grupo = imageSnapshot.getValue(Grupo.class);
                }
                SesionManager.setGrupo(grupo);
                if (grupo.getId() != null){
                    recuperarIntegrantesGrupo();
                } else {
                    ingresar(usuario.getMail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                errorIngreso(databaseError.getMessage());
            }
        };
    }

    private void errorIngreso(String mensaje) {
        closeDialogo();
        mAuth.signOut();
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private void ingresar(String mail) {
        closeDialogo();
        SesionManager.setUsuario(usuario);

        // Limpieza de datos legados: la clave nunca debio persistirse en la base.
        if (usuario.getId() != null){
            dbUsuarios.child(usuario.getId()).child(PREF_CLAVE_LEGACY).removeValue();
        }

        saveAccess();

        //lanzar listener de alertas
        ServiciosSesion.iniciar(this);

        if (usuario.datosIncompletos()){
            goToDatosUsuario(mail);
        } else {
            goToMenu(mail);
        }
    }

    private void saveAccess() {
        // Solo se recuerda el mail: la sesion la mantiene Firebase Auth.
        getPreferences(Context.MODE_PRIVATE).edit()
                .putString(PREF_USUARIO, usuario.getMail())
                .apply();
    }

    private void registrarUsuario(){
        if (formValido(true)){
            final String email = txtEmail.getText().toString().trim();
            final String pass = txtClave.getText().toString();
            openDialogo(StringUtils.userRegister);
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(getListenerAuthentication(email));
        }
    }

    @NonNull
    private OnCompleteListener<AuthResult> getListenerAuthentication(final String email) {
        return new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(), StringUtils.userWithEqualMail, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), StringUtils.errorRegister, Toast.LENGTH_LONG).show();
                    }
                } else {
                    //se logueo mail + clave
                    persistir(email);
                }
                closeDialogo();
            }
        };
    }

    private boolean formValido(boolean validarFortalezaClave) {
        boolean valido=true;
        String mail = txtEmail.getText().toString().trim();
        String clave = txtClave.getText().toString();

        if (mail.isEmpty()){
            txtEmail.setError( StringUtils.fieldRequired );
            valido=false;
        } else if (!Validaciones.esMailValido(mail)){
            txtEmail.setError( StringUtils.fieldInvalid );
            valido=false;
        }

        if (clave.isEmpty()){
            txtClave.setError( StringUtils.fieldRequired );
            valido=false;
        } else if (validarFortalezaClave && !Validaciones.esClaveSegura(clave)){
            txtClave.setError( StringUtils.passInvalid );
            valido=false;
        }
        return valido;
    }

    private void persistir(String email) {
        String id = dbUsuarios.push().getKey();
        Usuario nuevo=new Usuario(id, email);
        dbUsuarios.child(id).setValue(nuevo);
    }

    @Override
    protected void onDestroy() {
        closeDialogo();
        super.onDestroy();
    }

    private void recuperarIntegrantesGrupo(){
        if (SesionManager.getGrupo().getId() != null){
            dbUsuarios.orderByChild("idGrupo").equalTo(SesionManager.getGrupo().getId()).addListenerForSingleValueEvent(integrantesListener);
        }
    }


    @NonNull
    private ValueEventListener getIntegrantesListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listIntegrantes.clear();
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    Usuario usuario = imageSnapshot.getValue(Usuario.class);
                    listIntegrantes.add(usuario);
                }
                SesionManager.getGrupo().setIntegrantes(listIntegrantes);
                ingresar(usuario.getMail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                errorIngreso(databaseError.getMessage());
            }
        };
    }
}
