package com.tangorra.matias.savi.Entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Grupo implements Serializable {

    private String id;
    private String nombre;
    private Integer maxUsuarios;
    private Double lat;
    private Double lng;
    private Integer maxRango;

    private List<Alarma> alarmas;

    private List<Usuario> integrantes;
    private List<Alarma> historial;

    public Grupo() {
    }

    public Grupo(String id, String nombre, Integer maxUsuarios, Double lat, Double lng, Integer maxRango) {
        this.id = id;
        this.nombre = nombre;
        this.maxUsuarios = maxUsuarios;
        this.lat = lat;
        this.lng = lng;
        this.maxRango = maxRango;
        this.alarmas=new ArrayList<Alarma>();
    }


    public Grupo(String nombre) {
        this.nombre = nombre;
    }

    public Integer getMaxUsuarios() {
        return maxUsuarios;
    }

    public void setMaxUsuarios(Integer maxUsuarios) {
        this.maxUsuarios = maxUsuarios;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getMaxRango() {
        return maxRango;
    }

    public void setMaxRango(Integer maxRango) {
        this.maxRango = maxRango;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Usuario> getIntegrantes() {
        return integrantes;
    }

    public void setIntegrantes(List<Usuario> integrantes) {
        this.integrantes = integrantes;
    }

    public List<Alarma> getHistorial() {
        return historial;
    }

    public void setHistorial(List<Alarma> historial) {
        this.historial = historial;
    }
}
