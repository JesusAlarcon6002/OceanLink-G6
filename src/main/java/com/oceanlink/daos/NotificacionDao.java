package com.oceanlink.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de apoyo para armar los correos: a quién avisar y qué servicios/clientes están involucrados. */
public class NotificacionDao extends DaoBase {

    /** Un servicio y su cliente (con el correo del cliente, para avisarle directamente). */
    public static class ServicioAfectado {
        public final String servicio;
        public final String cliente;
        public final double capacidadGbps;
        public final String estadoServicio;
        public final String correoCliente;
        public final boolean clienteActivo;

        public ServicioAfectado(String servicio, String cliente, double capacidadGbps,
                                String estadoServicio, String correoCliente, boolean clienteActivo) {
            this.servicio = servicio;
            this.cliente = cliente;
            this.capacidadGbps = capacidadGbps;
            this.estadoServicio = estadoServicio;
            this.correoCliente = correoCliente;
            this.clienteActivo = clienteActivo;
        }
    }

    /** Un segmento con su utilización. */
    public static class SegmentoUso {
        public final String etiqueta;
        public final double total;
        public final double utilizada;
        public final double reservada;

        public SegmentoUso(String etiqueta, double total, double utilizada, double reservada) {
            this.etiqueta = etiqueta;
            this.total = total;
            this.utilizada = utilizada;
            this.reservada = reservada;
        }

        public double porcentaje() {
            return total > 0 ? (utilizada + reservada) / total * 100.0 : 0;
        }
    }

    /** Correos de los usuarios ACTIVOS que tienen alguno de estos roles (nombre exacto de roles.nombres_rol). */
    public List<String> correosPorRoles(String... roles) {
        List<String> correos = new ArrayList<>();
        if (roles == null || roles.length == 0) {
            return correos;
        }
        StringBuilder marcas = new StringBuilder();
        for (int i = 0; i < roles.length; i++) {
            marcas.append(i == 0 ? "?" : ",?");
        }
        String sql = "SELECT DISTINCT u.correo FROM usuarios u JOIN roles r ON r.id_roles = u.roles_idroles " +
                "WHERE u.estado = 'Activo' AND u.correo IS NOT NULL AND u.correo <> '' AND r.nombres_rol IN (" + marcas + ")";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < roles.length; i++) {
                pstmt.setString(i + 1, roles[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    correos.add(rs.getString(1));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return correos;
    }

    /** Correos de todos los clientes ACTIVOS que tienen correo. */
    public List<String> correosClientesActivos() {
        List<String> correos = new ArrayList<>();
        String sql = "SELECT DISTINCT correos FROM clientes WHERE estado = 'Activo' AND correos IS NOT NULL AND correos <> ''";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                correos.add(rs.getString(1));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return correos;
    }

    /** Un servicio Activo próximo a vencer, con los datos para avisar a su cliente. */
    public static class ServicioVence {
        public final String servicio;
        public final String cliente;
        public final String correoCliente;
        public final double capacidadGbps;
        public final java.time.LocalDate expira;

        public ServicioVence(String servicio, String cliente, String correoCliente, double capacidadGbps, java.time.LocalDate expira) {
            this.servicio = servicio;
            this.cliente = cliente;
            this.correoCliente = correoCliente;
            this.capacidadGbps = capacidadGbps;
            this.expira = expira;
        }
    }

    /** Servicios Activos que vencen exactamente dentro de "dias" días (clientes Activos; el correo puede ser null). */
    public List<ServicioVence> serviciosPorVencer(int dias) {
        List<ServicioVence> lista = new ArrayList<>();
        String sql = "SELECT s.etiqueta_servicio, c.nombres, c.correos, s.capacidad_contratada_gbps, s.fecha_expiracion " +
                "FROM servicios s JOIN clientes c ON c.id_clientes = s.id_cliente " +
                "WHERE s.estado = 'Activo' AND c.estado = 'Activo' AND s.fecha_expiracion = DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY s.etiqueta_servicio";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dias);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ServicioVence(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4),
                            rs.getDate(5).toLocalDate()));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Cliente (solo id, nombre y correo) si está Activo y tiene correo; null si no. */
    public com.oceanlink.beans.Clientes clienteActivoConCorreo(int idCliente) {
        if (idCliente <= 0) {
            return null;
        }
        String sql = "SELECT id_clientes, nombres, correos FROM clientes WHERE id_clientes = ? AND estado = 'Activo' AND correos IS NOT NULL AND correos <> ''";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                com.oceanlink.beans.Clientes c = new com.oceanlink.beans.Clientes();
                c.setIdCliente(rs.getInt(1));
                c.setNombre(rs.getString(2));
                c.setCorreo(rs.getString(3));
                return c;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Correo del cliente si está Activo y tiene correo; null si no. */
    public String correoClienteActivo(int idCliente) {
        if (idCliente <= 0) {
            return null;
        }
        String sql = "SELECT correos FROM clientes WHERE id_clientes = ? AND estado = 'Activo' AND correos IS NOT NULL AND correos <> ''";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Servicios (Activos o Pendientes) cuya ruta pasa por la infraestructura indicada.
     * tipoInfra: "Segmento" o "Estación" (como lo guardan los formularios de incidencias y mantenimientos).
     */
    public List<ServicioAfectado> serviciosAfectados(String tipoInfra, int idInfra) {
        List<ServicioAfectado> lista = new ArrayList<>();
        if (tipoInfra == null || idInfra <= 0) {
            return lista;
        }
        boolean esSegmento = "Segmento".equals(tipoInfra);
        String sql = "SELECT DISTINCT s.etiqueta_servicio, c.nombres, s.capacidad_contratada_gbps, s.estado, c.correos, c.estado " +
                "FROM servicios s " +
                "JOIN clientes c ON c.id_clientes = s.id_cliente " +
                "JOIN ruta_segmento rs ON rs.id_ruta = s.id_ruta " +
                (esSegmento ? "" : "JOIN segmentos sg ON sg.id_segmentos = rs.id_segmento ") +
                "WHERE s.estado IN ('Activo','Pendiente') AND " +
                (esSegmento ? "rs.id_segmento = ?" : "(sg.id_estacion_origen = ? OR sg.id_estacion_destino = ?)") +
                " ORDER BY s.etiqueta_servicio";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idInfra);
            if (!esSegmento) {
                pstmt.setInt(2, idInfra);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ServicioAfectado(rs.getString(1), rs.getString(2), rs.getDouble(3),
                            rs.getString(4), rs.getString(5), "Activo".equals(rs.getString(6))));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** De los segmentos indicados, los que pasaron del umbral de utilización (en %). */
    public List<SegmentoUso> segmentosSobreUmbral(List<Integer> idsSegmentos, double umbral) {
        List<SegmentoUso> lista = new ArrayList<>();
        if (idsSegmentos == null || idsSegmentos.isEmpty()) {
            return lista;
        }
        StringBuilder marcas = new StringBuilder();
        for (int i = 0; i < idsSegmentos.size(); i++) {
            marcas.append(i == 0 ? "?" : ",?");
        }
        String sql = "SELECT etiqueta_segmentos, capacidad_total_gbps, capacidad_utilizada_gbps, capacidad_reservada_gbps " +
                "FROM segmentos WHERE capacidad_total_gbps > 0 AND id_segmentos IN (" + marcas + ") " +
                "ORDER BY etiqueta_segmentos";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < idsSegmentos.size(); i++) {
                pstmt.setInt(i + 1, idsSegmentos.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SegmentoUso s = new SegmentoUso(rs.getString(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4));
                    if (s.porcentaje() > umbral) {
                        lista.add(s);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
