package com.tangorra.matias.savi.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static SimpleDateFormat sdf2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm"	);
    public static SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yyyy");

    // Versiones anteriores guardaban las fechas con new Date(anio, mes + 1, dia): el constructor
    // deprecado suma 1900 al anio, asi que quedaban cerca del anio 3900 y con el mes corrido.
    private static final int ANIO_LEGACY = 3000;

    /** Crea una fecha a partir de los valores de un DatePicker (mes 0-11). */
    public static Date fecha(int anio, int mes, int dia) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(anio, mes, dia);
        return cal.getTime();
    }

    /** Corrige las fechas guardadas con el bug del constructor deprecado. */
    public static Date normalizar(Date fecha) {
        if (fecha == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        if (cal.get(Calendar.YEAR) < ANIO_LEGACY) {
            return fecha;
        }
        return fecha(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH) - 1, cal.get(Calendar.DAY_OF_MONTH));
    }
}
