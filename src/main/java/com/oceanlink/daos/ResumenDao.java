package com.oceanlink.daos;

import com.oceanlink.beans.Resumen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResumenDao {

    /** Cuenta stations y segmentos y suma la capacidad total en una sola consulta. */
    public Resumen obtenerResumen() {
        Resumen resumen = new Resumen();
        String sql = "SELECT (SELECT COUNT(*) FROM landing_stations) AS stations, " +
                "(SELECT COUNT(*) FROM segmentos) AS segmentos, " +
                "(SELECT COALESCE(SUM(capacidad_total_gbps), 0) FROM segmentos) AS capacidad";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                resumen.setLandingStations(rs.getInt("stations"));
                resumen.setSegmentos(rs.getInt("segmentos"));
                resumen.setCapacidadTotalGbps(rs.getDouble("capacidad"));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return resumen;
    }
}
