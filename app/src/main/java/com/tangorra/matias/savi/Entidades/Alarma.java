package com.tangorra.matias.savi.Entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Alarma implements Serializable {

    private String id;
    private String casa;
    private String alarma;
    private Date creacion;

    private List<String> vistoPor;


    public Alarma(String id, String casa, String alarma, Date creacion) {
        this.id = id;
        this.casa = casa;
        this.alarma = alarma;
        this.creacion = creacion;
    }

    public Alarma() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCasa() {
        return casa;
    }

    public void setCasa(String casa) {
        this.casa = casa;
    }

    public String getAlarma() {
        return alarma;
    }

    public void setAlarma(String alarma) {
        this.alarma = alarma;
    }

    public Date getCreacion() {
        return creacion;
    }

    public void setCreacion(Date creacion) {
        this.creacion = creacion;
    }

    public List<String> getVistoPor() {
        return vistoPor;
    }

    public void setVistoPor(List<String> vistoPor) {
        this.vistoPor = vistoPor;
    }

    @Override
    public String toString() {
        return "Alarma{" +
                "id='" + id + '\'' +
                ", casa='" + casa + '\'' +
                ", alarma='" + alarma + '\'' +
                '}';
    }
}
