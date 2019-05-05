package com.tangorra.matias.savi.Activitys;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.Calendar;
import java.util.Date;

public class ConfiguracionActivity extends AppCompatActivity {

    private Switch vacaciones;
    private Switch casaSola;
    private Switch visitasCasa;
    private Switch noMolestar;
    private Switch ignorarTodo;

    private LinearLayout seleccVacionesFechas;
    private LinearLayout seleccCasaSolaFecha;
    private LinearLayout seleccNoMolestarHora;
    private LinearLayout seleccVisitasHogar;
    private LinearLayout seleccIgnorar;

    private TextView mDisplayDateVacacionesInicio;
    private DatePickerDialog.OnDateSetListener mDateSetListenerVacionesInicio;
    private Date fechaInicioVacaciones;

    private TextView mDisplayDateVacacionesFin;
    private DatePickerDialog.OnDateSetListener mDateSetListenerVacionesFin;
    private Date fechaFinVacaciones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;
        getWindow().setLayout((int )(width*.9),(int )(heigth*.8) );
        getSupportActionBar().hide();


        asociarElementos();






    }

    private void asociarElementos() {
        vacaciones = findViewById(R.id.seleccVaciones);
        casaSola = findViewById(R.id.seleccCasaSola);
        visitasCasa = findViewById(R.id.seleccVisitasCasa);
        noMolestar = findViewById(R.id.seleccNoMolestar);
        ignorarTodo = findViewById(R.id.seleccIgnorarTodo);


        seleccVacionesFechas = findViewById(R.id.seleccVacionesFechas);
        seleccCasaSolaFecha = findViewById(R.id.seleccCasaSolaFecha);
        seleccNoMolestarHora = findViewById(R.id.seleccNoMolestarHora);
        seleccVisitasHogar = findViewById(R.id.seleccVisitasHogar);
        seleccIgnorar = findViewById(R.id.seleccIgnorar);

        desactivarOpciones();

        mDisplayDateVacacionesInicio = findViewById(R.id.configuracion_inicio_vacaciones);
        mDisplayDateVacacionesFin = findViewById(R.id.configuracion_fin_vacaciones);

        fechaPicker();

        vacaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vacaciones.isChecked()){
                    vacaciones.setChecked(false);
                } else{
                    disabledAllMinuss(StringUtils.config_vacaciones);

                }
            }
        });

        casaSola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!casaSola.isChecked()){
                    casaSola.setChecked(false);
                } else{
                    disabledAllMinuss(StringUtils.config_casaSola);
                }
            }
        });

        visitasCasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!visitasCasa.isChecked()){
                    visitasCasa.setChecked(false);
                } else{
                    disabledAllMinuss(StringUtils.config_visitasCasa);
                }
            }
        });

        noMolestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!noMolestar.isChecked()){
                    noMolestar.setChecked(false);
                } else{
                    disabledAllMinuss(StringUtils.config_noMolestar);
                }
            }
        });

        ignorarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ignorarTodo.isChecked()){
                    ignorarTodo.setChecked(false);
                } else{
                    disabledAllMinuss(StringUtils.config_ignorarTodo);
                }
            }
        });

    }

    private void fechaPicker() {
        mDisplayDateVacacionesInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ConfiguracionActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerVacionesInicio,
                        year, month, day
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerVacionesInicio = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month ++;
                String date = dayOfMonth + "/" + month + "/" + year;
                mDisplayDateVacacionesInicio.setText(date);
                fechaInicioVacaciones = new Date(year, month,dayOfMonth);
            }
        };


        mDisplayDateVacacionesFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ConfiguracionActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerVacionesFin,
                        year, month, day
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerVacionesFin = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month ++;
                String date = dayOfMonth + "/" + month + "/" + year;
                mDisplayDateVacacionesFin.setText(date);
                fechaFinVacaciones = new Date(year, month,dayOfMonth);
            }
        };
    }

    private void disabledAllMinuss(String id){
       if (id.equals(StringUtils.config_vacaciones)){
           desactivarOpciones();
           seleccVacionesFechas.setVisibility(LinearLayout.VISIBLE);

           vacaciones.setChecked(true);
           casaSola.setChecked(false);
           visitasCasa.setChecked(false);
           noMolestar.setChecked(false);
           ignorarTodo.setChecked(false);
       } else if (id.equals(StringUtils.config_casaSola)){
           desactivarOpciones();
           seleccCasaSolaFecha.setVisibility(LinearLayout.VISIBLE);

           vacaciones.setChecked(false);
           casaSola.setChecked(true);
           visitasCasa.setChecked(false);
           noMolestar.setChecked(false);
           ignorarTodo.setChecked(false);
       } else if (id.equals(StringUtils.config_visitasCasa)){
           desactivarOpciones();
           seleccVisitasHogar.setVisibility(LinearLayout.VISIBLE);

           vacaciones.setChecked(false);
           casaSola.setChecked(false);
           visitasCasa.setChecked(true);
           noMolestar.setChecked(false);
           ignorarTodo.setChecked(false);
       } else if (id.equals(StringUtils.config_noMolestar)){
           desactivarOpciones();
           seleccNoMolestarHora.setVisibility(LinearLayout.VISIBLE);

           vacaciones.setChecked(false);
           casaSola.setChecked(false);
           visitasCasa.setChecked(false);
           noMolestar.setChecked(true);
           ignorarTodo.setChecked(false);
       } else if (id.equals(StringUtils.config_ignorarTodo)){
           desactivarOpciones();
           seleccIgnorar.setVisibility(LinearLayout.VISIBLE);

           vacaciones.setChecked(false);
           casaSola.setChecked(false);
           visitasCasa.setChecked(false);
           noMolestar.setChecked(false);
           ignorarTodo.setChecked(true);
       }
    }


    private void desactivarOpciones(){
        seleccVacionesFechas.setVisibility(LinearLayout.GONE);
        seleccCasaSolaFecha.setVisibility(LinearLayout.GONE);
        seleccNoMolestarHora.setVisibility(LinearLayout.GONE);
        seleccVisitasHogar.setVisibility(LinearLayout.GONE);
        seleccIgnorar.setVisibility(LinearLayout.GONE);
    }



}
