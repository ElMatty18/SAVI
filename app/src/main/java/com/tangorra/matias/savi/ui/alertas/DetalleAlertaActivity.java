package com.tangorra.matias.savi.ui.alertas;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Service.PoliticaAlertas;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Detalle en vivo de una alerta: estado, nivel, respuestas de los vecinos y acciones. */
public class DetalleAlertaActivity extends AppCompatActivity {

    private static final String EXTRA_GRUPO = "idGrupo";
    private static final String EXTRA_ALERTA = "idAlerta";
    private static final String EXTRA_URGENTE = "urgente";

    public static Intent intent(Context context, String idGrupo, String idAlerta) {
        return new Intent(context, DetalleAlertaActivity.class)
                .putExtra(EXTRA_GRUPO, idGrupo)
                .putExtra(EXTRA_ALERTA, idAlerta);
    }

    /** Para abrir desde una notificacion urgente: se muestra aunque el telefono este bloqueado. */
    public static Intent intentUrgente(Context context, String idGrupo, String idAlerta) {
        return intent(context, idGrupo, idAlerta).putExtra(EXTRA_URGENTE, true);
    }

    private DetalleAlertaViewModel viewModel;

    private ImageView imagen;
    private TextView tipo;
    private TextView estado;
    private TextView destino;
    private TextView creador;
    private TextView cuando;
    private LinearProgressIndicator nivel;
    private TextView nivelTexto;
    private TextView nivelDetalle;
    private View seccionResponder;
    private TextView pregunta;
    private View btnCerrar;
    private View seccionAutoridades;
    private TextView sinRespuestas;
    private LinearProgressIndicator progreso;
    private final RespuestasAdapter respuestas = new RespuestasAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getIntent().getBooleanExtra(EXTRA_URGENTE, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        setContentView(R.layout.activity_detalle_alerta);

        String idGrupo = getIntent().getStringExtra(EXTRA_GRUPO);
        String idAlerta = getIntent().getStringExtra(EXTRA_ALERTA);
        if (idGrupo == null || idAlerta == null) {
            finish();
            return;
        }

        vincularVistas();

        viewModel = new ViewModelProvider(this).get(DetalleAlertaViewModel.class);
        viewModel.cargar(idGrupo, idAlerta);
        viewModel.alerta().observe(this, this::mostrar);
        viewModel.enviando().observe(this, enviando -> progreso.setVisibility(enviando ? View.VISIBLE : View.GONE));
        viewModel.error().observe(this, mensaje -> {
            if (mensaje != null) {
                Snackbar.make(findViewById(R.id.raiz), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void vincularVistas() {
        ((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.toolbar))
                .setNavigationOnClickListener(v -> finish());
        View scroll = findViewById(R.id.scroll);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), barras.bottom);
            return insets;
        });

        imagen = findViewById(R.id.imagen);
        tipo = findViewById(R.id.tipo);
        estado = findViewById(R.id.estado);
        destino = findViewById(R.id.destino);
        creador = findViewById(R.id.creador);
        cuando = findViewById(R.id.cuando);
        nivel = findViewById(R.id.nivel);
        nivelTexto = findViewById(R.id.nivel_texto);
        nivelDetalle = findViewById(R.id.nivel_detalle);
        seccionResponder = findViewById(R.id.seccion_responder);
        pregunta = findViewById(R.id.pregunta);
        btnCerrar = findViewById(R.id.btn_cerrar);
        seccionAutoridades = findViewById(R.id.seccion_autoridades);
        sinRespuestas = findViewById(R.id.sin_respuestas);
        progreso = findViewById(R.id.progreso);
        ((RecyclerView) findViewById(R.id.lista_respuestas)).setAdapter(respuestas);

        findViewById(R.id.btn_confirmar).setOnClickListener(v -> viewModel.responder(StringUtils.respuesta_confirma));
        findViewById(R.id.btn_falsa).setOnClickListener(v -> viewModel.responder(StringUtils.respuesta_cancela));
        btnCerrar.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.detalle_cerrar_titulo)
                .setMessage(R.string.detalle_cerrar_texto)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.detalle_cerrar, (d, w) -> viewModel.cerrar())
                .show());

        findViewById(R.id.btn_policia).setOnClickListener(v -> llamar("911"));
        findViewById(R.id.btn_emergencias).setOnClickListener(v -> llamar("107"));
        findViewById(R.id.btn_bomberos).setOnClickListener(v -> llamar("100"));
        findViewById(R.id.btn_violencia).setOnClickListener(v -> llamar("144"));
    }

    private void mostrar(Alerta alerta) {
        if (alerta == null) {
            Snackbar.make(findViewById(R.id.raiz), R.string.detalle_no_existe, Snackbar.LENGTH_INDEFINITE).show();
            return;
        }
        Usuario usuario = viewModel.usuario();

        imagen.setImageResource(PresentacionAlerta.imagen(alerta.getAlarma()));
        tipo.setText(alerta.getAlarma());
        PresentacionAlerta.Estado e = PresentacionAlerta.estado(alerta);
        estado.setText(e.etiqueta);
        estado.setTextColor(ContextCompat.getColor(this, e.color));
        estado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, e.contenedor)));

        String detalle = PresentacionAlerta.detalle(this, alerta);
        int separador = detalle.indexOf(" · ");
        destino.setText(separador > 0 ? detalle.substring(0, separador) : detalle);
        cuando.setText(separador > 0 ? detalle.substring(separador + 3) : "");
        cuando.setVisibility(separador > 0 ? View.VISIBLE : View.GONE);
        creador.setText(getString(R.string.detalle_emitida_por,
                alerta.getCreadoBy() != null ? StringUtils.getTextoFormateado(alerta.getCreadoBy()).trim() : "-"));

        mostrarNivel(alerta);

        boolean puedeResponder = PoliticaAlertas.puedeResponder(alerta, usuario);
        seccionResponder.setVisibility(puedeResponder ? View.VISIBLE : View.GONE);
        pregunta.setText(usuario != null && PoliticaAlertas.esDirigidaA(alerta, usuario)
                ? R.string.detalle_pregunta_destinatario : R.string.detalle_pregunta);
        btnCerrar.setVisibility(PoliticaAlertas.puedeCerrar(alerta, usuario) ? View.VISIBLE : View.GONE);
        seccionAutoridades.setVisibility(PoliticaAlertas.sugerirAutoridades(alerta)
                && e != PresentacionAlerta.Estado.CERRADA ? View.VISIBLE : View.GONE);

        List<RespuestaAlerta> lista = new ArrayList<>();
        for (RespuestaAlerta r : alerta.listaRespuestas()) {
            if (r != null) {
                lista.add(r);
            }
        }
        Collections.sort(lista, (a, b) -> Long.compare(tiempo(a), tiempo(b)));
        respuestas.mostrar(lista);
        sinRespuestas.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarNivel(Alerta alerta) {
        int valor = alerta.obtenerNivelAlerta();
        nivel.setProgressCompat(Math.max(valor, 2), true);
        int color;
        if (valor >= 67) {
            nivelTexto.setText(R.string.detalle_nivel_alto);
            color = MaterialColors.getColor(nivel, R.attr.colorError);
        } else if (valor >= 34) {
            nivelTexto.setText(R.string.detalle_nivel_medio);
            color = ContextCompat.getColor(this, R.color.alerta_atencion);
        } else {
            nivelTexto.setText(R.string.detalle_nivel_bajo);
            color = MaterialColors.getColor(nivel, R.attr.colorPrimary);
        }
        nivel.setIndicatorColor(color);
        nivelTexto.setTextColor(color);

        int total = 0;
        int confirmaron = 0;
        for (RespuestaAlerta r : alerta.listaRespuestas()) {
            if (r == null) {
                continue;
            }
            total++;
            if (r.getRespuestaAutomatica() == null && StringUtils.respuesta_confirma.equals(r.getRespuesta())) {
                confirmaron++;
            }
        }
        nivelDetalle.setText(getResources().getQuantityString(R.plurals.detalle_confirmaron, total, confirmaron, total));
        nivelDetalle.setVisibility(total == 0 ? View.GONE : View.VISIBLE);
    }

    private void llamar(String numero) {
        // ACTION_DIAL abre el marcador con el numero cargado; no necesita el permiso CALL_PHONE
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero)));
    }

    private static long tiempo(RespuestaAlerta r) {
        return r.getCreacion() != null ? r.getCreacion().getTime() : 0;
    }

    private static final class RespuestasAdapter extends RecyclerView.Adapter<RespuestasAdapter.Vista> {

        private List<RespuestaAlerta> items = new ArrayList<>();

        void mostrar(List<RespuestaAlerta> nuevos) {
            items = nuevos;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vista(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_respuesta, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Vista vista, int posicion) {
            RespuestaAlerta r = items.get(posicion);
            Context context = vista.itemView.getContext();
            vista.nombre.setText(StringUtils.getTextoFormateado(r.getGlosa()).trim());
            if (r.getRespuestaAutomatica() != null) {
                vista.respuesta.setText(context.getString(R.string.detalle_respuesta_automatica, r.getRespuestaAutomatica()));
                vista.icono.setImageResource(R.drawable.ic_schedule);
            } else if (StringUtils.respuesta_confirma.equals(r.getRespuesta())) {
                vista.respuesta.setText(R.string.detalle_respuesta_confirma);
                vista.icono.setImageResource(R.drawable.ic_check_circle);
            } else {
                vista.respuesta.setText(R.string.detalle_respuesta_cancela);
                vista.icono.setImageResource(R.drawable.ic_cancel);
            }
            vista.cuando.setText(r.getCreacion() != null
                    ? PresentacionAlerta.hace(r.getCreacion().getTime(), System.currentTimeMillis()) : "");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Vista extends RecyclerView.ViewHolder {
            final ImageView icono;
            final TextView nombre;
            final TextView respuesta;
            final TextView cuando;

            Vista(View itemView) {
                super(itemView);
                icono = itemView.findViewById(R.id.icono);
                nombre = itemView.findViewById(R.id.nombre);
                respuesta = itemView.findViewById(R.id.respuesta);
                cuando = itemView.findViewById(R.id.cuando);
            }
        }
    }
}
