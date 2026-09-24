package com.tangorra.matias.savi.ui.comun;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Utils.StringUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PresentacionAlertaTest {

    private static final long AHORA = 1_790_000_000_000L;
    private static final long MIN = 60_000L;

    @Test
    public void tiempoTranscurrido() {
        assertEquals("recién", PresentacionAlerta.hace(AHORA - 30_000, AHORA));
        assertEquals("hace 36 min", PresentacionAlerta.hace(AHORA - 36 * MIN, AHORA));
        assertEquals("hace 2 h", PresentacionAlerta.hace(AHORA - 150 * MIN, AHORA));
        assertEquals("ayer", PresentacionAlerta.hace(AHORA - 26 * 60 * MIN, AHORA));
        assertEquals("hace 3 días", PresentacionAlerta.hace(AHORA - 3 * 24 * 60 * MIN, AHORA));
    }

    @Test
    public void fechaFuturaNoDaNegativo() {
        assertEquals("recién", PresentacionAlerta.hace(AHORA + 5 * MIN, AHORA));
    }

    @Test
    public void estados() {
        Alerta a = new Alerta();
        a.setEstado(StringUtils.alertaActiva);
        assertEquals(PresentacionAlerta.Estado.ACTIVA, PresentacionAlerta.estado(a));
        a.setEstado(StringUtils.alertaConfirmadaDirigida);
        assertEquals(PresentacionAlerta.Estado.CONFIRMADA, PresentacionAlerta.estado(a));
        a.setEstado(StringUtils.alertaDesactivada);
        assertEquals(PresentacionAlerta.Estado.CERRADA, PresentacionAlerta.estado(a));
    }
}
