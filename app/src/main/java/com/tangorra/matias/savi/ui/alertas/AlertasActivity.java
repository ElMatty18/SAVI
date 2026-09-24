package com.tangorra.matias.savi.ui.alertas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.ui.comun.AlertasAdapter;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

/** Historial de alertas con filtros por estado. */
public class AlertasActivity extends AppCompatActivity {

    private static final String EXTRA_MODO = "modo";
    private static final String EXTRA_MIEMBRO = "miembro";

    private enum Modo { GRUPO, FAMILIA, MIEMBRO }

    public static Intent grupo(Context context) {
        return new Intent(context, AlertasActivity.class).putExtra(EXTRA_MODO, Modo.GRUPO.name());
    }

    public static Intent familia(Context context) {
        return new Intent(context, AlertasActivity.class).putExtra(EXTRA_MODO, Modo.FAMILIA.name());
    }

    /** Alertas que involucran a un familiar puntual. */
    public static Intent miembro(Context context, Usuario miembro) {
        return new Intent(context, AlertasActivity.class)
                .putExtra(EXTRA_MODO, Modo.MIEMBRO.name())
                .putExtra(EXTRA_MIEMBRO, miembro);
    }

    private TextView vacio;
    private View cargando;
    private String textoVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alertas);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        vacio = findViewById(R.id.vacio);
        cargando = findViewById(R.id.cargando);

        RecyclerView lista = findViewById(R.id.lista);
        ViewCompat.setOnApplyWindowInsetsListener(lista, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), barras.bottom);
            return insets;
        });
        AlertasAdapter adaptador = new AlertasAdapter(alerta ->
                startActivity(DetalleAlertaActivity.intent(this, alerta.getIdGrupo(), alerta.getId())));
        lista.setAdapter(adaptador);

        AlertasViewModel viewModel = new ViewModelProvider(this).get(AlertasViewModel.class);
        Usuario yo = SesionManager.getUsuario();
        Modo modo = Modo.valueOf(getIntent().getStringExtra(EXTRA_MODO) != null
                ? getIntent().getStringExtra(EXTRA_MODO) : Modo.GRUPO.name());
        textoVacio = getString(R.string.alertas_vacio);

        switch (modo) {
            case FAMILIA:
                toolbar.setTitle(R.string.alertas_familia_titulo);
                if (yo != null && yo.getIdFamilia() != null) {
                    viewModel.cargarFamilia(yo.getIdFamilia());
                } else {
                    sinDatos(getString(R.string.alertas_sin_familia));
                }
                break;
            case MIEMBRO:
                Usuario miembro = (Usuario) getIntent().getSerializableExtra(EXTRA_MIEMBRO);
                if (miembro == null) {
                    finish();
                    return;
                }
                toolbar.setTitle(getString(R.string.alertas_miembro_titulo, miembro.getGlosaFormateada().trim()));
                viewModel.cargarMiembro(miembro);
                break;
            default:
                toolbar.setTitle(R.string.alertas_grupo_titulo);
                if (yo != null && yo.getIdGrupo() != null) {
                    viewModel.cargarGrupo(yo.getIdGrupo());
                } else {
                    sinDatos(getString(R.string.alertas_sin_grupo));
                }
        }

        ChipGroup filtros = findViewById(R.id.filtros);
        filtros.setOnCheckedStateChangeListener((grupo, ids) -> {
            int id = ids.isEmpty() ? R.id.filtro_todas : ids.get(0);
            PresentacionAlerta.Estado estado = null;
            if (id == R.id.filtro_activas) {
                estado = PresentacionAlerta.Estado.ACTIVA;
            } else if (id == R.id.filtro_confirmadas) {
                estado = PresentacionAlerta.Estado.CONFIRMADA;
            } else if (id == R.id.filtro_cerradas) {
                estado = PresentacionAlerta.Estado.CERRADA;
            }
            textoVacio = getString(estado == null ? R.string.alertas_vacio : R.string.alertas_vacio_filtro);
            viewModel.filtrar(estado);
        });

        viewModel.alertas().observe(this, alertas -> {
            cargando.setVisibility(View.GONE);
            adaptador.submitList(alertas);
            vacio.setText(textoVacio);
            vacio.setVisibility(alertas.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void sinDatos(String mensaje) {
        cargando.setVisibility(View.GONE);
        vacio.setText(mensaje);
        vacio.setVisibility(View.VISIBLE);
        findViewById(R.id.filtros).setVisibility(View.GONE);
    }
}
