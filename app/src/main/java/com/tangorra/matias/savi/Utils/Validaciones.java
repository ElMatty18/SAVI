package com.tangorra.matias.savi.Utils;

import java.util.regex.Pattern;

public final class Validaciones {

    private static final Pattern MAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}$");
    // Al menos un digito, una mayuscula y una minuscula, entre 6 y 30 caracteres (minimo de Firebase Auth: 6)
    private static final Pattern CLAVE = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])\\S{6,30}$");

    private Validaciones() {
    }

    public static boolean esMailValido(String mail) {
        return mail != null && MAIL.matcher(mail.trim()).matches();
    }

    /** DNI argentino: 7 u 8 digitos (se aceptan puntos). */
    public static boolean esDniValido(String dni) {
        return dni != null && dni.replace(".", "").trim().matches("\\d{7,8}");
    }

    public static boolean esClaveSegura(String clave) {
        return clave != null && CLAVE.matcher(clave).matches();
    }
}
