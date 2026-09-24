package com.tangorra.matias.savi.ui.grupo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GrupoActivityTest {

    @Test
    public void elZoomBajaAlAgrandarElRadio() {
        assertEquals(16f, GrupoActivity.zoomPara(150), 0.01f);
        assertEquals(15f, GrupoActivity.zoomPara(300), 0.01f);
        assertTrue(GrupoActivity.zoomPara(2000) < GrupoActivity.zoomPara(500));
    }

    @Test
    public void elZoomQuedaAcotado() {
        assertEquals(17f, GrupoActivity.zoomPara(0), 0.01f);
        assertEquals(11f, GrupoActivity.zoomPara(100_000), 0.01f);
    }
}
