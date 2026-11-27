package com.oceanlink.daos;

import com.oceanlink.beans.Historial;

import java.sql.*;
import java.util.ArrayList;

/** Lectura de historial_utilizacion con el nombre del segmento, la solicitud y el servicio. */
public class HistorialDao extends DaoBase {

    public ArrayList<Historial> obtenerListaHistorial() {
        return obtenerListaHistorial(0, null, null);
    }

    /**
     * Historial con filtros opcionales (todo con parámetros, nada de texto pegado al SQL):
     * idSegmento 0 = todos; desde/hasta (inclusive) null = sin límite.
     */
    public ArrayList<Historial> obtenerListaHistorial(int idSegmento, java.time.LocalDate desde, java.time.LocalDate hasta) {
        ArrayList<Historial> lista = new ArrayList<>();
        String sql = "SELECT h.id_historial_utilizacion, h.fecha_movimiento, s.etiqueta_segmentos, h.tipo_movimiento, " +
                "h.capacidad_cambio_gbps, h.porcentaje_utilizacion, h.capacidad_resultante_gbps, " +
                "sc.etiqueta_solicitud, sv.etiqueta_servicio " +
                "FROM historial_utilizacion h " +
                "JOIN segmentos s ON s.id_segmentos = h.id_segmento " +
                "LEFT JOIN solicitudes_capacidad sc ON sc.idsolicitudes_capacidad = h.id_solicitud " +
                "LEFT JOIN servicios sv ON sv.id_servicios = h.id_servicio " +
                "WHERE (? = 0 OR h.id_segmento = ?) " +
                "AND (? IS NULL OR h.fecha_movimiento >= ?) " +
                "AND (? IS NULL OR h.fecha_movimiento < ?) " +
                "ORDER BY h.fecha_movimiento DESC, h.id_historial_utilizacion DESC";
        Timestamp tDesde = desde == null ? null : Timestamp.valueOf(desde.atStartOfDay());
        Timestamp tHasta = hasta == null ? null : Timestamp.valueOf(hasta.plusDays(1).atStartOfDay());
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSegmento);
            stmt.setInt(2, idSegmento);
            stmt.setTimestamp(3, tDesde);
            stmt.setTimestamp(4, tDesde);
            stmt.setTimestamp(5, tHasta);
            stmt.setTimestamp(6, tHasta);
            try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Historial h = new Historial();
                h.setIdHistorial(rs.getInt(1));
                Timestamp ts = rs.getTimestamp(2);
                h.setFecha(ts == null ? null : ts.toLocalDateTime());
                h.setEtiquetaSegmento(rs.getString(3));
                h.setTipoMovimiento(rs.getString(4));
                h.setCapacidadCambioGbps(rs.getDouble(5));
                h.setPorcentajeUtilizacion(rs.getDouble(6));
                h.setCapacidadResultanteGbps(rs.getDouble(7));
                h.setEtiquetaSolicitud(rs.getString(8));
                h.setEtiquetaServicio(rs.getString(9));
                lista.add(h);
            }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
