package com.tangorra.matias.savi.Entidades;

import java.util.Date;

public class RespuestaAlerta {

    private String idUsuario;
    private String nombreUsuario;
    private String apellidoUsuario;
    private Date creacion;
    private String idAlarma;
    private String respuesta;
    private String respuestaAutomatica;

    public RespuestaAlerta() {
    }

    public RespuestaAlerta(String idUsuario, String nombreUsuario, String apellidoUsuario, Date creacion, String idAlarma, String respuesta, String respuestaAutomatica) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
        this.creacion = creacion;
        this.idAlarma = idAlarma;
        this.respuesta = respuesta;
        this.respuestaAutomatica = respuestaAutomatica;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Date getCreacion() {
        return creacion;
    }

    public void setCreacion(Date creacion) {
        this.creacion = creacion;
    }

    public String getIdAlarma() {
        return idAlarma;
    }

    public void setIdAlarma(String idAlarma) {
        this.idAlarma = idAlarma;
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }


    public String getGlosa(){
        return getNombreUsuario() + " " + getApellidoUsuario();
    }
}
