package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.MapsUtils;

public final class PoliticaNotificaciones {

    private PoliticaNotificaciones() {
    }

    /** Se avisa si no la creo el usuario, no la vio y alguno de sus domicilios esta dentro del rango. */
    public static boolean debeNotificar(Notificacion notificacion, Usuario usuario) {
        if (notificacion == null || usuario == null || usuario.getId() == null) {
            return false;
        }
        if (usuario.getId().equals(notificacion.getCreadoBy())) {
            return false;
        }
        if (notificacion.getVistoPor() != null && notificacion.getVistoPor().contains(usuario.getId())) {
            return false;
        }
        if (usuario.getPerfil() == null) {
            return false;
        }
        // Antes se exigia estar en rango de ambos domicilios, y sin domicilio alternativo nunca se notificaba.
        return enRango(notificacion, usuario.getPerfil().getDomicilio())
                || enRango(notificacion, usuario.getPerfil().getDomicilioAlterno());
    }

    static boolean enRango(Notificacion notificacion, Domicilio domicilio) {
        if (domicilio == null || notificacion.getRango() == null) {
            return false;
        }
        double distancia = MapsUtils.distanciaCoord(notificacion.getLat(), notificacion.getLng(), domicilio.getLat(), domicilio.getLng());
        return distancia < notificacion.getRango();
    }
}
