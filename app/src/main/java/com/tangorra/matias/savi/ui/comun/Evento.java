package com.tangorra.matias.savi.ui.comun;

/** Valor que se consume una sola vez (mensajes, navegacion), para no repetirlo al rotar la pantalla. */
public final class Evento<T> {

    private final T contenido;
    private boolean usado;

    public Evento(T contenido) {
        this.contenido = contenido;
    }

    public T consumir() {
        if (usado) {
            return null;
        }
        usado = true;
        return contenido;
    }
}
