package com.oceanlink.daos;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Rutas;
import com.oceanlink.util.GrafoRutas;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CRUD de rutas. Una ruta es una lista ordenada de segmentos (tabla ruta_segmento, columna orden).
 * El estado "real" de la ruta (caída, en mantenimiento...) NO se guarda: se calcula con GrafoRutas
 * cada vez que se consulta, a partir del estado actual de sus segmentos y estaciones.
 */
public class RutasDao extends DaoBase {

    // ---------------------------------------------------------------- grafo

    /** Carga todos los segmentos y estaciones actuales para hacer los cálculos de rutas. */
    public GrafoRutas cargarGrafo() {
        try (Connection conn = getConnection()) {
            return cargarGrafo(conn);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return new GrafoRutas(new ArrayList<GrafoRutas.Tramo>(), new ArrayList<GrafoRutas.Estacion>());
        }
    }

    /** Versión que usa una conexión ya abierta (para usarla dentro de una transacción). */
    public GrafoRutas cargarGrafo(Connection conn) throws SQLException {
        List<GrafoRutas.Tramo> tramos = new ArrayList<>();
        List<GrafoRutas.Estacion> estaciones = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT id_segmentos, etiqueta_segmentos, id_estacion_origen, id_estacion_destino, estado, " +
                    "capacidad_total_gbps, capacidad_utilizada_gbps, capacidad_reservada_gbps FROM segmentos " +
                    "ORDER BY etiqueta_segmentos")) {
                while (rs.next()) {
                    tramos.add(new GrafoRutas.Tramo(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4),
                            rs.getString(5), rs.getDouble(6), rs.getDouble(7), rs.getDouble(8)));
                }
            }
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT id_landing_stations, etiqueta_station, estado FROM landing_stations")) {
                while (rs.next()) {
                    estaciones.add(new GrafoRutas.Estacion(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
            }
        }
        return new GrafoRutas(tramos, estaciones);
    }

    // ---------------------------------------------------------------- consultas

    private static final String SELECT_BASE =
            "SELECT r.idrutas, r.etiqueta_rutas, r.estado, " +
            "o.id_landing_stations AS id_o, o.etiqueta_station AS et_o, " +
            "d.id_landing_stations AS id_d, d.etiqueta_station AS et_d " +
            "FROM rutas r " +
            "JOIN landing_stations o ON o.id_landing_stations = r.id_estacion_origen " +
            "JOIN landing_stations d ON d.id_landing_stations = r.id_estacion_destino ";

    public ArrayList<Rutas> obtenerListaRutas() {
        return consultar("ORDER BY r.etiqueta_rutas", null);
    }

    public Rutas obtenerRutas(int idRuta) {
        ArrayList<Rutas> lista = consultar("WHERE r.idrutas = ?", idRuta);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Rutas registradas entre dos estaciones (en cualquier sentido). */
    public ArrayList<Rutas> obtenerRutasEntre(int idA, int idB) {
        ArrayList<Rutas> resultado = new ArrayList<>();
        for (Rutas r : obtenerListaRutas()) {
            boolean directa = r.getIdOrigen() == idA && r.getIdDestino() == idB;
            boolean inversa = r.getIdOrigen() == idB && r.getIdDestino() == idA;
            if (directa || inversa) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    private ArrayList<Rutas> consultar(String condicion, Integer parametro) {
        ArrayList<Rutas> lista = new ArrayList<>();
        try (Connection conn = getConnection()) {
            GrafoRutas grafo = cargarGrafo(conn);

            // segmentos de cada ruta, en orden
            Map<Integer, List<Integer>> segmentosPorRuta = new HashMap<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id_ruta, id_segmento FROM ruta_segmento ORDER BY id_ruta, orden")) {
                while (rs.next()) {
                    segmentosPorRuta.computeIfAbsent(rs.getInt(1), k -> new ArrayList<>()).add(rs.getInt(2));
                }
            }
            // rutas que ya tienen servicios
            Set<Integer> conServicios = new HashSet<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT id_ruta FROM servicios")) {
                while (rs.next()) {
                    conServicios.add(rs.getInt(1));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_BASE + condicion)) {
                if (parametro != null) {
                    pstmt.setInt(1, parametro);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Rutas r = new Rutas();
                        r.setIdRuta(rs.getInt("idrutas"));
                        r.setEtiqueta(rs.getString("etiqueta_rutas"));
                        r.setEstado(rs.getString("estado"));
                        Landing_Stations o = new Landing_Stations();
                        o.setId_landing_stations(rs.getInt("id_o"));
                        o.setEtiqueta_station(rs.getString("et_o"));
                        r.setEstacionOrigen(o);
                        Landing_Stations d = new Landing_Stations();
                        d.setId_landing_stations(rs.getInt("id_d"));
                        d.setEtiqueta_station(rs.getString("et_d"));
                        r.setEstacionDestino(d);

                        List<Integer> ids = segmentosPorRuta.getOrDefault(r.getIdRuta(), new ArrayList<Integer>());
                        for (int id : ids) {
                            GrafoRutas.Tramo t = grafo.tramo(id);
                            if (t != null) r.getSegmentos().add(t);
                        }
                        r.setEvaluacion(grafo.evaluar(r.getSegmentos()));
                        r.setTieneServicios(conServicios.contains(r.getIdRuta()));
                        lista.add(r);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean existeEtiqueta(String etiqueta, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM rutas WHERE etiqueta_rutas = ? AND idrutas <> ?";
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

    // ---------------------------------------------------------------- escritura

    /** Inserta la ruta y sus segmentos (con su orden) en UNA transacción. */
    public boolean crearRutas(Rutas r) {
        String sql = "INSERT INTO rutas (etiqueta_rutas, id_estacion_origen, id_estacion_destino, estado) VALUES (?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int nuevoId;
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, r.getEtiqueta());
                    pstmt.setInt(2, r.getIdOrigen());
                    pstmt.setInt(3, r.getIdDestino());
                    pstmt.setString(4, r.getEstado());
                    pstmt.executeUpdate();
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        keys.next();
                        nuevoId = keys.getInt(1);
                    }
                }
                insertarSegmentos(conn, nuevoId, r.getIdsSegmentos());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza la ruta. Si cambiarSegmentos es false (la ruta ya tiene servicios) solo se
     * cambian la etiqueta y el estado: cambiar el camino movería capacidad ya reservada.
     */
    public boolean actualizarRutas(Rutas r, boolean cambiarSegmentos) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (cambiarSegmentos) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE rutas SET etiqueta_rutas = ?, id_estacion_origen = ?, id_estacion_destino = ?, estado = ? WHERE idrutas = ?")) {
                        pstmt.setString(1, r.getEtiqueta());
                        pstmt.setInt(2, r.getIdOrigen());
                        pstmt.setInt(3, r.getIdDestino());
                        pstmt.setString(4, r.getEstado());
                        pstmt.setInt(5, r.getIdRuta());
                        pstmt.executeUpdate();
                    }
                    borrarSegmentos(conn, r.getIdRuta());
                    insertarSegmentos(conn, r.getIdRuta(), r.getIdsSegmentos());
                } else {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE rutas SET etiqueta_rutas = ?, estado = ? WHERE idrutas = ?")) {
                        pstmt.setString(1, r.getEtiqueta());
                        pstmt.setString(2, r.getEstado());
                        pstmt.setInt(3, r.getIdRuta());
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Borra la ruta y sus segmentos. Falla (false) si la ruta tiene servicios (FK RESTRICT). */
    public boolean borrarRutas(int idRuta) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                borrarSegmentos(conn, idRuta);
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM rutas WHERE idrutas = ?")) {
                    pstmt.setInt(1, idRuta);
                    pstmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertarSegmentos(Connection conn, int idRuta, List<Integer> idsEnOrden) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO ruta_segmento (id_ruta, id_segmento, orden) VALUES (?,?,?)")) {
            int orden = 1;
            for (int idSegmento : idsEnOrden) {
                pstmt.setInt(1, idRuta);
                pstmt.setInt(2, idSegmento);
                pstmt.setInt(3, orden++);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void borrarSegmentos(Connection conn, int idRuta) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM ruta_segmento WHERE id_ruta = ?")) {
            pstmt.setInt(1, idRuta);
            pstmt.executeUpdate();
        }
    }
}
