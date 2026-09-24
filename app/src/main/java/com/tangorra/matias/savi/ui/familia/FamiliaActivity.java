package com.tangorra.matias.savi.ui.familia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.ui.alertas.AlertasActivity;
import com.tangorra.matias.savi.ui.comun.Avatares;
import com.tangorra.matias.savi.ui.comun.QrDialogo;

import java.util.List;
import java.util.Objects;

/** Grupo familiar: vincular familiares por QR y acciones rapidas sobre cada uno. */
public class FamiliaActivity extends AppCompatActivity {

    private FamiliaViewModel viewModel;
    private MaterialToolbar toolbar;
    private View vacio;
    private final FamiliaresAdapter adaptador = new FamiliaresAdapter();

    private final ActivityResultLauncher<ScanOptions> escaner = registerForActivityResult(new ScanContract(), resultado -> {
        if (resultado.getContents() != null) {
            viewModel.vincular(resultado.getContents());
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_familia);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_mi_qr) {
                mostrarMiQr();
                return true;
            } else if (item.getItemId() == R.id.accion_salir) {
                confirmarSalida();
                return true;
            }
            return false;
        });

        vacio = findViewById(R.id.vacio);
        RecyclerView lista = findViewById(R.id.lista);
        lista.setAdapter(adaptador);
        ExtendedFloatingActionButton agregar = findViewById(R.id.btn_agregar);
        agregar.setOnClickListener(v -> escanear());
        findViewById(R.id.btn_mi_qr).setOnClickListener(v -> mostrarMiQr());

        ViewCompat.setOnApplyWindowInsetsListener(agregar, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });
        // El boton se achica al bajar la lista para no tapar la ultima tarjeta
        lista.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0) {
                    agregar.shrink();
                } else if (dy < 0) {
                    agregar.extend();
                }
            }
        });

        viewModel = new ViewModelProvider(this).get(FamiliaViewModel.class);
        viewModel.familiares().observe(this, this::mostrar);
        viewModel.yo().observe(this, yo ->
                toolbar.getMenu().findItem(R.id.accion_salir).setVisible(yo != null && yo.getIdFamilia() != null));
        viewModel.trabajando().observe(this, t ->
                findViewById(R.id.progreso).setVisibility(t ? View.VISIBLE : View.GONE));
        viewModel.mensajes().observe(this, evento -> {
            String mensaje = evento.consumir();
            if (mensaje != null) {
                Snackbar.make(findViewById(R.id.raiz), mensaje, Snackbar.LENGTH_LONG).setAnchorView(agregar).show();
            }
        });
    }

    private void mostrar(List<Usuario> familiares) {
        adaptador.submitList(familiares);
        vacio.setVisibility(familiares.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void escanear() {
        ScanOptions opciones = new ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(getString(R.string.escanear_familiar))
                .setBeepEnabled(false)
                .setOrientationLocked(false);
        escaner.launch(opciones);
    }

    private void mostrarMiQr() {
        Usuario yo = viewModel.yo().getValue();
        if (yo != null) {
            QrDialogo.mostrar(getSupportFragmentManager(), getString(R.string.qr_mio_titulo),
                    getString(R.string.qr_mio_detalle), yo.getId());
        }
    }

    private void confirmarSalida() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.familia_salir)
                .setMessage(R.string.familia_salir_texto)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.familia_salir, (d, w) -> viewModel.salir())
                .show();
    }

    private void confirmarQuitar(Usuario familiar) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.familia_quitar_titulo, familiar.getGlosaFormateada().trim()))
                .setMessage(R.string.familia_quitar_texto)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.familia_quitar, (d, w) -> viewModel.quitar(familiar))
                .show();
    }

    private void llamar(Usuario familiar) {
        if (familiar.getCelular() == null || familiar.getCelular().trim().isEmpty()) {
            Snackbar.make(findViewById(R.id.raiz), R.string.familia_sin_telefono, Snackbar.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + familiar.getCelular().trim())));
    }

    /** Abre la casa del familiar en la app de mapas. */
    private void verCasa(Usuario familiar) {
        Domicilio casa = familiar.getPerfil() != null ? familiar.getPerfil().getDomicilio() : null;
        if (casa == null || (casa.getLat() == 0 && casa.getLng() == 0)) {
            Snackbar.make(findViewById(R.id.raiz), R.string.familia_sin_domicilio, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String etiqueta = Uri.encode("Casa de " + familiar.getGlosaFormateada().trim());
        Uri geo = Uri.parse("geo:0,0?q=" + casa.getLat() + "," + casa.getLng() + "(" + etiqueta + ")");
        startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, geo), null));
    }

    private final class FamiliaresAdapter extends ListAdapter<Usuario, FamiliaresAdapter.Vista> {

        FamiliaresAdapter() {
            super(new DiffUtil.ItemCallback<Usuario>() {
                @Override
                public boolean areItemsTheSame(@NonNull Usuario a, @NonNull Usuario b) {
                    return Objects.equals(a.getId(), b.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Usuario a, @NonNull Usuario b) {
                    return Objects.equals(a.getGlosa(), b.getGlosa()) && Objects.equals(a.getCelular(), b.getCelular());
                }
            });
        }

        @NonNull
        @Override
        public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vista(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_familiar, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Vista vista, int posicion) {
            final Usuario familiar = getItem(posicion);
            vista.nombre.setText(familiar.getGlosaFormateada().trim());
            vista.detalle.setText(familiar.getCelular() != null && !familiar.getCelular().isEmpty()
                    ? familiar.getCelular() : familiar.getMail());
            Avatares.cargar(vista.avatar, familiar.getId());
            vista.itemView.findViewById(R.id.btn_llamar).setOnClickListener(v -> llamar(familiar));
            vista.itemView.findViewById(R.id.btn_alertas).setOnClickListener(v ->
                    startActivity(AlertasActivity.miembro(FamiliaActivity.this, familiar)));
            vista.itemView.findViewById(R.id.btn_casa).setOnClickListener(v -> verCasa(familiar));
            vista.itemView.findViewById(R.id.btn_quitar).setOnClickListener(v -> confirmarQuitar(familiar));
        }

        final class Vista extends RecyclerView.ViewHolder {
            final ImageView avatar;
            final TextView nombre;
            final TextView detalle;

            Vista(View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatar);
                nombre = itemView.findViewById(R.id.nombre);
                detalle = itemView.findViewById(R.id.detalle);
            }
        }
    }
}
