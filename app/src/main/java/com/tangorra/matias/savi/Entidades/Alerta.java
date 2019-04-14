package com.tangorra.matias.savi.Entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Alerta implements Serializable {

    private String id;
    private String casa;
    private String alarma;

    private Date creacion;
    private String creadoBy;

    private List<String> vistoPor;

    public Alerta(String id, String casa, String alarma, Date creacion, String creadoBy) {
        this.id = id;
        this.casa = casa;
        this.alarma = alarma;
        this.creacion = creacion;
        this.creadoBy = creadoBy;
    }

    public Alerta() {
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

    public String getCreadoBy() {
        return creadoBy;
    }

    public void setCreadoBy(String creadoBy) {
        this.creadoBy = creadoBy;
    }


    @Override
    public String toString() {
        return "Alerta{" +
                "id='" + id + '\'' +
                ", casa='" + casa + '\'' +
                ", alarma='" + alarma + '\'' +
                '}';
    }
}
