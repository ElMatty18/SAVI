package com.tangorra.matias.savi.ui.acceso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.tangorra.matias.savi.Activitys.MenuPrincipalActivity;
import com.tangorra.matias.savi.Activitys.PerfilActivity;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.ServiciosSesion;
import com.tangorra.matias.savi.Utils.Validaciones;
import com.tangorra.matias.savi.data.Sesion;

public class AccesoActivity extends AppCompatActivity {

    private static final String PREF_MAIL = "usuario";
    // Versiones anteriores guardaban la clave en texto plano: se borra al iniciar.
    private static final String PREF_CLAVE_LEGADA = "clave";

    private AccesoViewModel viewModel;

    private TextInputLayout campoMail;
    private TextInputLayout campoClave;
    private TextInputEditText txtMail;
    private TextInputEditText txtClave;
    private LinearProgressIndicator progreso;
    private MaterialButton btnIngresar;
    private MaterialButton btnRegistrar;
    private MaterialButton btnOtraCuenta;

    // Mientras se verifica una sesion guardada se mantiene el splash
    private boolean verificandoSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acceso);

        splash.setKeepOnScreenCondition(() -> verificandoSesion);

        View raiz = findViewById(R.id.raiz);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        campoMail = findViewById(R.id.campo_mail);
        campoClave = findViewById(R.id.campo_clave);
        txtMail = findViewById(R.id.txt_mail);
        txtClave = findViewById(R.id.txt_clave);
        progreso = findViewById(R.id.progreso);
        btnIngresar = findViewById(R.id.btn_ingresar);
        btnRegistrar = findViewById(R.id.btn_registrar);
        btnOtraCuenta = findViewById(R.id.btn_otra_cuenta);

        cargarMailRecordado();

        btnIngresar.setOnClickListener(v -> ingresar());
        btnRegistrar.setOnClickListener(v -> registrar());
        btnOtraCuenta.setOnClickListener(v -> olvidarCuenta());
        txtClave.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                ingresar();
                return true;
            }
            return false;
        });

        viewModel = new ViewModelProvider(this).get(AccesoViewModel.class);
        viewModel.estado().observe(this, this::mostrar);

        if (savedInstanceState == null) {
            verificandoSesion = viewModel.continuarSesion();
        }
    }

    private void mostrar(AccesoViewModel.Estado estado) {
        boolean cargando = estado.tipo == AccesoViewModel.Estado.Tipo.CARGANDO;
        progreso.setVisibility(cargando ? View.VISIBLE : View.INVISIBLE);
        btnIngresar.setEnabled(!cargando);
        btnRegistrar.setEnabled(!cargando);
        campoMail.setEnabled(!cargando);
        campoClave.setEnabled(!cargando);

        if (estado.tipo == AccesoViewModel.Estado.Tipo.ERROR) {
            verificandoSesion = false;
            Snackbar.make(findViewById(R.id.raiz), estado.mensaje, Snackbar.LENGTH_LONG).show();
        } else if (estado.tipo == AccesoViewModel.Estado.Tipo.INGRESO) {
            entrar(estado.usuario);
        }
    }

    private void ingresar() {
        if (formularioValido(false)) {
            viewModel.ingresar(texto(txtMail), clave());
        }
    }

    private void registrar() {
        if (formularioValido(true)) {
            viewModel.registrar(texto(txtMail), clave());
        }
    }

    private void entrar(Usuario usuario) {
        // Solo se recuerda el mail: la sesion la mantiene Firebase Auth
        preferencias().edit().putString(PREF_MAIL, usuario.getMail()).apply();

        Sesion.iniciar(usuario.getId());
        ServiciosSesion.iniciar(this);

        Class<?> destino = usuario.datosIncompletos() ? PerfilActivity.class : MenuPrincipalActivity.class;
        startActivity(new Intent(this, destino).putExtra(MenuPrincipalActivity.usuario, usuario.getMail()));
        finish();
    }

    private boolean formularioValido(boolean esRegistro) {
        campoMail.setError(null);
        campoClave.setError(null);
        boolean valido = true;

        String mail = texto(txtMail);
        if (mail.isEmpty()) {
            campoMail.setError(getString(R.string.error_campo_requerido));
            valido = false;
        } else if (!Validaciones.esMailValido(mail)) {
            campoMail.setError(getString(R.string.error_mail_invalido));
            valido = false;
        }

        String clave = clave();
        if (clave.isEmpty()) {
            campoClave.setError(getString(R.string.error_campo_requerido));
            valido = false;
        } else if (esRegistro && !Validaciones.esClaveSegura(clave)) {
            campoClave.setError(getString(R.string.error_clave_insegura));
            valido = false;
        }
        return valido;
    }

    private void cargarMailRecordado() {
        SharedPreferences preferencias = preferencias();
        preferencias.edit().remove(PREF_CLAVE_LEGADA).apply();

        String mail = preferencias.getString(PREF_MAIL, "");
        txtMail.setText(mail);
        btnOtraCuenta.setVisibility(mail.isEmpty() ? View.GONE : View.VISIBLE);
        if (!mail.isEmpty()) {
            txtClave.requestFocus();
        }
    }

    private void olvidarCuenta() {
        preferencias().edit().remove(PREF_MAIL).apply();
        FirebaseAuth.getInstance().signOut();
        txtMail.setText("");
        txtClave.setText("");
        btnOtraCuenta.setVisibility(View.GONE);
        txtMail.requestFocus();
    }

    private SharedPreferences preferencias() {
        // Mismo archivo que usaba la version anterior (getPreferences de AccesoActivity)
        return getSharedPreferences("AccesoActivity", Context.MODE_PRIVATE);
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private String clave() {
        return txtClave.getText() != null ? txtClave.getText().toString() : "";
    }
}
