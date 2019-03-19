package com.tangorra.matias.savi.Entidades;

public class SesionManager {
    private static final SesionManager ourInstance = new SesionManager();

    public static SesionManager getInstance() {
        return ourInstance;
    }

    private static Grupo grupo;
    private static Usuario usuario;


    private SesionManager() {
    }

    public static Grupo getGrupo() {
        return grupo;
    }

    public static void setGrupo(Grupo grupo) {
        SesionManager.grupo = grupo;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        SesionManager.usuario = usuario;
    }

    public static void clean() {
        grupo = new Grupo("sin nombre");
        usuario = new Usuario();
    }
}
