package com.tangorra.matias.savi.Service;

import com.tangorra.matias.savi.Entidades.Alerta;
import com.tangorra.matias.savi.Entidades.Usuario;
import com.tangorra.matias.savi.Utils.StringUtils;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ProcesadorAlertasTest {

    private final Usuario beto = new Usuario("u2", "beto@barrio.com");

    private static Alerta alerta(String dirigida, String dirigidaId) {
        Alerta a = new Alerta("a1", dirigida, StringUtils.ALARMA_SONANDO, new Date(), "ana garcia");
        a.setDirigidaId(dirigidaId);
        return a;
    }

    @Test
    public void textoSegunDondeEsLaAlerta() {
        assertEquals("En tu casa · la emitió Ana Garcia",
                ProcesadorAlertas.textoNotificacion(alerta("Beto Lopez ", "u2"), beto));
        assertEquals("En la casa de Caro Diaz · la emitió Ana Garcia",
                ProcesadorAlertas.textoNotificacion(alerta("Caro Diaz ", "u3"), beto));
        assertEquals("En el barrio · la emitió Ana Garcia",
                ProcesadorAlertas.textoNotificacion(alerta(StringUtils.ALL_USERS, null), beto));
    }
}
