package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.StringUtils;

/**
 * Reglas de negocio de las alertas vecinales, sin dependencias de Android para poder testearlas.
 */
public final class PoliticaAlertas {

    public enum Modo { SONORA, VIBRACION, SILENCIOSA }

    public static final class Aviso {
        public final String nivel;
        public final Modo modo;

        Aviso(String nivel, Modo modo) {
            this.nivel = nivel;
            this.modo = modo;
        }
    }

    private PoliticaAlertas() {
    }

    /** Una alerta requiere respuesta del usuario si esta activa, no la creo el y todavia no la respondio. */
    public static boolean requiereRespuesta(Alerta alerta, Usuario usuario) {
        if (alerta == null || usuario == null || usuario.getId() == null) {
            return false;
        }
        if (!StringUtils.alertaActiva.equals(alerta.getEstado())) {
            return false;
        }
        if (usuario.getId().equals(alerta.getCreadoById())) {
            return false;
        }
        return !respondio(alerta, usuario.getId());
    }

    public static boolean respondio(Alerta alerta, String idUsuario) {
        if (alerta.getRespuestas() != null && alerta.getRespuestas().containsKey(idUsuario)) {
            return true;
        }
        // Formato viejo: lista sin clave por usuario
        for (RespuestaAlerta item : alerta.listaRespuestas()) {
            if (item != null && idUsuario.equals(item.getIdUsuario())) {
                return true;
            }
        }
        return false;
    }

    public static boolean esDirigidaA(Alerta alerta, Usuario usuario) {
        if (alerta.getDirigidaId() != null) {
            return alerta.getDirigidaId().equals(usuario.getId());
        }
        // Alertas viejas solo guardaban el nombre del destinatario.
        return alerta.getDirigida() != null
                && alerta.getDirigida().equals(StringUtils.getTextoFormateado(usuario.getGlosa()));
    }

    /** Devuelve como avisar al usuario, o null si esta alerta no le genera aviso. */
    public static Aviso aviso(Alerta alerta, Usuario usuario) {
        String tipo = alerta.getAlarma();
        if (tipo == null) {
            return null;
        }
        boolean dirigida = esDirigidaA(alerta, usuario);

        if (tipo.equals(StringUtils.ALARMA_SONANDO)) {
            // Aviso sonoro al usuario al que esta dirigida la alarma
            return dirigida ? new Aviso(StringUtils.notificacion_alerta, Modo.SONORA) : null;
        } else if (tipo.equals(StringUtils.SOSPECHA_ROBO)) {
            // Ruidosa para todos excepto para el destinatario
            return dirigida ? null : new Aviso(StringUtils.notificacion_riesgo, Modo.SONORA);
        } else if (tipo.equals(StringUtils.ACTITUD_SOSPECHOSA)) {
            return new Aviso(StringUtils.notificacion_cuidado, Modo.SONORA);
        } else if (tipo.equals(StringUtils.DANO_VEHICULO)) {
            return dirigida ? new Aviso(StringUtils.notificacion_advertencia, Modo.VIBRACION) : null;
        } else if (tipo.equals(StringUtils.PRINCIPIO_FUEGO)) {
            // Sonora para el destinatario, vibracion para los demas
            return new Aviso(StringUtils.notificacion_peligro, dirigida ? Modo.SONORA : Modo.VIBRACION);
        } else if (tipo.equals(StringUtils.AGRESION)) {
            return new Aviso(StringUtils.notificacion_amenaza, Modo.SONORA);
        } else if (tipo.equals(StringUtils.MAL_ESTACIONADO)) {
            return new Aviso(StringUtils.notificacion_aviso, Modo.SILENCIOSA);
        }
        return null;
    }
}
