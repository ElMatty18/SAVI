package com.tangorra.matias.savi.Entidades;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Usuario implements Serializable {

    private String id;
    private String idFamilia;

    private String mail;

    private String nombre;

    private String apellido;
    private String fijo;
    private String celular;
    private String dni;
    private Date fechaNacimiento;

    private String idGrupo;
    private Grupo grupo;

    private Configuracion configuracion;

    // Token de FCM del ultimo dispositivo donde inicio sesion (lo usan las Cloud Functions)
    private String fcmToken;

    // Solo para mostrar en pantalla: no se guarda dentro del usuario
    @Exclude
    public Grupo getGrupo() {
        return grupo;
    }

    @Exclude
    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    private Estado estado;

    private PerfilUsuario perfil = new PerfilUsuario();

    public Usuario(String id,String mail) {
        this.id =id ;
        this.mail = mail;
    }

    public Usuario() {
    }

    public String getFijo() {
        return fijo;
    }

    public void setFijo(String fijo) {
        this.fijo = fijo;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Exclude
    public Boolean datosIncompletos(){
        // El telefono fijo es opcional: muchos vecinos ya no tienen
        if (vacio(nombre) || vacio(apellido) || vacio(dni) || vacio(celular)){
            return true;
        }
        return false;
    }

    /** Nombre y apellido con mayusculas, como se muestra (y se guarda en "dirigida" de las alertas). */
    @Exclude
    public String getGlosaFormateada(){
        return com.tangorra.matias.savi.Utils.StringUtils.getTextoFormateado(getGlosa());
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    @Exclude
    public String getGlosa(){
        return getNombre() + " " + getApellido();
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getIdFamilia() {
        return idFamilia;
    }

    public void setIdFamilia(String idFamilia) {
        this.idFamilia = idFamilia;
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
