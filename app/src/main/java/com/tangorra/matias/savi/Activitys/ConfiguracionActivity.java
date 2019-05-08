package com.tangorra.matias.savi.Activitys;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tangorra.matias.savi.Entidades.Configuracion;
import com.tangorra.matias.savi.Entidades.SesionManager;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.FirebaseUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.util.Calendar;
import java.util.Date;

public class ConfiguracionActivity extends AppCompatActivity {

    private DatabaseReference dbUsuarios = FirebaseDatabase.getInstance().getReference(FirebaseUtils.dbUsuario);

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

    private TextView mDisplayDateAusencia;
    private DatePickerDialog.OnDateSetListener mDateSetListenerAusencia;
    private Date fechaAusencia;

    private TimePicker seleccNoMolestarHoraFin;

    private Button confirmarVacaciones;
    private Button confirmarCasaSola;
    private Button confirmarVisitasCasa;
    private Button confirmarNoMolestar;
    private Button confirmarIgnorarTodo;

    private Button cancelarVacaciones;
    private Button cancelarCasaSola;
    private Button cancelarVisitasCasa;
    private Button cancelarNoMolestar;
    private Button cancelarIgnorarTodo;

    private TextView mensajeVacaciones;
    private TextView mensajeAusente;
    private TextView mensajeVisitas;
    private TextView mensajeNoMolestar;
    private TextView mensajeIgnorarTodo;

    private Calendar calendar;
    private String format = "";
    private TextView time;





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

        validarConfiguracionCargada(SesionManager.getUsuario());

    }

    private void validarConfiguracionCargada(Usuario usuario) {
        if (usuario.getConfiguracion() != null){
           String seleccionada = usuario.getConfiguracion().getConfiguracionSeleccionada();
           if (seleccionada != null ){
               if (seleccionada.equals(StringUtils.config_vacaciones)){
                   //cargar
                   vacaciones.performClick();
                   mDisplayDateVacacionesInicio.setText(getFechaFormateada(SesionManager.getUsuario().getConfiguracion().getInicioVacaciones()));
                   mDisplayDateVacacionesFin.setText(getFechaFormateada(SesionManager.getUsuario().getConfiguracion().getFinVacaciones()));
                   mensajeVacaciones.setText(SesionManager.getUsuario().getConfiguracion().getMensaje());
               } else if (seleccionada.equals(StringUtils.config_casaSola)){
                   //cargar
                   casaSola.performClick();
                   mDisplayDateAusencia.setText(getFechaFormateada(SesionManager.getUsuario().getConfiguracion().getAusenciaDia()));
                   mensajeAusente.setText(SesionManager.getUsuario().getConfiguracion().getMensaje());
               } else if (seleccionada.equals(StringUtils.config_visitasCasa)){
                   //cargar
                   visitasCasa.performClick();
                   mensajeVisitas.setText(SesionManager.getUsuario().getConfiguracion().getMensaje());
               } else if (seleccionada.equals(StringUtils.config_noMolestar)){
                   //cargar
                   noMolestar.performClick();
                   mensajeNoMolestar.setText(SesionManager.getUsuario().getConfiguracion().getMensaje());
               } else if (seleccionada.equals(StringUtils.config_ignorarTodo)){
                   //cargar
                   ignorarTodo.performClick();
                   mensajeIgnorarTodo.setText(SesionManager.getUsuario().getConfiguracion().getMensaje());
               }
           }
        }
    }


    private String getFechaFormateada(Date fecha){
        int dia = fecha.getDate();
        int mes = fecha.getMonth();
        int anio = fecha.getYear();
        String formateada = String.valueOf(dia) +"/"+ String.valueOf(mes) +"/"+ String.valueOf(anio);
        return formateada;
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
        mDisplayDateAusencia = findViewById(R.id.configuracion_casa_sola);

        mensajeVacaciones = findViewById(R.id.txtConfiguracionMensajeVacaciones);
        mensajeAusente = findViewById(R.id.txtConfiguracionMensajeAusencia);
        mensajeVisitas = findViewById(R.id.txtConfiguracionMensajeVisitas);
        mensajeNoMolestar = findViewById(R.id.txtConfiguracionMensajeNoMolestar);
        mensajeIgnorarTodo = findViewById(R.id.txtConfiguracionMensajeIgnorarTodo);

        fechaPicker();

        seleccNoMolestarHoraFin  = (TimePicker) findViewById(R.id.seleccNoMolestarHoraFin);




     /*   int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        showTime(hour, min);*/

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


        confirmarVacaciones = findViewById(R.id.btnConfiguracionConfirmarVacaciones);
        confirmarVacaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validarObligatorios
                if (validaVacaciones()){
                    Configuracion configuracion = new Configuracion();
                    configuracion.activarConfiguracion(StringUtils.config_vacaciones, mensajeVacaciones.getText().toString());
                    configuracion.setInicioVacaciones(fechaInicioVacaciones);
                    configuracion.setFinVacaciones(fechaFinVacaciones);
                    SesionManager.getUsuario().setConfiguracion(configuracion);

                    persistirUsuario(SesionManager.getUsuario());
                }

            }
        });

        confirmarCasaSola = findViewById(R.id.btnConfiguracionConfirmarAusencia);
        confirmarCasaSola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validarObligatorios
                if (validaAusencia()){
                    Configuracion configuracion = new Configuracion();
                    configuracion.activarConfiguracion(StringUtils.config_casaSola, mensajeAusente.getText().toString());
                    configuracion.setAusenciaDia(fechaAusencia);
                    SesionManager.getUsuario().setConfiguracion(configuracion);

                    persistirUsuario(SesionManager.getUsuario());
                }

            }
        });

        confirmarVisitasCasa = findViewById(R.id.btnConfiguracionConfirmarVisitas);
        confirmarVisitasCasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validarObligatorios
                if (validaVisitas()){
                    Configuracion configuracion = new Configuracion();
                    configuracion.activarConfiguracion(StringUtils.config_visitasCasa, mensajeVisitas.getText().toString());
                    configuracion.setVisitasCasa(true);
                    SesionManager.getUsuario().setConfiguracion(configuracion);

                    persistirUsuario(SesionManager.getUsuario());
                }


            }
        });

        confirmarNoMolestar = findViewById(R.id.btnConfiguracionConfirmarNoMolestar);
        confirmarNoMolestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validaNoMolestar()){
                    //validarObligatorios

                    int hour, minute;
                    String am_pm;
                    if (Build.VERSION.SDK_INT >= 23 ){
                        hour = seleccNoMolestarHoraFin.getHour();
                        minute = seleccNoMolestarHoraFin.getMinute();
                    }
                    else{
                        hour = seleccNoMolestarHoraFin.getCurrentHour();
                        minute = seleccNoMolestarHoraFin.getCurrentMinute();
                    }

                    Date noMolestarDate = new Date();

                    noMolestarDate.setHours(hour);
                    noMolestarDate.setMinutes(minute);

                    Configuracion configuracion = new Configuracion();
                    configuracion.activarConfiguracion(StringUtils.config_noMolestar, mensajeNoMolestar.getText().toString());
                    configuracion.setNoMolestar(new Date());
                    configuracion.setNoMolestar(noMolestarDate);
                    SesionManager.getUsuario().setConfiguracion(configuracion);

                    persistirUsuario(SesionManager.getUsuario());
                }
            }
        });



        confirmarIgnorarTodo = findViewById(R.id.btnConfiguracionConfirmarIgnorar);
        confirmarIgnorarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validarObligatorios
                if (validaIgnorarTodo()){
                    Configuracion configuracion = new Configuracion();
                    configuracion.activarConfiguracion(StringUtils.config_ignorarTodo, mensajeIgnorarTodo.getText().toString());
                    configuracion.setIgnorarTodo(true);
                    SesionManager.getUsuario().setConfiguracion(configuracion);

                    persistirUsuario(SesionManager.getUsuario());
                }
            }
        });


        cancelarVacaciones= findViewById(R.id.btnDesactivarVacaciones);
        cancelarVacaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desactivarConfiguracion(SesionManager.getUsuario());
            }
        });

        cancelarCasaSola= findViewById(R.id.btnDesactivarAusencia);
        cancelarCasaSola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desactivarConfiguracion(SesionManager.getUsuario());
            }
        });

        cancelarVisitasCasa= findViewById(R.id.btnDesactivarVisitas);
        cancelarCasaSola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desactivarConfiguracion(SesionManager.getUsuario());
            }
        });

        cancelarNoMolestar= findViewById(R.id.btnDesactivarNoMolestar);
        cancelarNoMolestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desactivarConfiguracion(SesionManager.getUsuario());
            }
        });

        cancelarIgnorarTodo= findViewById(R.id.btnDesactivarIgnorarTodo);
        cancelarIgnorarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desactivarConfiguracion(SesionManager.getUsuario());
            }
        });


    }


    public void showTime(int hour, int min) {
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        time.setText(new StringBuilder().append(hour).append(" : ").append(min)
                .append(" ").append(format));
    }

    private boolean validaIgnorarTodo() {
        boolean valida = true;
        if (mensajeIgnorarTodo == null){
            mensajeIgnorarTodo.setText(StringUtils.fieldRequired);
            valida = false;
        }

        return valida;
    }

    private boolean validaVisitas() {
        boolean valida = true;
        if (mensajeVisitas == null){
            mensajeVisitas.setText(StringUtils.fieldRequired);
            valida = false;
        }

        return valida;
    }

    private boolean validaNoMolestar() {
        boolean valida = true;
        if (mensajeNoMolestar == null){
            mensajeNoMolestar.setText(StringUtils.fieldRequired);
            valida = false;
        }
        return valida;
    }

    private boolean validaAusencia() {
        boolean valida = true;
        if (fechaAusencia == null){
            mDisplayDateAusencia.setText(StringUtils.fieldRequired);
            valida = false;
        }

        if (mensajeAusente == null){
            mensajeAusente.setText(StringUtils.fieldRequired);
            valida = false;
        }

        return valida;
    }

    private boolean validaVacaciones() {
        boolean valida = true;
        if (fechaInicioVacaciones == null){
            mDisplayDateVacacionesInicio.setText(StringUtils.fieldRequired);
            valida = false;
        }
        if (fechaFinVacaciones == null){
            mDisplayDateVacacionesFin.setText(StringUtils.fieldRequired);
            valida = false;
        }
        if (mensajeVacaciones == null){
            mensajeVacaciones.setText(StringUtils.fieldRequired);
            valida = false;
        }
        return valida;
    }


    private void persistirUsuario(Usuario u) {
        dbUsuarios.child(u.getId()).setValue(u);
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


        mDisplayDateAusencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ConfiguracionActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerAusencia,
                        year, month, day
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerAusencia = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month ++;
                String date = dayOfMonth + "/" + month + "/" + year;
                mDisplayDateAusencia.setText(date);
                fechaAusencia = new Date(year, month,dayOfMonth);
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


    private void desactivarConfiguracion(Usuario u){
        u.setConfiguracion(new Configuracion());
        persistirUsuario(u);
        finish();

    }



}
