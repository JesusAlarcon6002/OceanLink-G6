package com.oceanlink.daos;

import com.oceanlink.beans.Clientes;
import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Solicitudes;

import java.sql.*;
import java.util.ArrayList;

/** CRUD de solicitudes_capacidad (con el nombre del cliente y de las estaciones por JOIN). */
public class SolicitudesDao extends DaoBase {

    private static final String SELECT_BASE =
            "SELECT s.idsolicitudes_capacidad, s.etiqueta_solicitud, s.capacidad_solicitada_gbps, " +
            "s.fecha_solicitud, s.estado, s.observaciones, " +
            "c.id_clientes, c.nombres, " +
            "o.id_landing_stations AS id_origen, o.etiqueta_station AS etiqueta_origen, " +
            "d.id_landing_stations AS id_destino, d.etiqueta_station AS etiqueta_destino " +
            "FROM solicitudes_capacidad s " +
            "JOIN clientes c ON c.id_clientes = s.id_cliente " +
            "JOIN landing_stations o ON o.id_landing_stations = s.id_estacion_origen " +
            "JOIN landing_stations d ON d.id_landing_stations = s.id_estacion_destino ";

    public ArrayList<Solicitudes> obtenerListaSolicitudes() {
        ArrayList<Solicitudes> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY s.fecha_solicitud DESC, s.idsolicitudes_capacidad DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Solicitudes obtenerSolicitudes(int idSolicitud) {
        Solicitudes sol = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BASE + "WHERE s.idsolicitudes_capacidad = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sol = mapear(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return sol;
    }

    /** true si ya existe otra solicitud con esa etiqueta. */
    public boolean existeEtiqueta(String etiqueta, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM solicitudes_capacidad WHERE etiqueta_solicitud = ? AND idsolicitudes_capacidad <> ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, etiqueta);
            pstmt.setInt(2, idExcluir);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** true si la solicitud ya tiene capacidad reservada en algún segmento (tabla solicitud_segmento). */
    public boolean tieneReservas(int idSolicitud) {
        String sql = "SELECT COUNT(*) FROM solicitud_segmento WHERE id_solicitud = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return true; // ante la duda, no dejamos borrar
        }
    }

    public boolean crearSolicitudes(Solicitudes sol) {
        String sql = "INSERT INTO solicitudes_capacidad (etiqueta_solicitud, id_cliente, id_estacion_origen, " +
                "id_estacion_destino, capacidad_solicitada_gbps, fecha_solicitud, estado, observaciones) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            llenarCampos(pstmt, sol);
            pstmt.executeUpdate();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarSolicitudes(Solicitudes sol) {
        String sql = "UPDATE solicitudes_capacidad SET etiqueta_solicitud = ?, id_cliente = ?, id_estacion_origen = ?, " +
                "id_estacion_destino = ?, capacidad_solicitada_gbps = ?, fecha_solicitud = ?, estado = ?, observaciones = ? " +
                "WHERE idsolicitudes_capacidad = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            llenarCampos(pstmt, sol);
            pstmt.setInt(9, sol.getIdSolicitud());
            return pstmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean borrarSolicitudes(int idSolicitud) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM solicitudes_capacidad WHERE idsolicitudes_capacidad = ?")) {
            pstmt.setInt(1, idSolicitud);
            return pstmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- auxiliares ----------

    // Parámetros 1..8, mismo orden para INSERT y UPDATE
    private void llenarCampos(PreparedStatement pstmt, Solicitudes sol) throws SQLException {
        pstmt.setString(1, sol.getEtiqueta());
        pstmt.setInt(2, sol.getCliente().getIdCliente());
        pstmt.setInt(3, sol.getEstacionOrigen().getId_landing_stations());
        pstmt.setInt(4, sol.getEstacionDestino().getId_landing_stations());
        pstmt.setBigDecimal(5, java.math.BigDecimal.valueOf(sol.getCapacidadSolicitadaGbps()));
        pstmt.setTimestamp(6, Timestamp.valueOf(sol.getFechaSolicitud()));
        pstmt.setString(7, sol.getEstado());
        pstmt.setString(8, sol.getObservaciones());
    }

    private Solicitudes mapear(ResultSet rs) throws SQLException {
        Solicitudes sol = new Solicitudes();
        sol.setIdSolicitud(rs.getInt("idsolicitudes_capacidad"));
        sol.setEtiqueta(rs.getString("etiqueta_solicitud"));
        sol.setCapacidadSolicitadaGbps(rs.getDouble("capacidad_solicitada_gbps"));
        sol.setFechaSolicitud(rs.getTimestamp("fecha_solicitud").toLocalDateTime());
        sol.setEstado(rs.getString("estado"));
        sol.setObservaciones(rs.getString("observaciones"));

        Clientes c = new Clientes();
        c.setIdCliente(rs.getInt("id_clientes"));
        c.setNombre(rs.getString("nombres"));
        sol.setCliente(c);

        Landing_Stations o = new Landing_Stations();
        o.setId_landing_stations(rs.getInt("id_origen"));
        o.setEtiqueta_station(rs.getString("etiqueta_origen"));
        sol.setEstacionOrigen(o);

        Landing_Stations d = new Landing_Stations();
        d.setId_landing_stations(rs.getInt("id_destino"));
        d.setEtiqueta_station(rs.getString("etiqueta_destino"));
        sol.setEstacionDestino(d);
        return sol;
    }
}
