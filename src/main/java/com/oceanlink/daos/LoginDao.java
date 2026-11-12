package com.oceanlink.daos;

import com.oceanlink.beans.UsuarioSesion;
import com.oceanlink.util.Rol;
import com.oceanlink.util.Seguridad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Valida las credenciales contra la tabla usuarios (junto con roles).
 * Todo con PreparedStatement. Solo entran usuarios con estado 'Activo'.
 */
public class LoginDao extends DaoBase {

    /**
     * @return el usuario listo para guardar en sesión, o null si el correo/contraseña no son válidos,
     *         el usuario está inactivo, o su rol no es uno de los 5 conocidos.
     */
    public UsuarioSesion autenticar(String correo, String clave) {
        if (correo == null || clave == null || correo.trim().isEmpty() || clave.isEmpty()) return null;

        String sql = "SELECT u.idusuarios, u.nombre, u.apellido, u.correo, u.password_hash, r.nombres_rol "
                + "FROM usuarios u JOIN roles r ON r.id_roles = u.roles_idroles "
                + "WHERE u.correo = ? AND u.estado = 'Activo'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String guardado = rs.getString("password_hash");
                if (!Seguridad.verificar(clave, guardado)) return null;
                Rol rol = Rol.porNombreBd(rs.getString("nombres_rol"));
                if (rol == null) return null;
                int id = rs.getInt("idusuarios");
                // Contraseña antigua en texto plano: la convertimos a hash en este primer ingreso correcto
                if (!Seguridad.esHash(guardado)) {
                    actualizarHash(conn, id, clave);
                }
                String nombre = rs.getString("nombre") + " " + rs.getString("apellido");
                return new UsuarioSesion(id, nombre.trim(), rs.getString("correo"), rol);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void actualizarHash(Connection conn, int id, String clave) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE usuarios SET password_hash = ? WHERE idusuarios = ?")) {
            ps.setString(1, Seguridad.hash(clave));
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // si falla, el login igual sigue siendo válido
        }
    }
}
