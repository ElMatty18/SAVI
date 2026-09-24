package com.tangorra.matias.savi.ui.notificaciones;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

import java.util.ArrayList;
import java.util.List;

/** Avisos del barrio (cortes, reuniones, obras) que alcanzan los domicilios del usuario. */
public class NotificacionesActivity extends AppCompatActivity {

    private NotificacionesViewModel viewModel;
    private final Adaptador adaptador = new Adaptador();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificaciones);

        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        ((RecyclerView) findViewById(R.id.lista)).setAdapter(adaptador);
        View nueva = findViewById(R.id.btn_nueva);
        nueva.setOnClickListener(v -> startActivity(new Intent(this, NuevaNotificacionActivity.class)));
        ViewCompat.setOnApplyWindowInsetsListener(nueva, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(NotificacionesViewModel.class);
        viewModel.notificaciones().observe(this, lista -> {
            adaptador.mostrar(lista);
            findViewById(R.id.vacio).setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    /** "Tuya · hace 2 h" o "A 300 m de tu casa · ayer" */
    private String detalle(Notificacion n) {
        String donde;
        if (viewModel.yo() != null && viewModel.yo().getId().equals(n.getCreadoBy())) {
            donde = getString(R.string.notificaciones_tuya);
        } else {
            double metros = viewModel.distancia(n);
            donde = metros < 0 ? getString(R.string.notificaciones_cerca)
                    : metros < 1000 ? getString(R.string.notificaciones_a_metros, Math.round(metros / 10) * 10)
                    : getString(R.string.notificaciones_a_km, metros / 1000);
        }
        if (n.getCreacion() == null) {
            return donde;
        }
        return donde + " · " + PresentacionAlerta.hace(n.getCreacion().getTime(), System.currentTimeMillis());
    }

    private final class Adaptador extends RecyclerView.Adapter<Adaptador.Vista> {

        private List<Notificacion> items = new ArrayList<>();

        void mostrar(List<Notificacion> nuevos) {
            items = nuevos;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vista(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion_barrio, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Vista vista, int posicion) {
            Notificacion n = items.get(posicion);
            vista.titulo.setText(n.getTitle());
            vista.contenido.setText(n.getContenido());
            vista.contenido.setVisibility(n.getContenido() == null || n.getContenido().isEmpty() ? View.GONE : View.VISIBLE);
            vista.detalle.setText(detalle(n));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class Vista extends RecyclerView.ViewHolder {
            final TextView titulo;
            final TextView contenido;
            final TextView detalle;

            Vista(View itemView) {
                super(itemView);
                titulo = itemView.findViewById(R.id.titulo);
                contenido = itemView.findViewById(R.id.contenido);
                detalle = itemView.findViewById(R.id.detalle);
            }
        }
    }
}
