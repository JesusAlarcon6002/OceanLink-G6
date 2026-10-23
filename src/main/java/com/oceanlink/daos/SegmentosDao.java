package com.oceanlink.daos;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Segmentos;

import java.sql.*;
import java.util.ArrayList;

public class SegmentosDao {
    public ArrayList<Segmentos> obtenerListaSegmentos() {
        ArrayList<Segmentos> listaSegmentos = new ArrayList<>();
        try {

            Connection conn = ConexionBD.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT sg.id_segmentos," +
                    " sg.etiqueta_segmentos AS 'Etiquetas de Segmentos'," +
                    " eo.etiqueta_station AS 'etiquetaStationsEo'," +
                    " eo.id_landing_stations AS 'idEOStationsEo'," +
                    " ed.etiqueta_station AS 'etiquetaStationsEd'," +
                    " ed.id_landing_stations AS 'idEDStationsEd'," +
                    " sg.capacidad_total_gbps AS 'Capacidad total (Gbps)'," +
                    " sg.capacidad_utilizada_gbps AS 'Capacidad utilizada (Gbps)'," +
                    " sg.capacidad_reservada_gbps AS 'Capacidad reservada (Gbps)'," +
                    " sg.estado AS 'Estado'" +
                    " FROM segmentos sg" +
                    " INNER JOIN landing_stations eo ON sg.id_estacion_origen = eo.id_landing_stations" +
                    " INNER JOIN landing_stations ed ON sg.id_estacion_destino = ed.id_landing_stations");

            while(rs.next()) {
                Segmentos sg = new Segmentos();
                sg.setIdSegmento(rs.getInt("sg.id_segmentos"));
                sg.setEtiquetaSegmento(rs.getString("Etiquetas de Segmentos"));
                sg.setCapacidadTotalGbps(rs.getDouble("Capacidad total (Gbps)"));
                sg.setCapacidadUtilizadaGbps(rs.getDouble("Capacidad utilizada (Gbps)"));
                sg.setCapacidadReservadaGbps(rs.getDouble("Capacidad reservada (Gbps)"));
                sg.setEstado(rs.getString("Estado"));

                Landing_Stations eo = new Landing_Stations();
                eo.setEtiqueta_station(rs.getString("etiquetaStationsEo"));
                eo.setId_landing_stations(rs.getInt("idEOStationsEo"));

                sg.setIdEstacionOrigen(eo);

                Landing_Stations ed = new Landing_Stations();
                ed.setEtiqueta_station(rs.getString("etiquetaStationsEd"));
                ed.setId_landing_stations(rs.getInt("idEDStationsEd"));

                sg.setIdEstacionDestino(ed);

                listaSegmentos.add(sg);
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return listaSegmentos;
    }

    public void crearSegmentos(String etiqueta, String estacionOrigen, String estacionDestino, String capacidadTotal){
        try{

            try(Connection conn = ConexionBD.getConnection();){
                String sql = "INSERT INTO segmentos (etiqueta_segmentos, id_estacion_origen, id_estacion_destino,capacidad_total_gbps) VALUES (?,?,?,?)";
                try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setString(1,etiqueta);
                    pstmt.setString(2,estacionOrigen);
                    pstmt.setString(3,estacionDestino);
                    pstmt.setString(4,capacidadTotal);
                    pstmt.executeUpdate();
                }
            }
        } catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public Segmentos obtenerSegmentos(int idSegmento) {
        Segmentos sg = null;
        try {


            try (Connection conn = ConexionBD.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT id_segmentos AS 'id'," +
                         " sg.etiqueta_segmentos AS 'etiqueta'," +
                         " eo.id_landing_stations AS 'idEstacionOrigen'," +
                         " eo.etiqueta_station AS 'estacionOrigen'," +
                         " ed.id_landing_stations AS 'idEstacionDestino'," +
                         " ed.etiqueta_station AS 'estacionDestino'," +
                         " sg.capacidad_total_gbps AS 'capacidadTotal'," +
                         " sg.estado AS 'estado'" +
                         " FROM segmentos sg" +
                         " INNER JOIN" +
                         " landing_stations eo ON eo.id_landing_stations = sg.id_estacion_origen " +
                         " INNER JOIN " +
                         " landing_stations ed ON ed.id_landing_stations = sg.id_estacion_destino" +
                         " where id_segmentos = ?;");) {

                pstmt.setInt(1, idSegmento);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sg = new Segmentos();
                        sg.setIdSegmento(rs.getInt("id"));
                        sg.setEtiquetaSegmento(rs.getString("etiqueta"));
                        sg.setCapacidadTotalGbps(rs.getDouble("capacidadTotal"));
                        sg.setEstado(rs.getString("estado"));

                        Landing_Stations eo = new Landing_Stations();
                        eo.setId_landing_stations(rs.getInt("idEstacionOrigen"));
                        eo.setEtiqueta_station(rs.getString("estacionOrigen"));

                        sg.setIdEstacionOrigen(eo);

                        Landing_Stations ed = new Landing_Stations();
                        ed.setId_landing_stations(rs.getInt("idEstacionDestino"));
                        ed.setEtiqueta_station(rs.getString("estacionDestino"));

                        sg.setIdEstacionDestino(ed);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return sg;
    }

    public void actualizarSegmentos(int idSegmento,String etiqueta, String estacionOrigen, String estacionDestino, String capacidadTotal, String estado) {
        try {

            try (Connection conn = ConexionBD.getConnection();) {
                String sql = "UPDATE segmentos SET etiqueta_segmentos = ?, id_estacion_origen = ?, id_estacion_destino = ?, capacidad_total_gbps = ?, estado = ? "
                        + "WHERE id_segmentos = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, etiqueta);
                    pstmt.setString(2, estacionOrigen);
                    pstmt.setString(3, estacionDestino);
                    pstmt.setString(4, capacidadTotal);
                    pstmt.setString(5, estado);
                    pstmt.setInt(6, idSegmento);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void borrarSegmentos(int idSegmentos) {
        try {

            try (Connection conn = ConexionBD.getConnection()) {
                String sql = "DELETE FROM segmentos WHERE id_segmentos = ?;";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idSegmentos);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}