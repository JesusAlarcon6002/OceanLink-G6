package com.oceanlink.daos;

import com.oceanlink.beans.Incidencias;

import java.sql.*;
import java.util.ArrayList;

/**
 * CRUD de incidencias. La infraestructura afectada (un segmento o una estación)
 * vive en las tablas incidencia_segmento e incidencia_landing_station.
 */
public class IncidenciasDao extends DaoBase {

    private static final String SELECT_BASE =
            "SELECT i.id_incidencias, i.etiqueta_incidencias, i.titulo, i.descripcion, i.severidad, i.estado, " +
            "i.fecha_deteccion, i.fecha_restauracion, i.solucion, i.observaciones, " +
            "isg.id_segmento, ils.landing_stations_idlanding_stations AS id_estacion, " +
            "sg.etiqueta_segmentos, ls.etiqueta_station " +
            "FROM incidencias i " +
            "LEFT JOIN incidencia_segmento isg ON isg.id_incidencia = i.id_incidencias " +
            "LEFT JOIN segmentos sg ON sg.id_segmentos = isg.id_segmento " +
            "LEFT JOIN incidencia_landing_station ils ON ils.incidencias_id_incidencias = i.id_incidencias " +
            "LEFT JOIN landing_stations ls ON ls.id_landing_stations = ils.landing_stations_idlanding_stations ";

    public ArrayList<Incidencias> obtenerListaIncidencias() {
        ArrayList<Incidencias> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY i.fecha_deteccion DESC, i.id_incidencias DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Si una incidencia tuviera varias infraestructuras, el JOIN repite su fila:
            // nos quedamos con la primera para que cada incidencia salga una sola vez.
            java.util.HashSet<Integer> vistos = new java.util.HashSet<>();
            while (rs.next()) {
                if (vistos.add(rs.getInt("id_incidencias"))) {
                    lista.add(mapear(rs));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Incidencias obtenerIncidencias(int idIncidencia) {
        Incidencias inc = null;
        String sql = SELECT_BASE + "WHERE i.id_incidencias = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idIncidencia);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    inc = mapear(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return inc;
    }

    /** true si ya existe otra incidencia con esa etiqueta. */
    public boolean existeEtiqueta(String etiqueta, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM incidencias WHERE etiqueta_incidencias = ? AND id_incidencias <> ?";
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

    /** Inserta la incidencia y su infraestructura en UNA transacción. */
    public boolean crearIncidencias(Incidencias inc) {
        String sql = "INSERT INTO incidencias (etiqueta_incidencias, titulo, descripcion, severidad, estado, " +
                "fecha_deteccion, fecha_restauracion, solucion, observaciones) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int nuevoId;
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    llenarCampos(pstmt, inc);
                    pstmt.executeUpdate();
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        keys.next();
                        nuevoId = keys.getInt(1);
                    }
                }
                insertarInfraestructura(conn, nuevoId, inc);
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

    public boolean actualizarIncidencias(Incidencias inc) {
        String sql = "UPDATE incidencias SET etiqueta_incidencias = ?, titulo = ?, descripcion = ?, severidad = ?, " +
                "estado = ?, fecha_deteccion = ?, fecha_restauracion = ?, solucion = ?, observaciones = ? " +
                "WHERE id_incidencias = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    llenarCampos(pstmt, inc);
                    pstmt.setInt(10, inc.getIdIncidencia());
                    pstmt.executeUpdate();
                }
                borrarInfraestructura(conn, inc.getIdIncidencia());
                insertarInfraestructura(conn, inc.getIdIncidencia(), inc);
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

    public boolean borrarIncidencias(int idIncidencia) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                borrarInfraestructura(conn, idIncidencia);
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM incidencias WHERE id_incidencias = ?")) {
                    pstmt.setInt(1, idIncidencia);
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

    // ---------- auxiliares ----------

    // Parámetros 1..9, mismo orden para INSERT y UPDATE
    private void llenarCampos(PreparedStatement pstmt, Incidencias inc) throws SQLException {
        pstmt.setString(1, inc.getEtiqueta());
        pstmt.setString(2, inc.getTitulo());
        pstmt.setString(3, inc.getDescripcion());
        pstmt.setString(4, inc.getSeveridad());
        pstmt.setString(5, inc.getEstado());
        pstmt.setTimestamp(6, Timestamp.valueOf(inc.getFechaDeteccion()));
        if (inc.getFechaRestauracion() == null) {
            pstmt.setNull(7, Types.TIMESTAMP);
        } else {
            pstmt.setTimestamp(7, Timestamp.valueOf(inc.getFechaRestauracion()));
        }
        pstmt.setString(8, inc.getSolucion());
        pstmt.setString(9, inc.getObservaciones());
    }

    private void insertarInfraestructura(Connection conn, int idIncidencia, Incidencias inc) throws SQLException {
        if (inc.getTipoInfra() == null) {
            return;
        }
        boolean esSegmento = "Segmento".equals(inc.getTipoInfra());
        String sql = esSegmento
                ? "INSERT INTO incidencia_segmento (id_incidencia, id_segmento) VALUES (?, ?)"
                : "INSERT INTO incidencia_landing_station (incidencias_id_incidencias, landing_stations_idlanding_stations) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idIncidencia);
            pstmt.setInt(2, inc.getIdInfra());
            pstmt.executeUpdate();
        }
    }

    private void borrarInfraestructura(Connection conn, int idIncidencia) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM incidencia_segmento WHERE id_incidencia = ?")) {
            pstmt.setInt(1, idIncidencia);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM incidencia_landing_station WHERE incidencias_id_incidencias = ?")) {
            pstmt.setInt(1, idIncidencia);
            pstmt.executeUpdate();
        }
    }

    private Incidencias mapear(ResultSet rs) throws SQLException {
        Incidencias inc = new Incidencias();
        inc.setIdIncidencia(rs.getInt("id_incidencias"));
        inc.setEtiqueta(rs.getString("etiqueta_incidencias"));
        inc.setTitulo(rs.getString("titulo"));
        inc.setDescripcion(rs.getString("descripcion"));
        inc.setSeveridad(rs.getString("severidad"));
        inc.setEstado(rs.getString("estado"));
        inc.setFechaDeteccion(rs.getTimestamp("fecha_deteccion").toLocalDateTime());
        Timestamp rest = rs.getTimestamp("fecha_restauracion");
        if (rest != null) {
            inc.setFechaRestauracion(rest.toLocalDateTime());
        }
        inc.setSolucion(rs.getString("solucion"));
        inc.setObservaciones(rs.getString("observaciones"));

        int idSegmento = rs.getInt("id_segmento");
        boolean haySegmento = !rs.wasNull();
        int idEstacion = rs.getInt("id_estacion");
        boolean hayEstacion = !rs.wasNull();
        if (haySegmento) {
            inc.setTipoInfra("Segmento");
            inc.setIdInfra(idSegmento);
            inc.setEtiquetaInfra(rs.getString("etiqueta_segmentos"));
        } else if (hayEstacion) {
            inc.setTipoInfra("Estación");
            inc.setIdInfra(idEstacion);
            inc.setEtiquetaInfra(rs.getString("etiqueta_station"));
        }
        return inc;
    }
}
