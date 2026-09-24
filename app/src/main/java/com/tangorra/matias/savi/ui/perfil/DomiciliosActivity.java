package com.tangorra.matias.savi.ui.perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.PerfilUsuario;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.data.Sesion;
import com.tangorra.matias.savi.data.UsuarioRepositorio;
import com.tangorra.matias.savi.ui.comun.SelectorUbicacionActivity;

/** Casa principal y domicilio alternativo, elegidos en el mapa y guardados al confirmar. */
public class DomiciliosActivity extends AppCompatActivity {

    private Usuario usuario;
    private GoogleMap mapa;
    private boolean eligiendoPrincipal;

    private final ActivityResultLauncher<SelectorUbicacionActivity.Pedido> selector = registerForActivityResult(
            new SelectorUbicacionActivity.Contrato(), elegido -> {
                if (elegido != null) {
                    guardar(eligiendoPrincipal, new Domicilio(elegido.latitude, elegido.longitude));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_domicilios);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        configurarFila(findViewById(R.id.principal), true);
        configurarFila(findViewById(R.id.alternativo), false);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa)).getMapAsync(listo -> {
            mapa = listo;
            mapa.getUiSettings().setMapToolbarEnabled(false);
            mostrar();
        });

        // En vivo: refleja lo guardado apenas llega a la base
        Sesion.usuario().observe(this, u -> {
            if (u != null) {
                usuario = u;
                mostrar();
            }
        });
    }

    private void configurarFila(View fila, boolean principal) {
        ((TextView) fila.findViewById(R.id.titulo)).setText(principal ? R.string.domicilios_principal : R.string.domicilios_alternativo);
        ((ImageView) fila.findViewById(R.id.icono)).setImageResource(principal ? R.drawable.ic_home_pin : R.drawable.ic_location_on);
        fila.findViewById(R.id.btn_elegir).setOnClickListener(v -> elegir(principal));
        fila.findViewById(R.id.btn_quitar).setOnClickListener(v -> guardar(principal, null));
    }

    private void elegir(boolean principal) {
        eligiendoPrincipal = principal;
        Domicilio actual = domicilio(principal);
        LatLng inicial = Domicilio.estaCargado(actual) ? new LatLng(actual.getLat(), actual.getLng()) : null;
        selector.launch(new SelectorUbicacionActivity.Pedido(
                getString(principal ? R.string.domicilios_principal : R.string.domicilios_alternativo), inicial, 0));
    }

    private void guardar(boolean principal, Domicilio domicilio) {
        if (usuario == null) {
            return;
        }
        findViewById(R.id.progreso).setVisibility(View.VISIBLE);
        UsuarioRepositorio.guardarDomicilio(usuario.getId(), principal, domicilio).addOnCompleteListener(t -> {
            findViewById(R.id.progreso).setVisibility(View.GONE);
            if (!t.isSuccessful()) {
                Snackbar.make(findViewById(R.id.raiz), R.string.domicilios_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private Domicilio domicilio(boolean principal) {
        PerfilUsuario perfil = usuario != null ? usuario.getPerfil() : null;
        if (perfil == null) {
            return null;
        }
        return principal ? perfil.getDomicilio() : perfil.getDomicilioAlterno();
    }

    private void mostrar() {
        if (usuario == null) {
            return;
        }
        mostrarFila(findViewById(R.id.principal), true);
        mostrarFila(findViewById(R.id.alternativo), false);
        if (mapa == null) {
            return;
        }
        mapa.clear();
        LatLngBounds.Builder limites = new LatLngBounds.Builder();
        int puntos = 0;
        for (boolean principal : new boolean[]{true, false}) {
            Domicilio d = domicilio(principal);
            if (Domicilio.estaCargado(d)) {
                LatLng punto = new LatLng(d.getLat(), d.getLng());
                mapa.addMarker(new MarkerOptions().position(punto)
                        .title(getString(principal ? R.string.domicilios_principal : R.string.domicilios_alternativo))
                        .icon(BitmapDescriptorFactory.defaultMarker(principal
                                ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_AZURE)));
                limites.include(punto);
                puntos++;
            }
        }
        if (puntos == 1) {
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(limites.build().getCenter(), 15));
        } else if (puntos == 2) {
            View contenedor = getSupportFragmentManager().findFragmentById(R.id.mapa).getView();
            int ancho = contenedor != null && contenedor.getWidth() > 0 ? contenedor.getWidth() : 800;
            int alto = contenedor != null && contenedor.getHeight() > 0 ? contenedor.getHeight() : 400;
            mapa.moveCamera(CameraUpdateFactory.newLatLngBounds(limites.build(), ancho, alto, 120));
        }
    }

    private void mostrarFila(View fila, boolean principal) {
        boolean cargado = Domicilio.estaCargado(domicilio(principal));
        TextView estado = fila.findViewById(R.id.estado);
        if (cargado) {
            estado.setText(R.string.domicilios_cargado);
        } else {
            estado.setText(principal ? R.string.domicilios_sin_cargar : R.string.domicilios_alternativo_ayuda);
        }
        ((MaterialButton) fila.findViewById(R.id.btn_elegir)).setText(cargado ? R.string.domicilios_cambiar : R.string.domicilios_elegir);
        // La casa principal no se quita: se usa para avisos y para que la familia la ubique
        fila.findViewById(R.id.btn_quitar).setVisibility(cargado && !principal ? View.VISIBLE : View.GONE);
    }
}
