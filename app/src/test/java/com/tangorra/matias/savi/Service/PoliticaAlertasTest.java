package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.RespuestaAlerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.StringUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PoliticaAlertasTest {

    private static Usuario usuario(String id, String nombre, String apellido) {
        Usuario u = new Usuario(id, id + "@barrio.com");
        u.setNombre(nombre);
        u.setApellido(apellido);
        return u;
    }

    private static Alerta alerta(String tipo, String creador, String dirigidaId) {
        Alerta a = new Alerta("a1", "Destino", tipo, new Date(), "Creador");
        a.setEstado(StringUtils.alertaActiva);
        a.setCreadoById(creador);
        a.setDirigidaId(dirigidaId);
        return a;
    }

    private final Usuario yo = usuario("u1", "ana", "perez");

    @Test
    public void requiereRespuestaSiEstaActivaYNoLaCreeNiRespondi() {
        assertTrue(PoliticaAlertas.requiereRespuesta(alerta(StringUtils.AGRESION, "u2", null), yo));
    }

    @Test
    public void noRequiereRespuestaSiLaCreeYo() {
        assertFalse(PoliticaAlertas.requiereRespuesta(alerta(StringUtils.AGRESION, "u1", null), yo));
    }

    @Test
    public void alertaSinCreadorNoRompe() {
        // Las alertas "para todos" se guardaban sin creadoById y provocaban NPE
        assertTrue(PoliticaAlertas.requiereRespuesta(alerta(StringUtils.AGRESION, null, null), yo));
    }

    @Test
    public void noRequiereRespuestaSiYaRespondi() {
        Alerta a = alerta(StringUtils.AGRESION, "u2", null);
        RespuestaAlerta r = new RespuestaAlerta();
        r.setIdUsuario("u1");
        a.setRespuestas(new ArrayList<RespuestaAlerta>());
        a.getRespuestas().add(r);
        assertFalse(PoliticaAlertas.requiereRespuesta(a, yo));
    }

    @Test
    public void noRequiereRespuestaSiEstaDesactivada() {
        Alerta a = alerta(StringUtils.AGRESION, "u2", null);
        a.setEstado(StringUtils.alertaDesactivada);
        assertFalse(PoliticaAlertas.requiereRespuesta(a, yo));
    }

    @Test
    public void alarmaSonandoSoloAvisaAlDestinatario() {
        assertEquals(PoliticaAlertas.Modo.SONORA, PoliticaAlertas.aviso(alerta(StringUtils.ALARMA_SONANDO, "u2", "u1"), yo).modo);
        assertNull(PoliticaAlertas.aviso(alerta(StringUtils.ALARMA_SONANDO, "u2", "u3"), yo));
    }

    @Test
    public void sospechaDeRoboAvisaATodosMenosAlDestinatario() {
        assertNull(PoliticaAlertas.aviso(alerta(StringUtils.SOSPECHA_ROBO, "u2", "u1"), yo));
        assertEquals(PoliticaAlertas.Modo.SONORA, PoliticaAlertas.aviso(alerta(StringUtils.SOSPECHA_ROBO, "u2", "u3"), yo).modo);
    }

    @Test
    public void principioDeFuegoSuenaAlDestinatarioYVibraAlResto() {
        assertEquals(PoliticaAlertas.Modo.SONORA, PoliticaAlertas.aviso(alerta(StringUtils.PRINCIPIO_FUEGO, "u2", "u1"), yo).modo);
        assertEquals(PoliticaAlertas.Modo.VIBRACION, PoliticaAlertas.aviso(alerta(StringUtils.PRINCIPIO_FUEGO, "u2", "u3"), yo).modo);
    }

    @Test
    public void malEstacionadoEsSilenciosa() {
        assertEquals(PoliticaAlertas.Modo.SILENCIOSA, PoliticaAlertas.aviso(alerta(StringUtils.MAL_ESTACIONADO, "u2", null), yo).modo);
    }

    @Test
    public void alertasViejasSeComparanPorNombre() {
        Alerta a = alerta(StringUtils.DANO_VEHICULO, "u2", null);
        a.setDirigida(StringUtils.getTextoFormateado(yo.getGlosa()));
        assertEquals(PoliticaAlertas.Modo.VIBRACION, PoliticaAlertas.aviso(a, yo).modo);
    }
}
