package com.tangorra.matias.savi.ui.comun;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.tangorra.matias.savi.R;

/**
 * Pantalla para elegir un punto en el mapa (grupo vecinal, notificacion, domicilio).
 * Se usa con {@link Contrato}: devuelve el centro del mapa al confirmar.
 */
public class SelectorUbicacionActivity extends AppCompatActivity {

    private static final String EXTRA_TITULO = "titulo";
    private static final String EXTRA_LAT = "lat";
    private static final String EXTRA_LNG = "lng";
    private static final String EXTRA_RADIO = "radio";

    // Obelisco, Buenos Aires: se usa si no hay una ubicacion inicial ni permiso de ubicacion
    private static final LatLng POR_DEFECTO = new LatLng(-34.6037, -58.3816);
    private static final float ZOOM = 16f;

    /** Pedido al selector. radioMetros > 0 dibuja el area alrededor del punto. */
    public static final class Pedido {
        final String titulo;
        final LatLng inicial;
        final int radioMetros;

        public Pedido(String titulo, @Nullable LatLng inicial, int radioMetros) {
            this.titulo = titulo;
            this.inicial = inicial;
            this.radioMetros = radioMetros;
        }
    }

    public static final class Contrato extends ActivityResultContract<Pedido, LatLng> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Pedido pedido) {
            Intent intent = new Intent(context, SelectorUbicacionActivity.class)
                    .putExtra(EXTRA_TITULO, pedido.titulo)
                    .putExtra(EXTRA_RADIO, pedido.radioMetros);
            if (pedido.inicial != null) {
                intent.putExtra(EXTRA_LAT, pedido.inicial.latitude).putExtra(EXTRA_LNG, pedido.inicial.longitude);
            }
            return intent;
        }

        @Override
        public LatLng parseResult(int codigo, @Nullable Intent datos) {
            if (codigo != Activity.RESULT_OK || datos == null) {
                return null;
            }
            return new LatLng(datos.getDoubleExtra(EXTRA_LAT, 0), datos.getDoubleExtra(EXTRA_LNG, 0));
        }
    }

    private GoogleMap mapa;
    private Circle area;
    private int radioMetros;

    private final ActivityResultLauncher<String[]> permisos = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                if (tienePermiso()) {
                    irAMiUbicacion();
                } else {
                    Snackbar.make(findViewById(R.id.raiz), R.string.ubicacion_sin_permiso, Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selector_ubicacion);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra(EXTRA_TITULO));
        toolbar.setNavigationOnClickListener(v -> finish());
        radioMetros = getIntent().getIntExtra(EXTRA_RADIO, 0);

        View panel = findViewById(R.id.panel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, barras.top, 0, 0);
            toolbar.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.alto_toolbar) + barras.top;
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) panel.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            panel.setLayoutParams(margenes);
            return insets;
        });

        findViewById(R.id.btn_mi_ubicacion).setOnClickListener(v -> {
            if (tienePermiso()) {
                irAMiUbicacion();
            } else {
                permisos.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });
        findViewById(R.id.btn_confirmar).setOnClickListener(v -> confirmar());

        SupportMapFragment fragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        fragmento.getMapAsync(this::alListo);
    }

    @SuppressLint("MissingPermission")
    private void alListo(GoogleMap listo) {
        mapa = listo;
        mapa.getUiSettings().setMyLocationButtonEnabled(false);
        mapa.getUiSettings().setMapToolbarEnabled(false);
        int alto = getResources().getDimensionPixelSize(R.dimen.alto_toolbar);
        mapa.setPadding(0, alto, 0, alto * 3);

        boolean hayInicial = getIntent().hasExtra(EXTRA_LAT);
        LatLng inicial = hayInicial
                ? new LatLng(getIntent().getDoubleExtra(EXTRA_LAT, 0), getIntent().getDoubleExtra(EXTRA_LNG, 0))
                : POR_DEFECTO;
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(inicial, ZOOM));
        if (tienePermiso()) {
            mapa.setMyLocationEnabled(true);
            if (!hayInicial) {
                irAMiUbicacion();
            }
        }

        if (radioMetros > 0) {
            int color = ContextCompat.getColor(this, R.color.md_primary);
            area = mapa.addCircle(new CircleOptions()
                    .center(inicial)
                    .radius(radioMetros)
                    .strokeWidth(4f)
                    .strokeColor(color)
                    .fillColor(ColorUtils.setAlphaComponent(color, 40)));
            mapa.setOnCameraMoveListener(() -> area.setCenter(mapa.getCameraPosition().target));
        }
    }

    @SuppressLint("MissingPermission")
    private void irAMiUbicacion() {
        if (mapa == null) {
            return;
        }
        mapa.setMyLocationEnabled(true);
        LocationServices.getFusedLocationProviderClient(this)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(ubicacion -> {
                    if (ubicacion != null) {
                        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude()), ZOOM));
                    } else {
                        Snackbar.make(findViewById(R.id.raiz), R.string.ubicacion_no_disponible, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private boolean tienePermiso() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void confirmar() {
        if (mapa == null) {
            return;
        }
        LatLng centro = mapa.getCameraPosition().target;
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_LAT, centro.latitude).putExtra(EXTRA_LNG, centro.longitude));
        finish();
    }
}
