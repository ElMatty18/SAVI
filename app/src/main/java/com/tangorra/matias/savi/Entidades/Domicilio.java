package com.tangorra.matias.savi.Entidades;


import java.io.Serializable;

public class Domicilio  implements Serializable {

    private double lat = 0.0;
    private double lng = 0.0;

    public Domicilio() {
    }

    public Domicilio(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
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

    /** Los domicilios sin cargar quedan en (0, 0). */
    public static boolean estaCargado(Domicilio domicilio) {
        return domicilio != null && (domicilio.lat != 0 || domicilio.lng != 0);
    }
}
