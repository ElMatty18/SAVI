package com.tangorra.matias.savi.Entidades;

import com.tangorra.matias.savi.Utils.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Alerta implements Serializable {

    private String id;

    private String dirigida;
    private String dirigidaId;

    private String alarma;

    private Date creacion;
    private String creadoBy;
    private String creadoById;

    private String respuesta;
    private String respuestaAutomatica;

    private String estado;

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getDirigidaId() {
        return dirigidaId;
    }

    public void setDirigidaId(String dirigidaId) {
        this.dirigidaId = dirigidaId;
    }

    @Override
    public String toString() {
        return "Alerta{" +
                "id='" + id + '\'' +
                ", dirigida='" + dirigida + '\'' +
                ", alarma='" + alarma + '\'' +
                '}';
    }

    public int obtenerNivelAlerta(){
        if (this.getRespuestas() != null){
            int cantidadRespestas = this.getRespuestas().size();
            int cantidadOk = 0;
            int cantidadNoOk = 0;
            int cantidadNoAplica = 0;
            for (RespuestaAlerta itemRespuesta : this.getRespuestas() ) {
                if (itemRespuesta.getRespuestaAutomatica() != null){
                    cantidadNoAplica++;
                } else {
                    if (itemRespuesta.getRespuesta() != null){
                        if (itemRespuesta.getRespuesta().equals(StringUtils.respuesta_confirma)){
                            cantidadNoOk++;
                        } else {
                            if (itemRespuesta.getRespuesta().equals(StringUtils.respuesta_cancela)){
                                cantidadOk++;
                            }
                        }
                    }
                    else {
                        cantidadNoAplica++;
                    }
                }
            }

            if ((cantidadOk + cantidadNoAplica)<cantidadNoOk){
                return 100;
            } else {
                if (cantidadRespestas != 0){
                    return 100 -(((cantidadOk + cantidadNoAplica - cantidadNoOk) / cantidadRespestas) * 100);
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }

    }

    public String getCreadoById() {
        return creadoById;
    }

    public void setCreadoById(String creadoById) {
        this.creadoById = creadoById;
    }
}
