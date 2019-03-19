package com.tangorra.matias.savi.Entidades;

import java.io.Serializable;
import java.util.List;

public class PerfilUsuario implements Serializable {

    private Domicilio domicilio;
    private Domicilio domicilioAlterno;

    public PerfilUsuario() {
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    public Domicilio getDomicilioAlterno() {
        return domicilioAlterno;
    }

    public void setDomicilioAlterno(Domicilio domicilioAlterno) {
        this.domicilioAlterno = domicilioAlterno;
    }
}
