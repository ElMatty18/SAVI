package com.tangorra.matias.savi.Entidades;

import com.google.firebase.database.Exclude;
import com.tangorra.matias.savi.Utils.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

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

    // Respuestas indexadas por uid: cada vecino escribe solo la suya (alertas/{id}/respuestas/{uid}).
    // Las alertas viejas guardaban una lista; Firebase la lee igual como mapa {"0": ..., "1": ...}.
    private HashMap<String, RespuestaAlerta> respuestas;

    // Grupo al que pertenece; no se guarda (esta implicito en la ruta) pero se completa al leerla
    private transient String idGrupo;

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

    public HashMap<String, RespuestaAlerta> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(HashMap<String, RespuestaAlerta> respuestas) {
        this.respuestas = respuestas;
    }

    @Exclude
    public String getIdGrupo() {
        return idGrupo;
    }

    @Exclude
    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    /** Si la alerta fue emitida por o dirigida a alguno de estos usuarios. */
    @Exclude
    public boolean involucraA(java.util.Set<String> idsUsuarios) {
        return idsUsuarios.contains(creadoById) || idsUsuarios.contains(dirigidaId);
    }

    @Exclude
    public Collection<RespuestaAlerta> listaRespuestas() {
        if (respuestas == null) {
            return Collections.emptyList();
        }
        return respuestas.values();
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

    /**
     * Nivel de riesgo entre 0 y 100 segun las respuestas de los vecinos: sube con cada confirmacion
     * y baja con las cancelaciones o respuestas automaticas.
     */
    @Exclude
    public int obtenerNivelAlerta(){
        Collection<RespuestaAlerta> lista = listaRespuestas();
        if (lista.isEmpty()){
            return 0;
        }
        int confirman = 0;
        int noConfirman = 0;
        for (RespuestaAlerta itemRespuesta : lista) {
            if (itemRespuesta == null){
                continue;
            }
            if (itemRespuesta.getRespuestaAutomatica() == null && StringUtils.respuesta_confirma.equals(itemRespuesta.getRespuesta())){
                confirman++;
            } else {
                noConfirman++;
            }
        }
        if (noConfirman < confirman){
            return 100;
        }
        // Formula original: 100 - (noConfirman - confirman) / total * 100 = 200 * confirman / total.
        // Antes se calculaba con division entera y el resultado era siempre 0 o 100.
        int total = confirman + noConfirman;
        return (int) Math.round(200.0 * confirman / total);
    }

    public String getCreadoById() {
        return creadoById;
    }

    public void setCreadoById(String creadoById) {
        this.creadoById = creadoById;
    }
}
