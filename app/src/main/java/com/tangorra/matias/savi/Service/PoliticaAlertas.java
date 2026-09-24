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

        public Aviso(String nivel, Modo modo) {
            this.nivel = nivel;
            this.modo = modo;
        }
    }

    private PoliticaAlertas() {
    }

    /**
     * Con una respuesta automatica vigente el aviso cambia: "no molestar" lo hace silencioso
     * e "ignorar todo" lo suprime. El resto de los modos avisa normalmente.
     */
    public static Aviso ajustarPorConfiguracion(Aviso aviso, com.tangorra.matias.savi.Entidades.Configuracion configuracion,
                                                java.util.Date ahora) {
        if (aviso == null || configuracion == null || !configuracion.vigente(ahora)) {
            return aviso;
        }
        String modo = configuracion.getConfiguracionSeleccionada();
        if (StringUtils.config_ignorarTodo.equals(modo)) {
            return null;
        } else if (StringUtils.config_noMolestar.equals(modo)) {
            return new Aviso(aviso.nivel, Modo.SILENCIOSA);
        }
        return aviso;
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

    /** Desde el detalle se puede responder mientras la alerta no este cerrada, salvo quien la creo. */
    public static boolean puedeResponder(Alerta alerta, Usuario usuario) {
        if (alerta == null || usuario == null || usuario.getId() == null) {
            return false;
        }
        boolean abierta = StringUtils.alertaActiva.equals(alerta.getEstado())
                || StringUtils.alertaConfirmadaDirigida.equals(alerta.getEstado());
        return abierta && !esCreador(alerta, usuario) && !respondio(alerta, usuario.getId());
    }

    /** Quien la creo puede darla por resuelta. */
    public static boolean puedeCerrar(Alerta alerta, Usuario usuario) {
        return alerta != null && usuario != null && esCreador(alerta, usuario)
                && !StringUtils.alertaDesactivada.equals(alerta.getEstado());
    }

    public static boolean esCreador(Alerta alerta, Usuario usuario) {
        return usuario.getId() != null && usuario.getId().equals(alerta.getCreadoById());
    }

    /** Se sugiere llamar a las autoridades si el destinatario la confirmo o los vecinos la consideran grave. */
    public static boolean sugerirAutoridades(Alerta alerta) {
        return StringUtils.alertaConfirmadaDirigida.equals(alerta.getEstado()) || alerta.obtenerNivelAlerta() >= 50;
    }

    /**
     * Estado nuevo al responder: solo el destinatario confirma o desactiva la alerta.
     * @return el estado a guardar, o null si no cambia.
     */
    public static String estadoAlResponder(Alerta alerta, Usuario usuario, String respuesta) {
        if (!esDirigidaA(alerta, usuario) || alerta.getDirigidaId() == null) {
            return null;
        }
        if (StringUtils.respuesta_cancela.equals(respuesta)) {
            return StringUtils.alertaDesactivada;
        } else if (StringUtils.respuesta_confirma.equals(respuesta)) {
            return StringUtils.alertaConfirmadaDirigida;
        }
        return null;
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
