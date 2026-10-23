package com.oceanlink.daos;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Segmentos;

import java.sql.*;
import java.util.ArrayList;

public class Landing_StationsDao {
    public ArrayList<Landing_Stations> obtenerListaLanding_Stations() {
        ArrayList<Landing_Stations> listaLanding_Stations = new ArrayList<>();
        try {

            Connection conn = ConexionBD.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT id_landing_stations, etiqueta_station AS 'Etiqueta', ubicacion AS 'Ubicación', estado AS 'Estado' FROM landing_stations");

            while (rs.next()) {
                Landing_Stations station = new Landing_Stations();

                station.setId_landing_stations(rs.getInt("id_landing_stations"));
                station.setEtiqueta_station(rs.getString("Etiqueta"));
                station.setUbicacion(rs.getString("Ubicación"));
                station.setEstado(rs.getString("Estado"));
                listaLanding_Stations.add(station);
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return listaLanding_Stations;
    }

    public void crearLandingStations(String etiqueta, String ubicacion){
        try{

            try(Connection conn = ConexionBD.getConnection();){
                String sql = "INSERT INTO landing_stations (etiqueta_station, ubicacion) VALUES (?,?)";
                try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                    pstmt.setString(1,etiqueta);
                    pstmt.setString(2,ubicacion);
                    pstmt.executeUpdate();
                }
            }
        } catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    public Landing_Stations obtenerLandingStations(int id_landing_stations){
        Landing_Stations ls = null;
        try {


            try (Connection conn = ConexionBD.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT id_landing_stations AS 'id'," +
                         " etiqueta_station AS 'etiqueta'," +
                         " ubicacion AS 'ubicacion'," +
                         " estado AS 'Estado'" +
                         " FROM landing_stations" +
                         " WHERE id_landing_stations = ?;");) {

                pstmt.setInt(1, id_landing_stations);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ls = new Landing_Stations();
                        ls.setId_landing_stations(rs.getInt("id"));
                        ls.setEtiqueta_station(rs.getString("etiqueta"));
                        ls.setUbicacion(rs.getString("ubicacion"));
                        ls.setEstado(rs.getString("Estado"));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return ls;
    }

    public void actualizarLandingStations(int idLandingStation,String etiqueta, String ubicacion, String estado) {
        try {

            try (Connection conn = ConexionBD.getConnection();) {
                String sql = "UPDATE landing_stations SET etiqueta_station = ?, ubicacion = ?, estado = ?"
                        + " WHERE id_landing_stations = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, etiqueta);
                    pstmt.setString(2, ubicacion);
                    pstmt.setString(3, estado);
                    pstmt.setInt(4, idLandingStation);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void borrarLandingStations(int idLandingStations) {
        try {

            try (Connection conn = ConexionBD.getConnection()) {
                String sql = "DELETE FROM landing_stations WHERE id_landing_stations = ?;";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idLandingStations);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

