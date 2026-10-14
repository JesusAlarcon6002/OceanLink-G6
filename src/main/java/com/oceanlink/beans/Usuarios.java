package com.oceanlink.beans;

public class Usuarios {
    private int idUsuario;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String passwordHash;
    private String estado;
    private int idRoles;
    private String etiquetaUsuario;

    private String nombreCompleto;

    private String nombreRoles;

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdroles() {
        return idRoles;
    }

    public void setIdroles(int idRoles) {
        this.idRoles= idRoles;
    }

    public String getNombreRoles() {
        return nombreRoles;
    }

    public void setNombreRoles(String nombreRoles) {
        this.nombreRoles = nombreRoles;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEtiquetaUsuario() {
        return etiquetaUsuario;
    }

    public void setEtiquetaUsuario(String etiquetaUsuario) {
        this.etiquetaUsuario = etiquetaUsuario;
    }


}
