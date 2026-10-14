package com.oceanlink.daos;

import com.oceanlink.beans.Landing_Stations;

import java.sql.*;
import java.util.ArrayList;

public class Landing_StationsDao {
    public ArrayList<Landing_Stations> obtenerListaLanding_Stations() {
        ArrayList<Landing_Stations> listaLanding_Stations = new ArrayList<>();
        try {
            String user = "root";
            String pass = "12345678ABCD"; /*DEPENDE DE LA CONTRASEÑA DE TU WORKBENCH*/
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
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
}

