package com.tangorra.matias.savi.ui.grupo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.tangorra.matias.savi.Entidades.Grupo;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.ServiciosSesion;
import com.tangorra.matias.savi.ui.comun.Avatares;
import com.tangorra.matias.savi.ui.comun.QrDialogo;

import java.util.ArrayList;
import java.util.List;

/** Grupo vecinal actual: zona en el mapa, vecinos, invitar por QR y salir. */
public class GrupoActivity extends AppCompatActivity {

    private GrupoViewModel viewModel;
    private MaterialToolbar toolbar;
    private TextView zona;
    private TextView tituloVecinos;
    private GoogleMap mapa;
    private Grupo grupo;
    private final VecinosAdapter vecinos = new VecinosAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grupo);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_salir) {
                confirmarSalida();
                return true;
            }
            return false;
        });
        zona = findViewById(R.id.zona);
        tituloVecinos = findViewById(R.id.titulo_vecinos);
        ((RecyclerView) findViewById(R.id.vecinos)).setAdapter(vecinos);

        View invitar = findViewById(R.id.btn_invitar);
        invitar.setOnClickListener(v -> invitar());
        ViewCompat.setOnApplyWindowInsetsListener(invitar, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa)).getMapAsync(listo -> {
            mapa = listo;
            mapa.getUiSettings().setMapToolbarEnabled(false);
            mostrarZona();
        });

        viewModel = new ViewModelProvider(this).get(GrupoViewModel.class);
        viewModel.grupo().observe(this, g -> {
            if (g == null) {
                finish();
                return;
            }
            grupo = g;
            toolbar.setTitle(g.getNombre());
            mostrarZona();
        });
        viewModel.vecinos().observe(this, lista -> {
            tituloVecinos.setText(getResources().getQuantityString(R.plurals.grupo_vecinos, lista.size(), lista.size()));
            vecinos.mostrar(lista);
        });
        viewModel.salio().observe(this, evento -> {
            Boolean ok = evento.consumir();
            if (Boolean.TRUE.equals(ok)) {
                ServiciosSesion.iniciar(this);
                finish();
            } else if (Boolean.FALSE.equals(ok)) {
                Snackbar.make(findViewById(R.id.raiz), R.string.perfil_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarZona() {
        if (grupo == null) {
            return;
        }
        int radio = grupo.getMaxRango() != null ? grupo.getMaxRango() : 0;
        zona.setText(getString(R.string.grupo_zona_texto, radio));
        if (mapa == null || grupo.getLat() == null || grupo.getLng() == null) {
            return;
        }
        LatLng centro = new LatLng(grupo.getLat(), grupo.getLng());
        int color = ContextCompat.getColor(this, R.color.md_primary);
        mapa.clear();
        mapa.addMarker(new MarkerOptions().position(centro).title(grupo.getNombre()));
        if (radio > 0) {
            mapa.addCircle(new CircleOptions().center(centro).radius(radio).strokeWidth(4f)
                    .strokeColor(color).fillColor(ColorUtils.setAlphaComponent(color, 40)));
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, zoomPara(radio)));
        } else {
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, 15));
        }
    }

    /** Zoom aproximado para que entre el circulo en la tarjeta (cada nivel duplica la escala). */
    static float zoomPara(int radioMetros) {
        double zoom = 16 - Math.log(Math.max(radioMetros, 50) / 150d) / Math.log(2);
        return (float) Math.max(11, Math.min(17, zoom));
    }

    private void invitar() {
        if (grupo != null && grupo.getId() != null) {
            QrDialogo.mostrar(getSupportFragmentManager(), grupo.getNombre(), getString(R.string.grupo_qr_detalle), grupo.getId());
        }
    }

    private void confirmarSalida() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.grupo_salir)
                .setMessage(R.string.grupo_salir_texto)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.grupo_salir, (d, w) -> viewModel.salir())
                .show();
    }

    private final class VecinosAdapter extends RecyclerView.Adapter<VecinosAdapter.Vista> {

        private List<Usuario> items = new ArrayList<>();

        void mostrar(List<Usuario> nuevos) {
            items = nuevos;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vista(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vecino, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Vista vista, int posicion) {
            final Usuario vecino = items.get(posicion);
            Usuario yo = viewModel.yo().getValue();
            boolean soyYo = yo != null && vecino.getId().equals(yo.getId());
            vista.nombre.setText(vecino.getGlosaFormateada().trim());
            vista.detalle.setText(soyYo ? getString(R.string.grupo_vos) : vecino.getCelular());
            Avatares.cargar(vista.avatar, vecino.getId());
            boolean tieneCelular = !soyYo && vecino.getCelular() != null && !vecino.getCelular().trim().isEmpty();
            vista.llamar.setVisibility(tieneCelular ? View.VISIBLE : View.INVISIBLE);
            vista.llamar.setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + vecino.getCelular().trim()))));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class Vista extends RecyclerView.ViewHolder {
            final ImageView avatar;
            final TextView nombre;
            final TextView detalle;
            final View llamar;

            Vista(View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatar);
                nombre = itemView.findViewById(R.id.nombre);
                detalle = itemView.findViewById(R.id.detalle);
                llamar = itemView.findViewById(R.id.btn_llamar);
            }
        }
    }
}
