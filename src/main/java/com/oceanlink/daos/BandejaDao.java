package com.oceanlink.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Bandeja de la campana: avisos del sistema (copia de los correos) y mensajes entre usuarios. */
public class BandejaDao extends DaoBase {

    public static class Aviso {
        public int id;
        public String tipo, asunto, mensaje, remitente;
        public boolean leida;
        public Timestamp fecha;
    }

    public static class Destinatario {
        public int id;
        public String etiqueta, nombre, rol;
    }

    public static final int MAX_ASUNTO = 150;
    public static final int MAX_MENSAJE = 2000;

    /** Copia en la campana de cada usuario ACTIVO cuyo correo esté en la lista. Nunca lanza excepción. */
    public void avisarPorCorreos(Collection<String> correos, String asunto, String mensaje) {
        if (correos == null || correos.isEmpty()) return;
        String sql = "INSERT INTO notificaciones (id_usuario, tipo, asunto, mensaje, fecha) "
                + "SELECT idusuarios, 'Sistema', ?, ?, NOW() FROM usuarios WHERE estado = 'Activo' AND correo = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            String a = recortar(asunto, 200);
            String m = recortar(mensaje, 4000);
            for (String correo : correos) {
                if (correo == null || correo.trim().isEmpty()) continue;
                ps.setString(1, a);
                ps.setString(2, m);
                ps.setString(3, correo.trim());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[Campana] No se pudo guardar el aviso: " + e.getMessage());
        }
    }

    /** Mensaje de un usuario a otro (activo). @return true si se guardó. */
    public boolean enviarMensaje(int idRemitente, int idDestino, String asunto, String mensaje) {
        String sql = "INSERT INTO notificaciones (id_usuario, tipo, asunto, mensaje, id_remitente, fecha) "
                + "SELECT idusuarios, 'Mensaje', ?, ?, ?, NOW() FROM usuarios WHERE idusuarios = ? AND estado = 'Activo'";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, recortar(asunto, MAX_ASUNTO));
            ps.setString(2, recortar(mensaje, MAX_MENSAJE));
            ps.setInt(3, idRemitente);
            ps.setInt(4, idDestino);
            return ps.executeUpdate() == 1;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Aviso> listar(int idUsuario, int limite) {
        List<Aviso> lista = new ArrayList<>();
        String sql = "SELECT n.id_notificacion, n.tipo, n.asunto, n.mensaje, n.leida, n.fecha, "
                + "CONCAT(r.nombre,' ',r.apellido) FROM notificaciones n "
                + "LEFT JOIN usuarios r ON r.idusuarios = n.id_remitente "
                + "WHERE n.id_usuario = ? ORDER BY n.fecha DESC, n.id_notificacion DESC LIMIT ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Aviso a = new Aviso();
                    a.id = rs.getInt(1);
                    a.tipo = rs.getString(2);
                    a.asunto = rs.getString(3);
                    a.mensaje = rs.getString(4);
                    a.leida = rs.getInt(5) == 1;
                    a.fecha = rs.getTimestamp(6);
                    a.remitente = rs.getString(7);
                    lista.add(a);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int contarNoLeidas(int idUsuario) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM notificaciones WHERE id_usuario = ? AND leida = 0")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (ClassNotFoundException | SQLException e) {
            return 0;
        }
    }

    /** Solo marca si la notificación es del usuario que lo pide. */
    public void marcarLeida(int idUsuario, int idNotificacion) {
        ejecutar("UPDATE notificaciones SET leida = 1 WHERE id_usuario = ? AND id_notificacion = ?", idUsuario, idNotificacion);
    }

    public void marcarTodas(int idUsuario) {
        ejecutar("UPDATE notificaciones SET leida = 1 WHERE id_usuario = ? AND leida = 0", idUsuario, -1);
    }

    private void ejecutar(String sql, int a, int b) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a);
            if (b >= 0) ps.setInt(2, b);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /** Usuarios activos a quienes se les puede escribir (todos menos uno mismo). */
    public List<Destinatario> destinatarios(int idExcluir) {
        List<Destinatario> lista = new ArrayList<>();
        String sql = "SELECT u.idusuarios, u.etiqueta_usuario, CONCAT(u.nombre,' ',u.apellido), r.nombres_rol "
                + "FROM usuarios u JOIN roles r ON r.id_roles = u.roles_idroles "
                + "WHERE u.estado = 'Activo' AND u.idusuarios <> ? ORDER BY u.nombre, u.apellido";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Destinatario d = new Destinatario();
                    d.id = rs.getInt(1);
                    d.etiqueta = rs.getString(2);
                    d.nombre = rs.getString(3);
                    d.rol = rs.getString(4);
                    lista.add(d);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Correo y nombre de un usuario activo (para la copia por correo del mensaje). {correo, nombre} o null. */
    public String[] correoYNombre(int idUsuario) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT correo, CONCAT(nombre,' ',apellido) FROM usuarios WHERE idusuarios = ? AND estado = 'Activo'")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[]{rs.getString(1), rs.getString(2)} : null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static String recortar(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}
