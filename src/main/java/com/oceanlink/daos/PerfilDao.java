package com.oceanlink.daos;

import com.oceanlink.util.Seguridad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Datos de la cuenta de la persona que inició sesión (pantallas Perfil y Configuración).
 * Todo con PreparedStatement y siempre filtrado por el id de la sesión: nadie toca la cuenta de otro.
 */
public class PerfilDao extends DaoBase {

    public static class Perfil {
        public int id;
        public String nombre, apellido, correo, telefono, etiqueta, estado, rol;
    }

    public Perfil obtener(int idUsuario) {
        String sql = "SELECT u.idusuarios, u.nombre, u.apellido, u.correo, u.telefono, u.etiqueta_usuario, u.estado, r.nombres_rol "
                + "FROM usuarios u JOIN roles r ON r.id_roles = u.roles_idroles WHERE u.idusuarios = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Perfil p = new Perfil();
                p.id = rs.getInt("idusuarios");
                p.nombre = rs.getString("nombre");
                p.apellido = rs.getString("apellido");
                p.correo = rs.getString("correo");
                p.telefono = rs.getString("telefono");
                p.etiqueta = rs.getString("etiqueta_usuario");
                p.estado = rs.getString("estado");
                p.rol = rs.getString("nombres_rol");
                return p;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Actualiza solo nombre, apellido y teléfono (el correo y el rol los maneja el administrador). */
    public boolean actualizarDatos(int idUsuario, String nombre, String apellido, String telefono) {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, telefono = ? WHERE idusuarios = ? AND estado = 'Activo'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            if (telefono == null || telefono.isEmpty()) ps.setNull(3, java.sql.Types.VARCHAR); else ps.setString(3, telefono);
            ps.setInt(4, idUsuario);
            return ps.executeUpdate() == 1;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** true si la clave actual escrita coincide con la guardada. */
    public boolean claveActualCorrecta(int idUsuario, String claveActual) {
        String sql = "SELECT password_hash FROM usuarios WHERE idusuarios = ? AND estado = 'Activo'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && Seguridad.verificar(claveActual, rs.getString(1));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarClave(int idUsuario, String claveNueva) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE idusuarios = ? AND estado = 'Activo'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Seguridad.hash(claveNueva));
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() == 1;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
