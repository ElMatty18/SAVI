package com.tangorra.matias.savi.ui.comun;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.R;
import com.tangorra.matias.savi.Utils.StringUtils;

/** Como se muestra cada tipo y estado de alerta en la interfaz. */
public final class PresentacionAlerta {

    public static final String[] TIPOS = {
            StringUtils.ALARMA_SONANDO,
            StringUtils.SOSPECHA_ROBO,
            StringUtils.ACTITUD_SOSPECHOSA,
            StringUtils.DANO_VEHICULO,
            StringUtils.PRINCIPIO_FUEGO,
            StringUtils.AGRESION,
            StringUtils.MAL_ESTACIONADO,
    };

    private static final int[] IMAGENES = {
            R.drawable.alarma1, R.drawable.alarma2, R.drawable.alarma3, R.drawable.alarma4,
            R.drawable.alarma5, R.drawable.alarma6, R.drawable.alarma7,
    };

    private PresentacionAlerta() {
    }

    @DrawableRes
    public static int imagen(String tipo) {
        for (int i = 0; i < TIPOS.length; i++) {
            if (TIPOS[i].equals(tipo)) {
                return IMAGENES[i];
            }
        }
        return R.drawable.alarmar;
    }

    public enum Estado {
        ACTIVA("Activa", R.color.alerta_activa, R.color.alerta_activa_contenedor),
        CONFIRMADA("Confirmada", R.color.alerta_atencion, R.color.alerta_atencion_contenedor),
        CERRADA("Cerrada", R.color.alerta_cerrada, R.color.alerta_cerrada_contenedor);

        public final String etiqueta;
        @ColorRes public final int color;
        @ColorRes public final int contenedor;

        Estado(String etiqueta, int color, int contenedor) {
            this.etiqueta = etiqueta;
            this.color = color;
            this.contenedor = contenedor;
        }
    }

    public static Estado estado(Alerta alerta) {
        if (StringUtils.alertaActiva.equals(alerta.getEstado())) {
            return Estado.ACTIVA;
        } else if (StringUtils.alertaConfirmadaDirigida.equals(alerta.getEstado())) {
            return Estado.CONFIRMADA;
        }
        return Estado.CERRADA;
    }

    /** "Para Ana Garcia · hace 12 minutos" */
    public static String detalle(Context context, Alerta alerta) {
        String destino = StringUtils.ALL_USERS.equals(alerta.getDirigida()) || alerta.getDirigida() == null
                ? "Para todo el grupo"
                : "Para " + alerta.getDirigida().trim();
        if (alerta.getCreacion() == null) {
            return destino;
        }
        return destino + " · " + hace(alerta.getCreacion().getTime(), System.currentTimeMillis());
    }

    private static final long MINUTO = 60_000L;
    private static final long HORA = 60 * MINUTO;
    private static final long DIA = 24 * HORA;

    /** Tiempo transcurrido en castellano: "recien", "hace 5 min", "hace 2 h", "ayer", "hace 3 dias" o la fecha. */
    public static String hace(long momento, long ahora) {
        long diferencia = Math.max(0, ahora - momento);
        if (diferencia < MINUTO) {
            return "recién";
        } else if (diferencia < HORA) {
            return "hace " + (diferencia / MINUTO) + " min";
        } else if (diferencia < DIA) {
            return "hace " + (diferencia / HORA) + " h";
        } else if (diferencia < 2 * DIA) {
            return "ayer";
        } else if (diferencia < 7 * DIA) {
            return "hace " + (diferencia / DIA) + " días";
        }
        return new java.text.SimpleDateFormat("d MMM yyyy", new java.util.Locale("es", "AR")).format(new java.util.Date(momento));
    }
}
