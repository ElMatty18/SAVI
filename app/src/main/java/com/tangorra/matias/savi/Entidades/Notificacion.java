package com.tangorra.matias.savi.Entidades;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class Notificacion {

    private String id;
    private double lat = 0.0;
    private double lng = 0.0;

    private String contenido;
    private String title;
    private String creadoBy;
    private Integer rango;
    // Las notificaciones viejas no tienen fecha
    private java.util.Date creacion;

    // uid -> uid de quienes ya la vieron (vistoPor/{uid}). Las listas viejas se leen como {"0": uid}.
    private HashMap<String, String> vistoPor;

    public Notificacion() {

    }

    public String getCreadoBy() {
        return creadoBy;
    }

    public void setCreadoBy(String creadoBy) {
        this.creadoBy = creadoBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public HashMap<String, String> getVistoPor() {
        return vistoPor;
    }

    public void setVistoPor(HashMap<String, String> vistoPor) {
        this.vistoPor = vistoPor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRango() {
        return rango;
    }

    public void setRango(Integer rango) {
        this.rango = rango;
    }

    @Exclude
    public boolean vistaPor(String idUsuario) {
        return vistoPor != null && (vistoPor.containsKey(idUsuario) || vistoPor.containsValue(idUsuario));
    }

    public java.util.Date getCreacion() {
        return creacion;
    }

    public void setCreacion(java.util.Date creacion) {
        this.creacion = creacion;
    }
}
