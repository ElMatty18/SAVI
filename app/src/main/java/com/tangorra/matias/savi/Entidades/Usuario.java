package com.tangorra.matias.savi.Entidades;

import android.widget.ImageView;

import java.io.Serializable;
import java.util.Date;

public class Usuario implements Serializable {

    private String id;
    private String idFamilia;

    private String clave;
    private String mail;

    private String nombre;

    private String apellido;
    private String fijo;
    private String celular;
    private String dni;
    private Date fechaNacimiento;

    private String idGrupo;
    private Grupo grupo;

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    private Estado estado;

    private PerfilUsuario perfil = new PerfilUsuario();

    private transient  ImageView imgUsuario;

    public ImageView getImgUsuario() {
        return imgUsuario;
    }

    public void setImgUsuario(ImageView imgUsuario) {
        this.imgUsuario = imgUsuario;
    }

    public Usuario(String id,String mail, String clave) {
        this.id =id ;
        this.mail = mail;
        this.clave = clave;
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

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Boolean datosIncompletos(){
        if (this.nombre == null || this.apellido == null  || this.dni == null || this.celular == null || this.fijo == null){
            return true;
        }
        return false;
    }

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

}
