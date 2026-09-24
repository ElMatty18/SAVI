package com.tangorra.matias.savi.Entidades;

import com.tangorra.matias.savi.Utils.StringUtils;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class AlertaTest {

    private static Alerta conRespuestas(String... respuestas) {
        Alerta alerta = new Alerta();
        alerta.setRespuestas(new HashMap<String, RespuestaAlerta>());
        for (int i = 0; i < respuestas.length; i++) {
            RespuestaAlerta r = new RespuestaAlerta();
            r.setIdUsuario("u" + i);
            if ("auto".equals(respuestas[i])) {
                r.setRespuestaAutomatica(StringUtils.config_vacaciones);
            } else {
                r.setRespuesta(respuestas[i]);
            }
            alerta.getRespuestas().put("u" + i, r);
        }
        return alerta;
    }

    private static final String SI = StringUtils.respuesta_confirma;
    private static final String NO = StringUtils.respuesta_cancela;

    @Test
    public void sinRespuestasEsCero() {
        assertEquals(0, new Alerta().obtenerNivelAlerta());
        assertEquals(0, conRespuestas().obtenerNivelAlerta());
    }

    @Test
    public void mayoriaQueConfirmaEsMaximo() {
        assertEquals(100, conRespuestas(SI, SI, NO).obtenerNivelAlerta());
    }

    @Test
    public void nivelesIntermediosYaNoSeRedondeanA0o100() {
        // 1 de 3 confirma: 200 * 1 / 3
        assertEquals(67, conRespuestas(SI, NO, NO).obtenerNivelAlerta());
        // 1 de 5: 40, por debajo del umbral de 50 que muestra el menu de autoridades
        assertEquals(40, conRespuestas(SI, NO, NO, NO, NO).obtenerNivelAlerta());
    }

    @Test
    public void respuestasAutomaticasNoCuentanComoConfirmacion() {
        assertEquals(0, conRespuestas("auto", NO).obtenerNivelAlerta());
        assertEquals(100, conRespuestas(SI, "auto").obtenerNivelAlerta());
    }
}
