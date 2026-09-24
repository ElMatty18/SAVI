package com.tangorra.matias.savi.ui.grupo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.ServiciosSesion;
import com.tangorra.matias.savi.data.GrupoRepositorio;
import com.tangorra.matias.savi.ui.comun.SelectorUbicacionActivity;

/** Crear un grupo vecinal: nombre, centro de la zona y radio. */
public class CrearGrupoActivity extends AppCompatActivity {

    private static final String ESTADO_LAT = "lat";
    private static final String ESTADO_LNG = "lng";

    private TextInputEditText txtNombre;
    private TextView textoUbicacion;
    private TextView textoRadio;
    private Slider radio;
    private MaterialButton btnCrear;
    private LatLng centro;

    private final ActivityResultLauncher<SelectorUbicacionActivity.Pedido> selector = registerForActivityResult(
            new SelectorUbicacionActivity.Contrato(), elegido -> {
                if (elegido != null) {
                    centro = elegido;
                    mostrarUbicacion();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_grupo);

        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        txtNombre = findViewById(R.id.txt_nombre);
        textoUbicacion = findViewById(R.id.texto_ubicacion);
        textoRadio = findViewById(R.id.texto_radio);
        radio = findViewById(R.id.radio);
        btnCrear = findViewById(R.id.btn_crear);

        if (savedInstanceState != null && savedInstanceState.containsKey(ESTADO_LAT)) {
            centro = new LatLng(savedInstanceState.getDouble(ESTADO_LAT), savedInstanceState.getDouble(ESTADO_LNG));
        }
        mostrarUbicacion();
        mostrarRadio();

        radio.setLabelFormatter(valor -> getString(R.string.grupo_metros, (int) valor));
        radio.addOnChangeListener((s, valor, delUsuario) -> mostrarRadio());
        findViewById(R.id.tarjeta_ubicacion).setOnClickListener(v -> selector.launch(new SelectorUbicacionActivity.Pedido(
                getString(R.string.grupo_elegir_centro), centro, (int) radio.getValue())));
        txtNombre.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { actualizarBoton(); }
        });
        btnCrear.setOnClickListener(v -> crear());

        ViewCompat.setOnApplyWindowInsetsListener(btnCrear, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        if (centro != null) {
            estado.putDouble(ESTADO_LAT, centro.latitude);
            estado.putDouble(ESTADO_LNG, centro.longitude);
        }
    }

    private void mostrarUbicacion() {
        textoUbicacion.setText(centro == null ? R.string.grupo_elegir_centro : R.string.grupo_centro_elegido);
        actualizarBoton();
    }

    private void mostrarRadio() {
        textoRadio.setText(getString(R.string.grupo_metros, (int) radio.getValue()));
    }

    private void actualizarBoton() {
        btnCrear.setEnabled(centro != null && txtNombre.getText() != null && !txtNombre.getText().toString().trim().isEmpty());
    }

    private void crear() {
        Usuario yo = SesionManager.getUsuario();
        if (yo == null || centro == null) {
            return;
        }
        String nombre = txtNombre.getText().toString().trim();
        findViewById(R.id.progreso).setVisibility(View.VISIBLE);
        btnCrear.setEnabled(false);
        GrupoRepositorio.crear(yo, nombre, centro.latitude, centro.longitude, (int) radio.getValue())
                .addOnSuccessListener(id -> {
                    yo.setIdGrupo(id);
                    // El servicio de alertas pasa a escuchar el grupo nuevo
                    ServiciosSesion.iniciar(this);
                    finish();
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.progreso).setVisibility(View.GONE);
                    actualizarBoton();
                    Snackbar.make(findViewById(R.id.raiz), R.string.grupo_error_crear, Snackbar.LENGTH_LONG).show();
                });
    }
}
