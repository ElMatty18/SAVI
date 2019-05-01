package com.tangorra.matias.savi.Entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Alerta implements Serializable {

    private String id;

    private String dirigida;
    private String alarma;

    private Date creacion;
    private String creadoBy;

    private String respuesta;
    private String respuestaAutomatica;

    private List<RespuestaVisto> vistoPor;
    private List<RespuestaAlerta> respuestas;

    public Alerta(String id, String casa, String alarma, Date creacion, String creadoBy) {
        this.id = id;
        this.dirigida = casa;
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

    public String getDirigida() {
        return dirigida;
    }

    public void setDirigida(String dirigida) {
        this.dirigida = dirigida;
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

    public List<RespuestaVisto> getVistoPor() {
        return vistoPor;
    }

    public void setVistoPor(List<RespuestaVisto> vistoPor) {
        this.vistoPor = vistoPor;
    }

    public String getCreadoBy() {
        return creadoBy;
    }

    public void setCreadoBy(String creadoBy) {
        this.creadoBy = creadoBy;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getRespuestaAutomatica() {
        return respuestaAutomatica;
    }

    public void setRespuestaAutomatica(String respuestaAutomatica) {
        this.respuestaAutomatica = respuestaAutomatica;
    }

    public List<RespuestaAlerta> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<RespuestaAlerta> respuestas) {
        this.respuestas = respuestas;
    }

    @Override
    public String toString() {
        return "Alerta{" +
                "id='" + id + '\'' +
                ", dirigida='" + dirigida + '\'' +
                ", alarma='" + alarma + '\'' +
                '}';
    }
}
