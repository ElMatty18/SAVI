package com.tangorra.matias.savi.ui.notificaciones;

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
import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.data.NotificacionRepositorio;
import com.tangorra.matias.savi.ui.comun.SelectorUbicacionActivity;

/** Publicar un aviso informativo para los vecinos dentro de un radio. */
public class NuevaNotificacionActivity extends AppCompatActivity {

    private TextInputEditText txtTitulo;
    private TextInputEditText txtMensaje;
    private TextView textoUbicacion;
    private TextView textoRadio;
    private Slider radio;
    private MaterialButton btnPublicar;
    private LatLng lugar;

    private final ActivityResultLauncher<SelectorUbicacionActivity.Pedido> selector = registerForActivityResult(
            new SelectorUbicacionActivity.Contrato(), elegido -> {
                if (elegido != null) {
                    lugar = elegido;
                    textoUbicacion.setText(R.string.notificaciones_lugar_elegido);
                    actualizarBoton();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nueva_notificacion);

        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        txtTitulo = findViewById(R.id.txt_titulo);
        txtMensaje = findViewById(R.id.txt_mensaje);
        textoUbicacion = findViewById(R.id.texto_ubicacion);
        textoRadio = findViewById(R.id.texto_radio);
        radio = findViewById(R.id.radio);
        btnPublicar = findViewById(R.id.btn_publicar);

        radio.setLabelFormatter(this::formatoRadio);
        radio.addOnChangeListener((s, valor, delUsuario) -> textoRadio.setText(formatoRadio(valor)));
        textoRadio.setText(formatoRadio(radio.getValue()));

        findViewById(R.id.tarjeta_ubicacion).setOnClickListener(v -> selector.launch(new SelectorUbicacionActivity.Pedido(
                getString(R.string.notificaciones_elegir_lugar), lugar != null ? lugar : miCasa(), (int) radio.getValue())));
        txtTitulo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { actualizarBoton(); }
        });
        btnPublicar.setOnClickListener(v -> publicar());

        ViewCompat.setOnApplyWindowInsetsListener(btnPublicar, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });
    }

    private String formatoRadio(float metros) {
        return metros < 1000 ? getString(R.string.grupo_metros, (int) metros)
                : String.format(java.util.Locale.getDefault(), "%.1f km", metros / 1000);
    }

    /** Punto de partida del mapa: la casa del usuario, si la tiene cargada. */
    private LatLng miCasa() {
        Usuario yo = SesionManager.getUsuario();
        Domicilio casa = yo != null && yo.getPerfil() != null ? yo.getPerfil().getDomicilio() : null;
        return casa != null && (casa.getLat() != 0 || casa.getLng() != 0) ? new LatLng(casa.getLat(), casa.getLng()) : null;
    }

    private void actualizarBoton() {
        btnPublicar.setEnabled(lugar != null && txtTitulo.getText() != null && !txtTitulo.getText().toString().trim().isEmpty());
    }

    private void publicar() {
        Usuario yo = SesionManager.getUsuario();
        if (yo == null || lugar == null) {
            return;
        }
        findViewById(R.id.progreso).setVisibility(View.VISIBLE);
        btnPublicar.setEnabled(false);
        String mensaje = txtMensaje.getText() != null ? txtMensaje.getText().toString().trim() : "";
        NotificacionRepositorio.crear(yo, txtTitulo.getText().toString().trim(), mensaje,
                        lugar.latitude, lugar.longitude, (int) radio.getValue())
                .addOnSuccessListener(v -> finish())
                .addOnFailureListener(e -> {
                    findViewById(R.id.progreso).setVisibility(View.GONE);
                    actualizarBoton();
                    Snackbar.make(findViewById(R.id.raiz), R.string.notificaciones_error, Snackbar.LENGTH_LONG).show();
                });
    }
}
