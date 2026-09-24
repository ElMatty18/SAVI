package com.tangorra.matias.savi.ui.informacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.tangorra.matias.savi.BuildConfig;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.ui.comun.PresentacionAlerta;

/** Que es SAVI, como avisa cada tipo de alerta, numeros de emergencia y creditos. */
public class InformacionActivity extends AppCompatActivity {

    private static final int[][] EMERGENCIAS = {
            {R.string.emergencia_policia, 911, R.drawable.ic_local_police},
            {R.string.emergencia_medica, 107, R.drawable.ic_emergency},
            {R.string.emergencia_bomberos, 100, R.drawable.ic_fire_truck},
            {R.string.emergencia_violencia, 144, R.drawable.ic_support_agent},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informacion);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.info_version, BuildConfig.VERSION_NAME));

        View scroll = findViewById(R.id.scroll);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), barras.bottom);
            return insets;
        });

        ViewGroup tipos = findViewById(R.id.tipos);
        for (String tipo : PresentacionAlerta.TIPOS) {
            View fila = LayoutInflater.from(this).inflate(R.layout.item_alerta_resumen, tipos, false);
            ((ImageView) fila.findViewById(R.id.imagen)).setImageResource(PresentacionAlerta.imagen(tipo));
            ((TextView) fila.findViewById(R.id.titulo)).setText(tipo);
            ((TextView) fila.findViewById(R.id.detalle)).setText(PresentacionAlerta.comoSeAvisa(tipo));
            fila.findViewById(R.id.estado).setVisibility(View.GONE);
            tipos.addView(fila);
        }

        ViewGroup emergencias = findViewById(R.id.emergencias);
        for (int[] e : EMERGENCIAS) {
            View fila = LayoutInflater.from(this).inflate(R.layout.item_vecino, emergencias, false);
            ImageView icono = fila.findViewById(R.id.avatar);
            icono.setImageResource(e[2]);
            icono.setImageTintList(android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(icono, R.attr.colorPrimary)));
            ((TextView) fila.findViewById(R.id.nombre)).setText(e[0]);
            ((TextView) fila.findViewById(R.id.detalle)).setText(String.valueOf(e[1]));
            String numero = String.valueOf(e[1]);
            View.OnClickListener llamar = v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero)));
            fila.setOnClickListener(llamar);
            fila.findViewById(R.id.btn_llamar).setOnClickListener(llamar);
            emergencias.addView(fila);
        }
    }
}
