package com.oceanlink.daos;

import com.oceanlink.beans.Historial;

import java.sql.*;
import java.util.ArrayList;

/** Lectura de historial_utilizacion con el nombre del segmento, la solicitud y el servicio. */
public class HistorialDao extends DaoBase {

    public ArrayList<Historial> obtenerListaHistorial() {
        ArrayList<Historial> lista = new ArrayList<>();
        String sql = "SELECT h.id_historial_utilizacion, h.fecha_movimiento, s.etiqueta_segmentos, h.tipo_movimiento, " +
                "h.capacidad_cambio_gbps, h.porcentaje_utilizacion, h.capacidad_resultante_gbps, " +
                "sc.etiqueta_solicitud, sv.etiqueta_servicio " +
                "FROM historial_utilizacion h " +
                "JOIN segmentos s ON s.id_segmentos = h.id_segmento " +
                "LEFT JOIN solicitudes_capacidad sc ON sc.idsolicitudes_capacidad = h.id_solicitud " +
                "LEFT JOIN servicios sv ON sv.id_servicios = h.id_servicio " +
                "ORDER BY h.fecha_movimiento DESC, h.id_historial_utilizacion DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
