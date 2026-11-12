package com.oceanlink.daos;

import com.oceanlink.util.GrafoRutas;
import com.oceanlink.util.MovimientosCapacidad;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Reservar y liberar capacidad de una solicitud sobre una ruta. Cada operación es UNA transacción:
 * se bloquean la solicitud y los segmentos (FOR UPDATE, en orden de ID), se validan las reglas con los
 * valores ya bloqueados y recién ahí se escribe. Si algo falla, rollback y no queda nada a medias.
 * Los métodos devuelven null si salió bien, o el mensaje de error para mostrar al usuario.
 */
public class ReservasDao extends DaoBase {

    /** Un segmento con capacidad reservada por una solicitud. */
    public static class Reserva {
        public final int idSegmento;
        public final String etiquetaSegmento;
        public final double capacidad;

        public Reserva(int idSegmento, String etiquetaSegmento, double capacidad) {
            this.idSegmento = idSegmento;
            this.etiquetaSegmento = etiquetaSegmento;
            this.capacidad = capacidad;
        }
    }

    private static final DecimalFormat GBPS = new DecimalFormat("#,##0.##");

    /** Segmentos reservados por la solicitud, en el orden de la ruta si es posible (por etiqueta si no). */
    public List<Reserva> obtenerReservas(int idSolicitud) {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT ss.id_segmento, s.etiqueta_segmentos, ss.capacidad_reservada_gbps " +
                "FROM solicitud_segmento ss JOIN segmentos s ON s.id_segmentos = ss.id_segmento " +
                "WHERE ss.id_solicitud = ? ORDER BY s.etiqueta_segmentos";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(new Reserva(rs.getInt(1), rs.getString(2), rs.getDouble(3)));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** true si ya existe un servicio creado desde esa solicitud. */
    public boolean tieneServicio(int idSolicitud) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM servicios WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ reservar

    public String reservar(int idSolicitud, int idRuta) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String error = reservar(conn, idSolicitud, idRuta);
                if (error != null) {
                    conn.rollback();
                    return error;
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "Error de base de datos al reservar. Revisa la consola del servidor.";
        }
    }

    private String reservar(Connection conn, int idSolicitud, int idRuta) throws SQLException {
        // 1) bloquear la solicitud
        String estado;
        int solOrigen, solDestino;
        double capacidad;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT estado, id_estacion_origen, id_estacion_destino, capacidad_solicitada_gbps " +
                "FROM solicitudes_capacidad WHERE idsolicitudes_capacidad = ? FOR UPDATE")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return "La solicitud no existe.";
                estado = rs.getString(1);
                solOrigen = rs.getInt(2);
                solDestino = rs.getInt(3);
                capacidad = rs.getDouble(4);
            }
        }
        if ("Rechazada".equals(estado) || "Provisionada".equals(estado) || "Activa".equals(estado)) {
            return "No se puede reservar: la solicitud está en estado " + estado + ".";
        }
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM solicitud_segmento WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return "La solicitud ya tiene capacidad reservada. Libérala primero si quieres cambiar de ruta.";
            }
        }

        // 2) la ruta
        String estadoRuta;
        int rutaOrigen, rutaDestino;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT estado, id_estacion_origen, id_estacion_destino FROM rutas WHERE idrutas = ?")) {
            pstmt.setInt(1, idRuta);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return "La ruta elegida no existe.";
                estadoRuta = rs.getString(1);
                rutaOrigen = rs.getInt(2);
                rutaDestino = rs.getInt(3);
            }
        }
        if (!"Activa".equals(estadoRuta)) return "La ruta elegida está Inactiva.";
        boolean directa = rutaOrigen == solOrigen && rutaDestino == solDestino;
        boolean inversa = rutaOrigen == solDestino && rutaDestino == solOrigen;
        if (!directa && !inversa) return "La ruta no une el origen y el destino de la solicitud.";

        List<Integer> idsSegmentos = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id_segmento FROM ruta_segmento WHERE id_ruta = ? ORDER BY orden")) {
            pstmt.setInt(1, idRuta);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) idsSegmentos.add(rs.getInt(1));
            }
        }
        if (idsSegmentos.isEmpty()) return "La ruta no tiene segmentos.";

        // 3) bloquear segmentos y validar con los valores bloqueados
        List<GrafoRutas.Tramo> tramos = MovimientosCapacidad.bloquearSegmentos(conn, idsSegmentos);
        if (tramos.size() != new java.util.HashSet<>(idsSegmentos).size()) return "Algún segmento de la ruta ya no existe.";
        List<GrafoRutas.Estacion> estaciones = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_landing_stations, etiqueta_station, estado FROM landing_stations")) {
            while (rs.next()) estaciones.add(new GrafoRutas.Estacion(rs.getInt(1), rs.getString(2), rs.getString(3)));
        }
        GrafoRutas grafo = new GrafoRutas(tramos, estaciones);
        List<GrafoRutas.Tramo> enOrden = new ArrayList<>();
        for (int id : idsSegmentos) enOrden.add(grafo.tramo(id));
        GrafoRutas.Evaluacion ev = grafo.evaluar(enOrden);
        if (!ev.usableParaNuevos()) {
            return "No se puede reservar en esta ruta: " + (ev.motivo == null ? "no está operativa." : ev.motivo + ".");
        }
        for (GrafoRutas.Tramo t : enOrden) {
            if (t.disponible() + 1e-9 < capacidad) {
                return "El segmento " + t.etiqueta + " solo tiene " + GBPS.format(t.disponible()) +
                        " Gbps libres y la solicitud pide " + GBPS.format(capacidad) + " Gbps.";
            }
        }

        // 4) escribir
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO solicitud_segmento (id_solicitud, id_segmento, capacidad_reservada_gbps) VALUES (?,?,?)")) {
            for (GrafoRutas.Tramo t : enOrden) {
                ins.setInt(1, idSolicitud);
                ins.setInt(2, t.id);
                ins.setDouble(3, capacidad);
                ins.addBatch();
            }
            ins.executeBatch();
        }
        for (GrafoRutas.Tramo t : enOrden) {
            MovimientosCapacidad.aplicar(conn, t.id, 0, capacidad);
            MovimientosCapacidad.registrar(conn, t.id, idSolicitud, null, MovimientosCapacidad.RESERVA, capacidad);
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE solicitudes_capacidad SET estado = 'Aprobada' WHERE idsolicitudes_capacidad = ?")) {
            pstmt.setInt(1, idSolicitud);
            pstmt.executeUpdate();
        }
        return null;
    }

    // ------------------------------------------------------------------ liberar

    public String liberar(int idSolicitud) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String error = liberar(conn, idSolicitud);
                if (error != null) {
                    conn.rollback();
                    return error;
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "Error de base de datos al liberar. Revisa la consola del servidor.";
        }
    }

    private String liberar(Connection conn, int idSolicitud) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT estado FROM solicitudes_capacidad WHERE idsolicitudes_capacidad = ? FOR UPDATE")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return "La solicitud no existe.";
            }
        }
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM servicios WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "No se puede liberar: ya existe un servicio creado desde esta solicitud. Finaliza o elimina el servicio primero.";
                }
            }
        }
        List<Integer> ids = new ArrayList<>();
        List<Double> caps = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id_segmento, capacidad_reservada_gbps FROM solicitud_segmento WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                    caps.add(rs.getDouble(2));
                }
            }
        }
        if (ids.isEmpty()) return "La solicitud no tiene capacidad reservada.";

        MovimientosCapacidad.bloquearSegmentos(conn, ids);
        for (int i = 0; i < ids.size(); i++) {
            MovimientosCapacidad.aplicar(conn, ids.get(i), 0, -caps.get(i));
            MovimientosCapacidad.registrar(conn, ids.get(i), idSolicitud, null, MovimientosCapacidad.LIBERACION, -caps.get(i));
        }
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM solicitud_segmento WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE solicitudes_capacidad SET estado = 'En evaluación' WHERE idsolicitudes_capacidad = ?")) {
            pstmt.setInt(1, idSolicitud);
            pstmt.executeUpdate();
        }
        return null;
    }
}
