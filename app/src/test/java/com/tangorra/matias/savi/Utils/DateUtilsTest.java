package com.tangorra.matias.savi.Utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DateUtilsTest {

    private static String formato(Date fecha) {
        return DateUtils.sdf3.format(fecha);
    }

    @Test
    public void fechaUsaMesDelDatePicker() {
        assertEquals("15/03/1990", formato(DateUtils.fecha(1990, Calendar.MARCH, 15)));
    }

    @Test
    public void corrigeFechasGuardadasConElBugViejo() {
        // Lo que guardaba la version anterior al elegir 15/03/1990 en el DatePicker
        @SuppressWarnings("deprecation")
        Date legacy = new Date(1990, 3, 15);
        assertEquals("15/03/1990", formato(DateUtils.normalizar(legacy)));
    }

    @Test
    public void noTocaFechasCorrectas() {
        Date correcta = DateUtils.fecha(1990, Calendar.MARCH, 15);
        assertSame(correcta, DateUtils.normalizar(correcta));
        assertNull(DateUtils.normalizar(null));
    }
}
