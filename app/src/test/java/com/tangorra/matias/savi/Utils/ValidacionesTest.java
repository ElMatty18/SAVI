package com.tangorra.matias.savi.Utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidacionesTest {

    @Test
    public void mailValido() {
        assertTrue(Validaciones.esMailValido("vecino@barrio.com"));
        assertTrue(Validaciones.esMailValido("  vecino.uno@barrio.com.ar "));
    }

    @Test
    public void mailInvalido() {
        assertFalse(Validaciones.esMailValido(null));
        assertFalse(Validaciones.esMailValido(""));
        assertFalse(Validaciones.esMailValido("correo-invalido"));
        assertFalse(Validaciones.esMailValido("a@b"));
        assertFalse(Validaciones.esMailValido("a b@barrio.com"));
    }

    @Test
    public void claveSegura() {
        assertTrue(Validaciones.esClaveSegura("Barrio2024"));
        assertTrue(Validaciones.esClaveSegura("Ab1-cd!"));
    }

    @Test
    public void claveInsegura() {
        assertFalse(Validaciones.esClaveSegura(null));
        assertFalse(Validaciones.esClaveSegura("Ab1"));          // corta
        assertFalse(Validaciones.esClaveSegura("barrio2024"));   // sin mayuscula
        assertFalse(Validaciones.esClaveSegura("BARRIO2024"));   // sin minuscula
        assertFalse(Validaciones.esClaveSegura("BarrioSeguro")); // sin numero
        assertFalse(Validaciones.esClaveSegura("Barrio 2024"));  // con espacio
    }

    @Test
    public void dni() {
        assertTrue(Validaciones.esDniValido("30123456"));
        assertTrue(Validaciones.esDniValido("30.123.456"));
        assertTrue(Validaciones.esDniValido("5123456"));
        assertFalse(Validaciones.esDniValido(""));
        assertFalse(Validaciones.esDniValido("123"));
        assertFalse(Validaciones.esDniValido("30123456a"));
    }
}
