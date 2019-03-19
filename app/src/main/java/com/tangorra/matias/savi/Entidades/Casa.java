package com.tangorra.matias.savi.Entidades;

public class Casa {

    private int imgFoto;
    private String titulo;
    private String contenido;

    public Casa(int imgFoto, String titulo, String contenido) {
        this.imgFoto = imgFoto;
        this.titulo = titulo;
        this.contenido = contenido;
    }

    public int getImgFoto() {
        return imgFoto;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getContenido() {
        return contenido;
    }
}
