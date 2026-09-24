package com.tangorra.matias.savi.ui.perfil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tangorra.matias.savi.Activitys.MenuPrincipalActivity;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.Utils.Validaciones;
import com.tangorra.matias.savi.ui.comun.Avatares;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** Datos personales y foto. Tambien se usa para completar el perfil en el primer ingreso. */
public class PerfilActivity extends AppCompatActivity {

    private static final String EXTRA_PRIMERA_VEZ = "primeraVez";

    public static Intent intent(Context context, boolean primeraVez) {
        return new Intent(context, PerfilActivity.class).putExtra(EXTRA_PRIMERA_VEZ, primeraVez);
    }

    private PerfilViewModel viewModel;
    private boolean primeraVez;

    private TextInputLayout campoNombre;
    private TextInputLayout campoApellido;
    private TextInputLayout campoDni;
    private TextInputLayout campoCelular;
    private TextInputEditText txtNombre;
    private TextInputEditText txtApellido;
    private TextInputEditText txtDni;
    private TextInputEditText txtNacimiento;
    private TextInputEditText txtCelular;
    private TextInputEditText txtFijo;
    private ImageView avatar;
    private Date nacimiento;

    private final ActivityResultLauncher<PickVisualMediaRequest> selectorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::fotoElegida);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);
        Usuario usuario = viewModel.usuario();
        if (usuario == null) {
            finish();
            return;
        }
        primeraVez = getIntent().getBooleanExtra(EXTRA_PRIMERA_VEZ, false) || usuario.datosIncompletos();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (primeraVez) {
            // Hasta completar los datos obligatorios no se puede seguir
            toolbar.setTitle(R.string.perfil_titulo_primera_vez);
            toolbar.setNavigationIcon(null);
            findViewById(R.id.bienvenida).setVisibility(View.VISIBLE);
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    moveTaskToBack(true);
                }
            });
        } else {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        vincularVistas();
        cargar(usuario);

        viewModel.foto().observe(this, uri -> Glide.with(this).load(uri).placeholder(R.drawable.icon_casa).centerCrop().into(avatar));
        viewModel.trabajando().observe(this, t -> {
            findViewById(R.id.progreso).setVisibility(t ? View.VISIBLE : View.GONE);
            findViewById(R.id.btn_guardar).setEnabled(!t);
        });
        viewModel.errorFoto().observe(this, evento -> {
            String mensaje = evento.consumir();
            if (mensaje != null) {
                Snackbar.make(findViewById(R.id.raiz), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.resultado().observe(this, evento -> {
            PerfilViewModel.Resultado r = evento.consumir();
            if (r == PerfilViewModel.Resultado.GUARDADO) {
                if (primeraVez) {
                    startActivity(new Intent(this, MenuPrincipalActivity.class));
                }
                finish();
            } else if (r == PerfilViewModel.Resultado.ERROR) {
                Snackbar.make(findViewById(R.id.raiz), R.string.perfil_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void vincularVistas() {
        campoNombre = findViewById(R.id.campo_nombre);
        campoApellido = findViewById(R.id.campo_apellido);
        campoDni = findViewById(R.id.campo_dni);
        campoCelular = findViewById(R.id.campo_celular);
        txtNombre = findViewById(R.id.txt_nombre);
        txtApellido = findViewById(R.id.txt_apellido);
        txtDni = findViewById(R.id.txt_dni);
        txtNacimiento = findViewById(R.id.txt_nacimiento);
        txtCelular = findViewById(R.id.txt_celular);
        txtFijo = findViewById(R.id.txt_fijo);
        avatar = findViewById(R.id.avatar);

        View.OnClickListener elegirFoto = v -> selectorFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        avatar.setOnClickListener(elegirFoto);
        findViewById(R.id.btn_foto).setOnClickListener(elegirFoto);
        txtNacimiento.setOnClickListener(v -> elegirFecha());
        ((TextInputLayout) findViewById(R.id.campo_nacimiento)).setEndIconOnClickListener(v -> elegirFecha());
        findViewById(R.id.btn_domicilios).setOnClickListener(v -> startActivity(new Intent(this, DomiciliosActivity.class)));
        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());

        View boton = findViewById(R.id.btn_guardar);
        ViewCompat.setOnApplyWindowInsetsListener(boton, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });
    }

    private void cargar(Usuario usuario) {
        txtNombre.setText(formateado(usuario.getNombre()));
        txtApellido.setText(formateado(usuario.getApellido()));
        txtDni.setText(usuario.getDni());
        txtCelular.setText(usuario.getCelular());
        txtFijo.setText(usuario.getFijo());
        nacimiento = DateUtils.normalizar(usuario.getFechaNacimiento());
        mostrarFecha();
        Avatares.cargar(avatar, usuario.getId());
    }

    private void elegirFecha() {
        long seleccion = nacimiento != null ? aUtc(nacimiento) : MaterialDatePicker.todayInUtcMilliseconds();
        MaterialDatePicker<Long> selector = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.perfil_nacimiento)
                .setSelection(seleccion)
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
                        .build())
                .build();
        selector.addOnPositiveButtonClickListener(utc -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(utc);
            nacimiento = DateUtils.fecha(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            mostrarFecha();
        });
        selector.show(getSupportFragmentManager(), "fecha");
    }

    /** El selector trabaja en UTC; la fecha guardada es medianoche local. */
    private static long aUtc(Date local) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(local);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis();
    }

    private void mostrarFecha() {
        txtNacimiento.setText(nacimiento != null ? DateUtils.sdf3.format(nacimiento) : "");
    }

    private void fotoElegida(Uri uri) {
        if (uri == null) {
            return;
        }
        Glide.with(this).load(uri).centerCrop().into(avatar);
        try {
            viewModel.subirFoto(Avatares.prepararParaSubir(getContentResolver(), uri));
        } catch (IOException e) {
            Snackbar.make(findViewById(R.id.raiz), "No se pudo leer la imagen", Snackbar.LENGTH_LONG).show();
        }
    }

    private void guardar() {
        boolean valido = requerido(campoNombre, txtNombre) & requerido(campoApellido, txtApellido)
                & requerido(campoCelular, txtCelular);
        campoDni.setError(null);
        if (!Validaciones.esDniValido(texto(txtDni))) {
            campoDni.setError(getString(R.string.perfil_dni_invalido));
            valido = false;
        }
        if (!valido) {
            return;
        }
        viewModel.guardar(texto(txtNombre).toLowerCase(), texto(txtApellido).toLowerCase(),
                texto(txtDni).replace(".", ""), texto(txtCelular), texto(txtFijo), nacimiento);
    }

    private boolean requerido(TextInputLayout campo, TextInputEditText texto) {
        boolean ok = !texto(texto).isEmpty();
        campo.setError(ok ? null : getString(R.string.error_campo_requerido));
        return ok;
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private static String formateado(String valor) {
        return valor == null ? "" : StringUtils.getTextoFormateado(valor).trim();
    }
}
