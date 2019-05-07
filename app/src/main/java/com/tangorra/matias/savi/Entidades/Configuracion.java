package com.tangorra.matias.savi.Entidades;

import java.io.Serializable;
import java.util.Date;

public class Configuracion implements Serializable {

    private boolean configuracionActiva;
    private String configuracionSeleccionada;

    private String mensaje;

    //vacaciones
    private Date inicioVacaciones;
    private Date finVacaciones;

    //ausenciaDia
    private Date ausenciaDia;

    //visitas
    private Boolean visitasCasa;

    //noMolestarHora
    private Date noMolestar;

    //ignorarTodo
    private Boolean ignorarTodo;

    public Configuracion() {
    }

    public void activarConfiguracion(String tipoConfiguracion, String mensaje){
        this.configuracionActiva = true;
        this.configuracionSeleccionada = tipoConfiguracion;
        this.mensaje = mensaje;
    }



    public boolean validarVacaciones(Date fecha) {
        if ((inicioVacaciones != null) && (finVacaciones != null)){
            return fechaRango(inicioVacaciones, new Date(),finVacaciones);
        }
        return false;
    }

    private boolean fechaRango(Date inicioVacaciones, Date actual, Date finVacaciones) {
        if ( inicioVacaciones.before(actual) && actual.before(finVacaciones) ){
            return true;
        }
        return false;
    }

    public boolean isConfiguracionActiva() {
        return configuracionActiva;
    }

    public void setConfiguracionActiva(boolean configuracionActiva) {
        this.configuracionActiva = configuracionActiva;
    }

    public String getConfiguracionSeleccionada() {
        return configuracionSeleccionada;
    }

    public void setConfiguracionSeleccionada(String configuracionSeleccionada) {
        this.configuracionSeleccionada = configuracionSeleccionada;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Date getInicioVacaciones() {
        return inicioVacaciones;
    }

    public void setInicioVacaciones(Date inicioVacaciones) {
        this.inicioVacaciones = inicioVacaciones;
    }

    public Date getFinVacaciones() {
        return finVacaciones;
    }

    public void setFinVacaciones(Date finVacaciones) {
        this.finVacaciones = finVacaciones;
    }

    public Date getAusenciaDia() {
        return ausenciaDia;
    }

    public void setAusenciaDia(Date ausenciaDia) {
        this.ausenciaDia = ausenciaDia;
    }

    public Date getNoMolestar() {
        return noMolestar;
    }

    public void setNoMolestar(Date noMolestar) {
        this.noMolestar = noMolestar;
    }


    public Boolean getVisitasCasa() {
        return visitasCasa;
    }

    public void setVisitasCasa(Boolean visitasCasa) {
        this.visitasCasa = visitasCasa;
    }

    public Boolean getIgnorarTodo() {
        return ignorarTodo;
    }

    public void setIgnorarTodo(Boolean ignorarTodo) {
        this.ignorarTodo = ignorarTodo;
    }
}
