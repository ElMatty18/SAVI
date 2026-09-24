package com.tangorra.matias.savi.Entidades;

import com.tangorra.matias.savi.Service.PoliticaAlertas;
import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfiguracionTest {

    private static Date dia(int anio, int mes, int dia, int hora) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(anio, mes, dia, hora, 0);
        return cal.getTime();
    }

    private static Configuracion modo(String tipo) {
        Configuracion c = new Configuracion();
        c.activarConfiguracion(tipo, "mensaje");
        return c;
    }

    @Test
    public void vacacionesRigenSoloEntreLasFechasInclusive() {
        Configuracion c = modo(StringUtils.config_vacaciones);
        c.setInicioVacaciones(DateUtils.fecha(2026, Calendar.OCTOBER, 1));
        c.setFinVacaciones(DateUtils.fecha(2026, Calendar.OCTOBER, 10));
        assertFalse(c.vigente(dia(2026, Calendar.SEPTEMBER, 30, 23)));
        assertTrue(c.vigente(dia(2026, Calendar.OCTOBER, 1, 0)));
        assertTrue(c.vigente(dia(2026, Calendar.OCTOBER, 10, 22)));
        assertFalse(c.vigente(dia(2026, Calendar.OCTOBER, 11, 1)));
    }

    @Test
    public void casaSolaRigeSoloEseDia() {
        Configuracion c = modo(StringUtils.config_casaSola);
        c.setAusenciaDia(DateUtils.fecha(2026, Calendar.OCTOBER, 5));
        assertTrue(c.vigente(dia(2026, Calendar.OCTOBER, 5, 18)));
        assertFalse(c.vigente(dia(2026, Calendar.OCTOBER, 6, 8)));
    }

    @Test
    public void noMolestarVenceALaHora() {
        Configuracion c = modo(StringUtils.config_noMolestar);
        c.setNoMolestar(dia(2026, Calendar.OCTOBER, 5, 7));
        assertTrue(c.vigente(dia(2026, Calendar.OCTOBER, 5, 6)));
        assertFalse(c.vigente(dia(2026, Calendar.OCTOBER, 5, 8)));
    }

    @Test
    public void desactivadaNuncaRige() {
        Configuracion c = modo(StringUtils.config_ignorarTodo);
        assertTrue(c.vigente(new Date()));
        c.setConfiguracionActiva(false);
        assertFalse(c.vigente(new Date()));
    }

    @Test
    public void proximaHoraPasaAManianaSiYaPaso() {
        Date ahora = dia(2026, Calendar.OCTOBER, 5, 23);
        assertEquals(dia(2026, Calendar.OCTOBER, 6, 7), DateUtils.proximaHora(7, 0, ahora));
        assertEquals(dia(2026, Calendar.OCTOBER, 5, 23).getTime() + 30 * 60_000L,
                DateUtils.proximaHora(23, 30, ahora).getTime());
    }

    @Test
    public void ignorarTodoSuprimeElAvisoYNoMolestarLoSilencia() {
        PoliticaAlertas.Aviso sonora = new PoliticaAlertas.Aviso("alerta", PoliticaAlertas.Modo.SONORA);
        assertNull(PoliticaAlertas.ajustarPorConfiguracion(sonora, modo(StringUtils.config_ignorarTodo), new Date()));

        Configuracion noMolestar = modo(StringUtils.config_noMolestar);
        noMolestar.setNoMolestar(new Date(System.currentTimeMillis() + 3_600_000));
        assertEquals(PoliticaAlertas.Modo.SILENCIOSA,
                PoliticaAlertas.ajustarPorConfiguracion(sonora, noMolestar, new Date()).modo);

        assertEquals(PoliticaAlertas.Modo.SONORA,
                PoliticaAlertas.ajustarPorConfiguracion(sonora, modo(StringUtils.config_visitasCasa), new Date()).modo);
    }
}
