package com.oceanlink.daos;

import com.oceanlink.util.Seguridad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Recuperar contraseña y solicitudes de registro (todo con PreparedStatement).
 * Las solicitudes guardan la contraseña ya con hash: nunca en texto plano.
 */
public class CuentasDao extends DaoBase {

    /** Una fila de solicitudes_registro. */
    public static class Solicitud {
        public int id;
        public String nombre, apellido, correo, telefono, estado, motivoRechazo;
        public Timestamp fecha, fechaResolucion;
    }

    // ------------------------------------------------------------------ recuperar contraseña

    /** @return nombre del usuario ACTIVO con ese correo, o null si no existe / está inactivo. */
    public String nombreUsuarioActivo(String correo) {
        if (correo == null) return null;
        String sql = "SELECT nombre FROM usuarios WHERE correo = ? AND estado = 'Activo'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Cambia la clave (se guarda el hash). @return true si se actualizó un usuario activo. */
    public boolean cambiarClavePorCorreo(String correo, String claveNueva) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE correo = ? AND estado = 'Activo'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Seguridad.hash(claveNueva));
            ps.setString(2, correo.trim());
            return ps.executeUpdate() == 1;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ solicitudes de registro

    public boolean correoEnUso(String correo) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT 1 FROM usuarios WHERE correo = ?")) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return true; // ante la duda no se deja registrar
        }
    }

    public boolean hayPendiente(String correo) {
        String sql = "SELECT 1 FROM solicitudes_registro WHERE correo = ? AND estado = 'Pendiente'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public int contarPendientes() {
        try (Connection c = getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM solicitudes_registro WHERE estado = 'Pendiente'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** @return null si se guardó; si no, mensaje de error. */
    public String crearSolicitud(String nombre, String apellido, String correo, String telefono, String clavePlano) {
        return crearSolicitudConHash(nombre, apellido, correo, telefono, Seguridad.hash(clavePlano));
    }

    /** @return null si se guardó; si no, mensaje de error. */
    public String crearSolicitudConHash(String nombre, String apellido, String correo, String telefono, String hash) {
        if (correoEnUso(correo)) return "Ese correo ya tiene una cuenta. Si olvidaste la contraseña, usa \"Olvidé mi contraseña\".";
        if (hayPendiente(correo)) return "Ya hay una solicitud pendiente con ese correo. Espera a que un administrador la revise.";
        String sql = "INSERT INTO solicitudes_registro (nombre, apellido, correo, telefono, password_hash, estado, fecha_solicitud) "
                + "VALUES (?,?,?,?,?,'Pendiente',NOW())";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, correo.trim());
            ps.setString(4, telefono == null || telefono.isEmpty() ? null : telefono);
            ps.setString(5, hash);
            ps.executeUpdate();
            return null;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "No se pudo guardar la solicitud por un error del sistema. Inténtalo de nuevo.";
        }
    }

    /** Pendientes primero, luego las últimas resueltas. */
    public List<Solicitud> listar() {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT id_solicitud_registro, nombre, apellido, correo, telefono, estado, motivo_rechazo, fecha_solicitud, fecha_resolucion "
                + "FROM solicitudes_registro ORDER BY (estado = 'Pendiente') DESC, fecha_solicitud DESC LIMIT 200";
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Solicitud obtener(int id) {
        String sql = "SELECT id_solicitud_registro, nombre, apellido, correo, telefono, estado, motivo_rechazo, fecha_solicitud, fecha_resolucion "
                + "FROM solicitudes_registro WHERE id_solicitud_registro = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.id = rs.getInt(1);
        s.nombre = rs.getString(2);
        s.apellido = rs.getString(3);
        s.correo = rs.getString(4);
        s.telefono = rs.getString(5);
        s.estado = rs.getString(6);
        s.motivoRechazo = rs.getString(7);
        s.fecha = rs.getTimestamp(8);
        s.fechaResolucion = rs.getTimestamp(9);
        return s;
    }

    /**
     * Acepta la solicitud: crea el usuario (con el hash que eligió quien se registró) en una sola transacción.
     * @return null si salió bien; si no, mensaje de error.
     */
    public String aprobar(int idSolicitud, int idRol, int idAdmin) {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                String correo, nombre, apellido, telefono, hash;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT nombre, apellido, correo, telefono, password_hash, estado FROM solicitudes_registro "
                                + "WHERE id_solicitud_registro = ? FOR UPDATE")) {
                    ps.setInt(1, idSolicitud);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) return "La solicitud no existe.";
                        if (!"Pendiente".equals(rs.getString(6))) return "Esa solicitud ya fue resuelta.";
                        nombre = rs.getString(1);
                        apellido = rs.getString(2);
                        correo = rs.getString(3);
                        telefono = rs.getString(4);
                        hash = rs.getString(5);
                    }
                }
                String etiqueta = etiquetaLibre(c, correo);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO usuarios (etiqueta_usuario, nombre, apellido, correo, password_hash, telefono, roles_idroles) VALUES (?,?,?,?,?,?,?)")) {
                    ps.setString(1, etiqueta);
                    ps.setString(2, nombre);
                    ps.setString(3, apellido);
                    ps.setString(4, correo);
                    ps.setString(5, hash);
                    ps.setString(6, telefono);
                    ps.setInt(7, idRol);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE solicitudes_registro SET estado='Aprobada', fecha_resolucion=NOW(), id_admin_resuelve=? WHERE id_solicitud_registro=?")) {
                    ps.setInt(1, idAdmin);
                    ps.setInt(2, idSolicitud);
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLIntegrityConstraintViolationException e) {
                c.rollback();
                return "No se pudo crear la cuenta: ese correo ya existe o el rol no es válido.";
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "No se pudo aprobar por un error del sistema.";
        }
    }

    /** @return true si se rechazó una solicitud que estaba pendiente. */
    public boolean rechazar(int idSolicitud, int idAdmin, String motivo) {
        String sql = "UPDATE solicitudes_registro SET estado='Rechazada', fecha_resolucion=NOW(), id_admin_resuelve=?, motivo_rechazo=? "
                + "WHERE id_solicitud_registro=? AND estado='Pendiente'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAdmin);
            ps.setString(2, motivo == null || motivo.isEmpty() ? null : motivo);
            ps.setInt(3, idSolicitud);
            return ps.executeUpdate() == 1;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Usuario (etiqueta) a partir de la parte local del correo; si está ocupado se le agrega un número. */
    static String etiquetaLibre(Connection c, String correo) throws SQLException {
        String base = etiquetaBase(correo);
        for (int n = 0; n < 1000; n++) {
            String candidata = n == 0 ? base : base + n;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM usuarios WHERE etiqueta_usuario = ?")) {
                ps.setString(1, candidata);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return candidata;
                }
            }
        }
        return base + System.currentTimeMillis() % 100000;
    }

    public static String etiquetaBase(String correo) {
        String local = correo == null ? "" : correo.trim().toLowerCase();
        int a = local.indexOf('@');
        if (a >= 0) local = local.substring(0, a);
        local = local.replaceAll("[^a-z0-9._-]", "");
        if (local.length() > 30) local = local.substring(0, 30);
        return local.isEmpty() ? "usuario" : local;
    }
}
