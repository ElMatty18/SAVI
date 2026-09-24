package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.Entidades.Domicilio;
import com.tangorra.matias.savi.Entidades.Notificacion;
import com.tangorra.matias.savi.Entidades.Usuario;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PoliticaNotificacionesTest {

    private static Domicilio domicilio(double lat, double lng) {
        Domicilio d = new Domicilio();
        d.setLat(lat);
        d.setLng(lng);
        return d;
    }

    // Obelisco, Buenos Aires
    private static Notificacion notificacion(int rangoKm) {
        Notificacion n = new Notificacion();
        n.setId("n1");
        n.setCreadoBy("u2");
        n.setLat(-34.6037);
        n.setLng(-58.3816);
        n.setRango(rangoKm);
        return n;
    }

    private static Usuario usuarioConDomicilio(Domicilio principal) {
        Usuario u = new Usuario("u1", "u1@barrio.com");
        u.getPerfil().setDomicilio(principal);
        return u;
    }

    @Test
    public void notificaSiElDomicilioEstaEnRangoAunqueNoTengaAlternativo() {
        // Antes se exigian ambos domicilios en rango y esto nunca notificaba
        assertTrue(PoliticaNotificaciones.debeNotificar(notificacion(5), usuarioConDomicilio(domicilio(-34.6083, -58.3712))));
    }

    @Test
    public void noNotificaFueraDeRango() {
        // Plaza de Mayo a La Plata: ~50 km
        assertFalse(PoliticaNotificaciones.debeNotificar(notificacion(5), usuarioConDomicilio(domicilio(-34.9214, -57.9544))));
    }

    @Test
    public void noNotificaAlCreadorNiSiYaLaVio() {
        Usuario u = usuarioConDomicilio(domicilio(-34.6083, -58.3712));
        Notificacion propia = notificacion(5);
        propia.setCreadoBy("u1");
        assertFalse(PoliticaNotificaciones.debeNotificar(propia, u));

        Notificacion vista = notificacion(5);
        vista.setVistoPor(new HashMap<String, String>());
        vista.getVistoPor().put("u1", "u1");
        assertFalse(PoliticaNotificaciones.debeNotificar(vista, u));

        Notificacion vistaFormatoViejo = notificacion(5);
        vistaFormatoViejo.setVistoPor(new HashMap<String, String>());
        vistaFormatoViejo.getVistoPor().put("0", "u1");
        assertFalse(PoliticaNotificaciones.debeNotificar(vistaFormatoViejo, u));
    }
}
