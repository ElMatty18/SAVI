package com.tangorra.matias.savi.Entidades;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

class Estado implements Serializable {

    public boolean tieneSenal(){
        return true;
    }

    public LatLng obtenerUbicacion(){
        LatLng coordenadas = new LatLng(1, 1); //Si se puede recuperar, la ubicacion actual, sino null
        return coordenadas;
    }

}


