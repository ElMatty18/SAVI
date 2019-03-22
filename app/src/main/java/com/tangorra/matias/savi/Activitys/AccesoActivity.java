package com.tangorra.matias.savi.Activitys;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.AlertaService;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

public class AccesoActivity extends AppCompatActivity {

    private Boolean internet=true;

    private EditText txtEmail;
    private EditText txtClave;
    private ProgressDialog progressDialog;
    private Button registrarse, entrar;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener = getmAuthListener();

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private ValueEventListener usuarioListener = getUsuarioListener();

    private DatabaseReference dbGrupo = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbGrupo);
    private ValueEventListener grupoListener = getGrupoListener();

    private Usuario usuario = new Usuario();
    private Grupo grupo = new Grupo();

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



    }

    private void removeLastAccess() {
        removeLastAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getPreferences(context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("usuario", "");
                editor.putString("clave", "");
                editor.commit();

                txtEmail.setText("");
                txtClave.setText("");
                removeLastAccess.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loadLastAcces() {
        SharedPreferences sharedPreferences = getPreferences(context.MODE_PRIVATE);
        String usuario = sharedPreferences.getString("usuario","");
        String clave = sharedPreferences.getString("clave","");

        txtEmail.setText(usuario);
        txtClave.setText(clave);

        if (usuario.equals("") || clave.equals("")){
            removeLastAccess.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    private FirebaseAuth.AuthStateListener getmAuthListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Intent menu = new Intent(AccesoActivity.this, MenuPrincipalActivity.class);
                    startActivity(menu);
                    finish();
                } else {
                    //no esta logueado
                }
            }
        };
    }

    @Override
    public  void onBackPressed(){

    }

    private void logearUsuario(){
        if (formValido()){
            final String email = txtEmail.getText().toString().trim();
            final String pass = txtClave.getText().toString().trim();

            if ((!email.isEmpty() && !pass.isEmpty()) && internet){
                openDialogo(StringUtils.userLogin);
                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful() && internet){
                            if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(getApplicationContext(),StringUtils.userWithEqualMail, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),StringUtils.checkData, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            recuperarDatosUsuario(email);
                        }
                        closeDialogo();
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
        dbUsuarios.orderByChild("mail").equalTo(email).limitToFirst(1).addValueEventListener(usuarioListener);
    }

    private void recuperarDatosGrupoUsuario(String idGrupo) {
        dbGrupo.orderByChild("id").equalTo(idGrupo).limitToFirst(1).addValueEventListener(grupoListener);
    }

    @NonNull
    private ValueEventListener getUsuarioListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    usuario = imageSnapshot.getValue(Usuario.class);
                    Toast.makeText(getApplicationContext(),StringUtils.welcome + StringUtils.getTextoFormateado(usuario.getGlosa()), Toast.LENGTH_LONG).show();
                }
                recuperarDatosGrupoUsuario(usuario.getIdGrupo());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                ingresar(usuario.getMail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void ingresar(String mail) {
        SesionManager.setUsuario(usuario);

        saveAccess();

        //lanzar listener de alertas
        listenerAlertas();

        if (usuario.datosIncompletos()){
            goToDatosUsuario(mail);
        } else {
            goToMenu(mail);
        }
    }

    private void saveAccess() {
        SharedPreferences sharedPreferences = getPreferences(context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("usuario", usuario.getMail());
        editor.putString("clave", usuario.getClave());
        editor.commit();
    }

    private void listenerAlertas() {
        Intent intent = new Intent(this, AlertaService.class);
        startService(intent);
    }

    private void registrarUsuario(){
        if (formValido()){
            final String email = txtEmail.getText().toString().trim();
            final String pass = txtClave.getText().toString().trim();
            if (!email.isEmpty() && !pass.isEmpty()){
                openDialogo(StringUtils.userRegister);
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(getListenerAuthentication(email, pass));
            }
        }
    }

    @NonNull
    private OnCompleteListener<AuthResult> getListenerAuthentication(final String email, final String pass) {
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
                    persistir(email, pass);
                }
                closeDialogo();
            }
        };
    }

    private boolean formValido() {
        boolean valido=true;
        if( txtEmail.getText().toString().length() == 0 ){
            txtEmail.setError( StringUtils.fieldRequired );
            valido=false;
        }
        if( txtClave.getText().toString().length() == 0 ){
            txtClave.setError( StringUtils.fieldRequired );
            valido=false;
        }
        return valido;
    }

    private void persistir(String email, String pass) {
        String id = dbUsuarios.push().getKey();
        Usuario nuevo=new Usuario(id, email, pass);
        dbUsuarios.child(id).setValue(nuevo);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbUsuarios.removeEventListener(usuarioListener);
    }
}
