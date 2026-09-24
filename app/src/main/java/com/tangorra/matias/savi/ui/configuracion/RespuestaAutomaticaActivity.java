package com.tangorra.matias.savi.ui.configuracion;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.tangorra.matias.savi.Entidades.Configuracion;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.StringUtils;
import com.tangorra.matias.savi.data.Sesion;
import com.tangorra.matias.savi.data.UsuarioRepositorio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Respuesta automatica a las alertas: vacaciones, casa sola, visitas, no molestar o ignorar todo. */
public class RespuestaAutomaticaActivity extends AppCompatActivity {

    private static final class Modo {
        final String clave;
        @IdRes final int vista;
        @DrawableRes final int imagen;
        @StringRes final int titulo;
        @StringRes final int descripcion;
        @StringRes final int fecha;

        Modo(String clave, int vista, int imagen, int titulo, int descripcion, int fecha) {
            this.clave = clave;
            this.vista = vista;
            this.imagen = imagen;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.fecha = fecha;
        }
    }

    private static final Modo[] MODOS = {
            new Modo(StringUtils.config_vacaciones, R.id.modo_vacaciones, R.drawable.respuesta_viaje,
                    R.string.modo_vacaciones, R.string.modo_vacaciones_desc, R.string.modo_vacaciones_fecha),
            new Modo(StringUtils.config_casaSola, R.id.modo_casa_sola, R.drawable.respuesta_lejos,
                    R.string.modo_casa_sola, R.string.modo_casa_sola_desc, R.string.modo_casa_sola_fecha),
            new Modo(StringUtils.config_visitasCasa, R.id.modo_visitas, R.drawable.respuesta_visitas,
                    R.string.modo_visitas, R.string.modo_visitas_desc, 0),
            new Modo(StringUtils.config_noMolestar, R.id.modo_no_molestar, R.drawable.respuesta_no_molestar,
                    R.string.modo_no_molestar, R.string.modo_no_molestar_desc, R.string.modo_no_molestar_fecha),
            new Modo(StringUtils.config_ignorarTodo, R.id.modo_ignorar, R.drawable.respuesta_ignorar,
                    R.string.modo_ignorar, R.string.modo_ignorar_desc, 0),
    };

    private static final SimpleDateFormat DIA = new SimpleDateFormat("d MMM", new Locale("es", "AR"));
    private static final SimpleDateFormat HORA = new SimpleDateFormat("HH:mm", new Locale("es", "AR"));

    private Usuario usuario;
    private Modo elegido;
    private Date inicio;
    private Date fin;
    private Date limite;

    private TextView estado;
    private View btnDesactivar;
    private TextInputLayout campoFecha;
    private TextInputEditText txtFecha;
    private TextInputEditText txtMensaje;
    private MaterialButton btnActivar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_respuesta_automatica);

        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        estado = findViewById(R.id.estado);
        btnDesactivar = findViewById(R.id.btn_desactivar);
        campoFecha = findViewById(R.id.campo_fecha);
        txtFecha = findViewById(R.id.txt_fecha);
        txtMensaje = findViewById(R.id.txt_mensaje);
        btnActivar = findViewById(R.id.btn_activar);

        for (Modo modo : MODOS) {
            View tarjeta = findViewById(modo.vista);
            ((ImageView) tarjeta.findViewById(R.id.imagen)).setImageResource(modo.imagen);
            ((TextView) tarjeta.findViewById(R.id.titulo)).setText(modo.titulo);
            ((TextView) tarjeta.findViewById(R.id.descripcion)).setText(modo.descripcion);
            tarjeta.setOnClickListener(v -> elegir(modo));
        }
        txtFecha.setOnClickListener(v -> elegirFecha());
        campoFecha.setEndIconOnClickListener(v -> elegirFecha());
        btnActivar.setOnClickListener(v -> activar());
        btnDesactivar.setOnClickListener(v -> guardar(
                UsuarioRepositorio.usuarios().child(usuario.getId()).child("configuracion").child("configuracionActiva").setValue(false)));

        ViewCompat.setOnApplyWindowInsetsListener(btnActivar, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams margenes = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            margenes.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margen_boton_inferior) + barras.bottom;
            v.setLayoutParams(margenes);
            return insets;
        });

        Sesion.usuario().observe(this, u -> {
            if (u == null) {
                return;
            }
            boolean primeraCarga = usuario == null;
            usuario = u;
            mostrarEstado();
            if (primeraCarga) {
                cargarConfiguracion(u.getConfiguracion());
            }
        });
    }

    private void cargarConfiguracion(Configuracion c) {
        if (c == null || c.getConfiguracionSeleccionada() == null) {
            return;
        }
        for (Modo modo : MODOS) {
            if (modo.clave.equals(c.getConfiguracionSeleccionada())) {
                inicio = DateUtils.normalizar(c.getInicioVacaciones() != null ? c.getInicioVacaciones() : c.getAusenciaDia());
                fin = DateUtils.normalizar(c.getFinVacaciones());
                limite = c.getNoMolestar();
                txtMensaje.setText(c.getMensaje());
                elegir(modo);
            }
        }
    }

    private void mostrarEstado() {
        Configuracion c = usuario.getConfiguracion();
        if (c == null || !c.isConfiguracionActiva() || c.getConfiguracionSeleccionada() == null) {
            estado.setText(R.string.respuesta_desactivada);
            btnDesactivar.setVisibility(View.GONE);
            return;
        }
        String nombre = c.getConfiguracionSeleccionada();
        for (Modo modo : MODOS) {
            if (modo.clave.equals(nombre)) {
                nombre = getString(modo.titulo);
            }
        }
        estado.setText(c.vigente(new Date()) ? getString(R.string.respuesta_activa, nombre)
                : getString(R.string.respuesta_vencida, nombre));
        btnDesactivar.setVisibility(View.VISIBLE);
    }

    private void elegir(Modo modo) {
        if (elegido != modo) {
            // Las fechas de un modo no sirven para otro
            if (elegido != null) {
                inicio = null;
                fin = null;
                limite = null;
            }
            elegido = modo;
        }
        for (Modo m : MODOS) {
            ((MaterialCardView) findViewById(m.vista)).setChecked(m == modo);
        }
        campoFecha.setVisibility(modo.fecha != 0 ? View.VISIBLE : View.GONE);
        if (modo.fecha != 0) {
            campoFecha.setHint(modo.fecha);
        }
        mostrarFecha();
    }

    private void elegirFecha() {
        if (elegido == null) {
            return;
        }
        CalendarConstraints desdeHoy = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now()).build();
        if (StringUtils.config_vacaciones.equals(elegido.clave)) {
            MaterialDatePicker<Pair<Long, Long>> rango = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText(R.string.modo_vacaciones_fecha)
                    .setCalendarConstraints(desdeHoy)
                    .build();
            rango.addOnPositiveButtonClickListener(sel -> {
                inicio = desdeUtc(sel.first);
                fin = desdeUtc(sel.second);
                mostrarFecha();
            });
            rango.show(getSupportFragmentManager(), "rango");
        } else if (StringUtils.config_casaSola.equals(elegido.clave)) {
            MaterialDatePicker<Long> dia = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.modo_casa_sola_fecha)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(desdeHoy)
                    .build();
            dia.addOnPositiveButtonClickListener(sel -> {
                inicio = desdeUtc(sel);
                mostrarFecha();
            });
            dia.show(getSupportFragmentManager(), "dia");
        } else if (StringUtils.config_noMolestar.equals(elegido.clave)) {
            MaterialTimePicker hora = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(7)
                    .setTitleText(R.string.modo_no_molestar_fecha)
                    .build();
            hora.addOnPositiveButtonClickListener(v -> {
                // Si la hora ya paso hoy, es la de manana (antes quedaba vencido al guardarlo)
                limite = DateUtils.proximaHora(hora.getHour(), hora.getMinute(), new Date());
                mostrarFecha();
            });
            hora.show(getSupportFragmentManager(), "hora");
        }
    }

    /** Los selectores de fecha devuelven medianoche UTC; se guarda medianoche local. */
    private static Date desdeUtc(long utc) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(utc);
        return DateUtils.fecha(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    private void mostrarFecha() {
        String texto = "";
        if (elegido != null) {
            if (StringUtils.config_vacaciones.equals(elegido.clave) && inicio != null && fin != null) {
                texto = DIA.format(inicio) + " – " + DIA.format(fin);
            } else if (StringUtils.config_casaSola.equals(elegido.clave) && inicio != null) {
                texto = DIA.format(inicio);
            } else if (StringUtils.config_noMolestar.equals(elegido.clave) && limite != null) {
                texto = HORA.format(limite);
            }
        }
        txtFecha.setText(texto);
        btnActivar.setEnabled(elegido != null && (elegido.fecha == 0 || !texto.isEmpty()));
    }

    private void activar() {
        if (usuario == null || elegido == null) {
            return;
        }
        String mensaje = txtMensaje.getText() != null ? txtMensaje.getText().toString().trim() : "";
        Configuracion c = new Configuracion();
        c.activarConfiguracion(elegido.clave, mensaje.isEmpty() ? null : mensaje);
        if (StringUtils.config_vacaciones.equals(elegido.clave)) {
            c.setInicioVacaciones(inicio);
            c.setFinVacaciones(fin);
        } else if (StringUtils.config_casaSola.equals(elegido.clave)) {
            c.setAusenciaDia(inicio);
        } else if (StringUtils.config_noMolestar.equals(elegido.clave)) {
            c.setNoMolestar(limite);
        } else if (StringUtils.config_visitasCasa.equals(elegido.clave)) {
            c.setVisitasCasa(true);
        } else {
            c.setIgnorarTodo(true);
        }
        guardar(UsuarioRepositorio.usuarios().child(usuario.getId()).child("configuracion").setValue(c));
    }

    private void guardar(Task<Void> tarea) {
        findViewById(R.id.progreso).setVisibility(View.VISIBLE);
        tarea.addOnCompleteListener(t -> {
            findViewById(R.id.progreso).setVisibility(View.GONE);
            if (!t.isSuccessful()) {
                Snackbar.make(findViewById(R.id.raiz), R.string.respuesta_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
