package com.tangorra.matias.savi.Activitys;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.View.PopUpDomicilio;
import com.tangorra.matias.savi.View.PopUpDomicilioAlternativo;

import java.util.Calendar;
import java.util.Date;

public class PerfilActivity extends AppCompatActivity {


    private Button volver;

    private EditText nombreUsuario;
    private EditText apellidoUsuario;
    private EditText dniUsuario;
    private EditText nacimientoUsuario;
    private EditText celularUsuario;
    private EditText fijoUsuario;
    private Button guardar;

    private String nombreImagen;

    private LinearLayout fotoCasa;

    private LinearLayout lyMapaDomicilio;
    private LinearLayout lyMapaDomicilioSecundario;

    private static final int GALERY_INTENT=1;

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);
    private StorageReference storageUsuarios;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Date fechaNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        volver= findViewById(R.id.btnVolverMenuPerfil);
        guardar = findViewById(R.id.btnSavePerfil);
        nombreUsuario = findViewById(R.id.txtNombreUsuario);
        apellidoUsuario = findViewById(R.id.txtApellidoUsuario);
        dniUsuario = findViewById(R.id.txtDniUsuario);
        nacimientoUsuario = findViewById(R.id.dateFechaNacimientoUsuario);
        celularUsuario = findViewById(R.id.txtCelularUsuario);
        fijoUsuario = findViewById(R.id.txtTelefonoUsuario);
        fotoCasa = findViewById(R.id.perfilFoto);
        mDisplayDate = findViewById(R.id.tvDate);

        fechaPicker();

        lyMapaDomicilio = findViewById(R.id.perfilDomicilio);
        lyMapaDomicilio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PerfilActivity.this, PopUpDomicilio.class));
            }
        });

        lyMapaDomicilioSecundario = findViewById(R.id.perfilDomicilioAlternativo);
        lyMapaDomicilioSecundario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PerfilActivity.this, PopUpDomicilioAlternativo.class));
            }
        });

        cargarDatosUsuario();

        storageUsuarios = FirebaseStorage.getInstance().getReference();

        fotoCasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALERY_INTENT);
            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent navegar = new Intent(PerfilActivity.this, MenuPrincipalActivity.class);
                startActivity(navegar);
                finish();
            }
        });


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (formValido()){
                    Usuario usuario = SesionManager.getUsuario();
                    if (usuario.getId()== null){
                        //primer ingreso
                        String id = dbUsuarios.push().getKey();
                        usuario.setId(id);
                    }

                    GuardarUsuario(usuario);

                    Intent navegar = new Intent(PerfilActivity.this, MenuPrincipalActivity.class);
                    startActivity(navegar);
                    finish();
                }
            }
        });
    }

    private void fechaPicker() {
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        PerfilActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day
                        );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month ++;
                String date = dayOfMonth + "/" + month + "/" + year;
                mDisplayDate.setText(date);
                fechaNacimiento= new Date(year, month,dayOfMonth);
            }
        };
    }

    private void GuardarUsuario(Usuario usuario) {
        usuario.setNombre(nombreUsuario.getText().toString().trim());
        usuario.setApellido(apellidoUsuario.getText().toString().trim());
        usuario.setDni(dniUsuario.getText().toString().trim());
        usuario.setFijo(fijoUsuario.getText().toString().trim());
        usuario.setCelular(celularUsuario.getText().toString().trim());
        usuario.setFechaNacimiento(fechaNacimiento);

        dbUsuarios.child(usuario.getId()).setValue(usuario);
        dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario).child(usuario.getId());
        dbUsuarios.child("perfil").child("domicilio").setValue(usuario.getPerfil().getDomicilio());
        dbUsuarios.child("perfil").child("domicilioAlterno").setValue(usuario.getPerfil().getDomicilioAlterno());
    }

    private boolean formValido() {
        boolean valido=true;
        if( nombreUsuario.getText().toString().length() == 0 ){
            nombreUsuario.setError( "Campo requerido!" );
            valido=false;
        }
        if( apellidoUsuario.getText().toString().length() == 0 ){
            apellidoUsuario.setError( "Campo requerido!" );
            valido=false;
        }
        if( dniUsuario.getText().toString().length() == 0 ){
            dniUsuario.setError( "Campo requerido!" );
            valido=true;
        }
        if( fijoUsuario.getText().toString().length() == 0 ){
            fijoUsuario.setError( "Campo requerido!" );
            valido=false;
        }
        if( celularUsuario.getText().toString().length() == 0 ){
            celularUsuario.setError( "Campo requerido!" );
            valido=false;
        }
        if( mDisplayDate.getText().toString().length() == 0 ){
            mDisplayDate.setError( "Campo requerido!" );
            valido=false;
        }
        return valido;
    }

    private void cargarDatosUsuario(){
        nombreUsuario.setText(SesionManager.getUsuario().getNombre());
        apellidoUsuario.setText(SesionManager.getUsuario().getApellido());
        dniUsuario.setText(SesionManager.getUsuario().getDni());
        fijoUsuario.setText(SesionManager.getUsuario().getFijo());
        celularUsuario.setText(SesionManager.getUsuario().getCelular());

        if (SesionManager.getUsuario().getFechaNacimiento() != null){
            int year = SesionManager.getUsuario().getFechaNacimiento().getYear();
            int month = SesionManager.getUsuario().getFechaNacimiento().getMonth();
            int dayOfMonth = SesionManager.getUsuario().getFechaNacimiento().getDate();

            String date = dayOfMonth + "/" + month + "/" + year;
            mDisplayDate.setText(date);
        }
    }

    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(PerfilActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_INTENT && resultCode == RESULT_OK){
            Uri uri = data.getData();
            nombreImagen= uri.getLastPathSegment();
            StorageReference filePath = storageUsuarios.child("Fotos/"+SesionManager.getUsuario().getId());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri descargarFoto = taskSnapshot.getDownloadUrl();
                    Toast.makeText(PerfilActivity.this, "Se guardo la foto correctamente", Toast.LENGTH_LONG).show();;
                }
            });
        }
    }
}
