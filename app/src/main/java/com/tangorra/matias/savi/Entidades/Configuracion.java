package com.tangorra.matias.savi.Entidades;

import com.tangorra.matias.savi.Utils.DateUtils;
import com.tangorra.matias.savi.Utils.StringUtils;

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



    /**
     * Si la respuesta automatica aplica en este momento. Antes solo se miraba si estaba activa,
     * asi que por ejemplo el modo vacaciones seguia respondiendo despues de volver.
     */
    public boolean vigente(Date ahora) {
        if (!configuracionActiva || configuracionSeleccionada == null) {
            return false;
        }
        if (configuracionSeleccionada.equals(StringUtils.config_vacaciones)) {
            Date inicio = DateUtils.normalizar(inicioVacaciones);
            Date fin = DateUtils.normalizar(finVacaciones);
            return inicio != null && fin != null
                    && !ahora.before(DateUtils.inicioDelDia(inicio)) && ahora.before(DateUtils.finDelDia(fin));
        } else if (configuracionSeleccionada.equals(StringUtils.config_casaSola)) {
            Date dia = DateUtils.normalizar(ausenciaDia);
            return dia != null && !ahora.before(DateUtils.inicioDelDia(dia)) && ahora.before(DateUtils.finDelDia(dia));
        } else if (configuracionSeleccionada.equals(StringUtils.config_noMolestar)) {
            return noMolestar != null && ahora.before(noMolestar);
        }
        // Visitas en casa e ignorar todo: rigen hasta que el usuario las desactive
        return true;
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
