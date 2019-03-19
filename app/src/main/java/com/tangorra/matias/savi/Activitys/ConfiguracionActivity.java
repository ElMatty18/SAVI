package com.tangorra.matias.savi.Activitys;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.tangorra.matias.savi.R;

public class ConfiguracionActivity extends AppCompatActivity {

    private Button volver;

    private Switch vacaciones;
    private Switch casa_sola;
    private Switch visitas_casa;
    private Switch ignorar_todo;
    private Switch no_molestar;

    private LinearLayout calendario;
    private LinearLayout hora_no_molestar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        vacaciones = findViewById(R.id.seleccVaciones);
        no_molestar = findViewById(R.id.seleccNoMolestar);

        calendario=findViewById(R.id.lay_fechas_vacaciones);
        calendario.setVisibility(View.INVISIBLE);

        hora_no_molestar= findViewById(R.id.lay_hora_no_molestar);
        hora_no_molestar.setVisibility(View.INVISIBLE);

        volver = findViewById(R.id.btnVolverMenuConfiguracion);


        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent menu = new Intent(ConfiguracionActivity.this, MenuPrincipalActivity.class);
            startActivity(menu);
            finish();

            }
        });

        vacaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendario.setVisibility(View.VISIBLE);
                hora_no_molestar.setVisibility(View.INVISIBLE);
            }
        });

        no_molestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendario.setVisibility(View.INVISIBLE);
                hora_no_molestar.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public  void onBackPressed(){
        Intent menu = new Intent(ConfiguracionActivity.this, MenuPrincipalActivity.class);
        startActivity(menu);
        finish();
    }
}
