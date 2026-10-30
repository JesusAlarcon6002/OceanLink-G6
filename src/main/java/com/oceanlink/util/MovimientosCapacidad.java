package com.oceanlink.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones de capacidad sobre segmentos. TODOS los métodos reciben una Connection ya abierta
 * (con autoCommit en false) porque se usan dentro de una transacción: el que llama hace commit/rollback.
 *
 * Convención del historial (columna capacidad_cambio_gbps):
 *   Reserva de capacidad  +C   |  Liberación de capacidad -C
 *   Inicio de servicio    +C   |  Fin de servicio         -C   (cambio sobre la capacidad UTILIZADA)
 */
public final class MovimientosCapacidad {

    public static final String RESERVA = "Reserva de capacidad";
    public static final String LIBERACION = "Liberación de capacidad";
    public static final String AJUSTE = "Ajuste técnico";
    public static final String INICIO = "Inicio de servicio";
    public static final String FIN = "Fin de servicio";

    private MovimientosCapacidad() {
    }

    /**
     * Bloquea los segmentos (SELECT ... FOR UPDATE) SIEMPRE ordenados por ID, así dos operaciones
     * simultáneas no se bloquean entre sí (evita deadlocks), y devuelve su estado actual.
     */
    public static List<GrafoRutas.Tramo> bloquearSegmentos(Connection conn, List<Integer> ids) throws SQLException {
        List<Integer> ordenados = new ArrayList<>(new java.util.TreeSet<>(ids));
        List<GrafoRutas.Tramo> tramos = new ArrayList<>();
        if (ordenados.isEmpty()) return tramos;
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < ordenados.size(); i++) in.append(i == 0 ? "?" : ",?");
        String sql = "SELECT id_segmentos, etiqueta_segmentos, id_estacion_origen, id_estacion_destino, estado, " +
                "capacidad_total_gbps, capacidad_utilizada_gbps, capacidad_reservada_gbps " +
                "FROM segmentos WHERE id_segmentos IN (" + in + ") ORDER BY id_segmentos FOR UPDATE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ordenados.size(); i++) pstmt.setInt(i + 1, ordenados.get(i));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tramos.add(new GrafoRutas.Tramo(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4),
                            rs.getString(5), rs.getDouble(6), rs.getDouble(7), rs.getDouble(8)));
                }
            }
        }
        return tramos;
    }

    /** Suma (o resta, si es negativo) capacidad utilizada y/o reservada. Nunca deja valores negativos. */
    public static void aplicar(Connection conn, int idSegmento, double deltaUsada, double deltaReservada) throws SQLException {
        String sql = "UPDATE segmentos SET capacidad_utilizada_gbps = GREATEST(0, capacidad_utilizada_gbps + ?), " +
                "capacidad_reservada_gbps = GREATEST(0, capacidad_reservada_gbps + ?) WHERE id_segmentos = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, deltaUsada);
            pstmt.setDouble(2, deltaReservada);
            pstmt.setInt(3, idSegmento);
            pstmt.executeUpdate();
        }
    }

    /**
     * Escribe una fila en historial_utilizacion con el estado del segmento DESPUÉS del movimiento
     * (llamar a aplicar() primero). idServicio puede ser null (por ejemplo en una reserva).
     */
    public static void registrar(Connection conn, int idSegmento, int idSolicitud, Integer idServicio,
                                 String tipo, double cambio) throws SQLException {
        double total = 0, usada = 0, reservada = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT capacidad_total_gbps, capacidad_utilizada_gbps, capacidad_reservada_gbps FROM segmentos WHERE id_segmentos = ?")) {
            pstmt.setInt(1, idSegmento);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                    usada = rs.getDouble(2);
                    reservada = rs.getDouble(3);
                }
            }
        }
        double porcentaje = total > 0 ? Math.min(999.99, (usada + reservada) / total * 100.0) : 0;
        double resultante = Math.max(0, total - usada - reservada);

        String sql = "INSERT INTO historial_utilizacion (id_segmento, fecha_movimiento, capacidad_cambio_gbps, " +
                "porcentaje_utilizacion, tipo_movimiento, capacidad_resultante_gbps, id_solicitud, id_servicio) " +
                "VALUES (?, NOW(), ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSegmento);
            pstmt.setDouble(2, cambio);
            pstmt.setDouble(3, Math.round(porcentaje * 100.0) / 100.0);
            pstmt.setString(4, tipo);
            pstmt.setDouble(5, resultante);
            pstmt.setInt(6, idSolicitud);
            if (idServicio == null) pstmt.setNull(7, Types.INTEGER); else pstmt.setInt(7, idServicio);
            pstmt.executeUpdate();
        }
    }
}
