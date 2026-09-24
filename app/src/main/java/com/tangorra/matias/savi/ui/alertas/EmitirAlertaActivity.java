package com.tangorra.matias.savi.ui.alertas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

import java.util.ArrayList;
import java.util.List;

/** Elegir el tipo de alerta y a quien avisar, y emitirla. */
public class EmitirAlertaActivity extends AppCompatActivity {

    private EmitirAlertaViewModel viewModel;

    private TextView comoSeAvisa;
    private ChipGroup destinos;
    private TextView avisoDestino;
    private MaterialButton btnEmitir;
    private final TiposAdapter tipos = new TiposAdapter();
    private List<Usuario> vecinos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emitir_alerta);

        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        comoSeAvisa = findViewById(R.id.como_se_avisa);
        destinos = findViewById(R.id.destinos);
        avisoDestino = findViewById(R.id.aviso_destino);
        btnEmitir = findViewById(R.id.btn_emitir);
        ((RecyclerView) findViewById(R.id.tipos)).setAdapter(tipos);

        ViewCompat.setOnApplyWindowInsetsListener(btnEmitir, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            int margen = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior);
            margenes.bottomMargin = margen + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(EmitirAlertaViewModel.class);
        viewModel.vecinos().observe(this, lista -> {
            vecinos = lista;
            armarDestinos();
        });
        viewModel.tipo().observe(this, tipo -> {
            tipos.seleccionar(tipo);
            comoSeAvisa.setText(PresentacionAlerta.comoSeAvisa(tipo));
            comoSeAvisa.setVisibility(tipo == null ? View.GONE : View.VISIBLE);
            armarDestinos();
        });
        viewModel.resultado().observe(this, this::mostrarResultado);

        btnEmitir.setOnClickListener(v -> confirmar());
    }

    private final ChipGroup.OnCheckedStateChangeListener alCambiarDestino = (grupo, ids) -> {
        if (ids.isEmpty()) {
            viewModel.limpiarDestino();
        } else {
            Object etiqueta = grupo.findViewById(ids.get(0)).getTag();
            viewModel.elegirDestino(etiqueta instanceof Usuario ? (Usuario) etiqueta : null);
        }
        actualizarBoton();
    };

    /** "Todo el grupo" solo se ofrece si el tipo de alerta lo permite. */
    private void armarDestinos() {
        String tipo = viewModel.tipo().getValue();
        boolean requiereCasa = tipo != null && PresentacionAlerta.requiereDestinatario(tipo);
        Usuario elegido = viewModel.destinatario();

        destinos.setOnCheckedStateChangeListener(null);
        destinos.removeAllViews();
        if (!requiereCasa) {
            Chip todos = nuevoChip(getString(R.string.emitir_todo_el_grupo), null);
            todos.setChipIconResource(R.drawable.ic_groups);
            todos.setChipIconVisible(true);
            destinos.addView(todos);
        }
        for (Usuario vecino : vecinos) {
            Chip chip = nuevoChip(vecino.getGlosaFormateada().trim(), vecino);
            destinos.addView(chip);
            if (elegido != null && elegido.getId().equals(vecino.getId())) {
                chip.setChecked(true);
            }
        }
        destinos.setOnCheckedStateChangeListener(alCambiarDestino);
        if (elegido == null) {
            viewModel.limpiarDestino();
        }
        avisoDestino.setVisibility(requiereCasa ? View.VISIBLE : View.GONE);
        actualizarBoton();
    }

    private Chip nuevoChip(String texto, Usuario vecino) {
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.chip_destino, destinos, false);
        chip.setId(View.generateViewId());
        chip.setText(texto);
        chip.setTag(vecino);
        return chip;
    }

    private void actualizarBoton() {
        btnEmitir.setEnabled(viewModel.listoParaEmitir());
    }

    private void confirmar() {
        Usuario destinatario = viewModel.destinatario();
        String donde = destinatario != null
                ? getString(R.string.emitir_en_casa_de, destinatario.getGlosaFormateada().trim())
                : getString(R.string.emitir_en_el_grupo);
        new MaterialAlertDialogBuilder(this)
                .setIcon(PresentacionAlerta.imagen(viewModel.tipo().getValue()))
                .setTitle(R.string.emitir_confirmar_titulo)
                .setMessage(getString(R.string.emitir_confirmar_texto, viewModel.tipo().getValue(), donde))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.inicio_emitir_alerta, (d, w) -> viewModel.emitir())
                .show();
    }

    private void mostrarResultado(EmitirAlertaViewModel.Resultado resultado) {
        btnEmitir.setEnabled(resultado != EmitirAlertaViewModel.Resultado.ENVIANDO && viewModel.listoParaEmitir());
        if (resultado == EmitirAlertaViewModel.Resultado.ENVIADA) {
            // Se abre el detalle para seguir las respuestas de los vecinos
            startActivity(DetalleAlertaActivity.intent(this, viewModel.idGrupo(), viewModel.idAlertaEmitida()));
            finish();
        } else if (resultado == EmitirAlertaViewModel.Resultado.ERROR) {
            Snackbar.make(findViewById(R.id.raiz), R.string.emitir_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private final class TiposAdapter extends RecyclerView.Adapter<TiposAdapter.Vista> {

        private String seleccionado;

        void seleccionar(String tipo) {
            seleccionado = tipo;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vista(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tipo_alerta, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Vista vista, int posicion) {
            final String tipo = PresentacionAlerta.TIPOS[posicion];
            vista.imagen.setImageResource(PresentacionAlerta.imagen(tipo));
            vista.nombre.setText(tipo);
            vista.tarjeta.setChecked(tipo.equals(seleccionado));
            vista.tarjeta.setOnClickListener(v -> viewModel.elegirTipo(tipo));
        }

        @Override
        public int getItemCount() {
            return PresentacionAlerta.TIPOS.length;
        }

        final class Vista extends RecyclerView.ViewHolder {
            final MaterialCardView tarjeta;
            final ImageView imagen;
            final TextView nombre;

            Vista(View itemView) {
                super(itemView);
                tarjeta = (MaterialCardView) itemView;
                imagen = itemView.findViewById(R.id.imagen);
                nombre = itemView.findViewById(R.id.nombre);
            }
        }
    }
}
