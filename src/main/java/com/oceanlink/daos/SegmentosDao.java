package com.oceanlink.daos;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Segmentos;

import java.sql.*;
import java.util.ArrayList;

public class SegmentosDao {
    public ArrayList<Segmentos> obtenerListaSegmentos() {
        ArrayList<Segmentos> listaSegmentos = new ArrayList<>();
        try {
            String user = "root";
            String pass = "12345678ABCD"; /*DEPENDE DE LA CONTRASEÑA DE TU WORKBENCH*/
            String url = "jdbc:mysql://localhost:3306/proyecto_siete";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
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
}