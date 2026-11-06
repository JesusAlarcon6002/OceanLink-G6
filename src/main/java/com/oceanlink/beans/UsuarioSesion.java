package com.oceanlink.beans;

import com.oceanlink.util.Rol;

import java.io.Serializable;

/**
 * Lo único que se guarda en la sesión HTTP del usuario que inició sesión.
 * A propósito NO tiene contraseña ni hash.
 */
public class UsuarioSesion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int idUsuario;
    private final String nombreCompleto;
    private final String correo;
    private final Rol rol;

    public UsuarioSesion(int idUsuario, String nombreCompleto, String correo, Rol rol) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }

    public String getCorreo() { return correo; }

    public Rol getRol() { return rol; }
}
