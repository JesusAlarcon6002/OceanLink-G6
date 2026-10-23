package com.oceanlink.daos;

import com.oceanlink.beans.Mantenimientos;

import java.sql.*;
import java.util.ArrayList;

public class MantenimientosDao extends DaoBase {

    // Mismo SELECT para la lista y para "obtener uno": trae el mantenimiento y,
    // si tiene, su infraestructura (un segmento o una estación).
    private static final String SELECT_BASE =
            "SELECT m.id_mantenimientos, m.etiqueta_mantenimientos, m.tipo, m.descripcion, " +
            "m.fecha_programada, m.duracion_estimada_horas, m.estado, m.fecha_finalizacion, " +
            "m.resultado, m.actividades, " +
            "mi.id_segmento, mi.id_estacion, sg.etiqueta_segmentos, ls.etiqueta_station " +
            "FROM mantenimientos m " +
            "LEFT JOIN mantenimiento_infraestructura mi ON mi.id_mantenimiento = m.id_mantenimientos " +
            "LEFT JOIN segmentos sg ON sg.id_segmentos = mi.id_segmento " +
            "LEFT JOIN landing_stations ls ON ls.id_landing_stations = mi.id_estacion ";

    public ArrayList<Mantenimientos> obtenerListaMantenimientos() {
        ArrayList<Mantenimientos> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY m.fecha_programada DESC, m.id_mantenimientos DESC";
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

    public Mantenimientos obtenerMantenimientos(int idMantenimiento) {
        Mantenimientos m = null;
        String sql = SELECT_BASE + "WHERE m.id_mantenimientos = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMantenimiento);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    m = mapear(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return m;
    }

    /** true si ya existe otro mantenimiento con esa etiqueta (la columna es UNIQUE). */
    public boolean existeEtiqueta(String etiqueta, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM mantenimientos WHERE etiqueta_mantenimientos = ? AND id_mantenimientos <> ?";
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

    /**
     * Inserta el mantenimiento y su infraestructura en UNA transacción:
     * o se guardan las dos tablas o no se guarda ninguna.
     */
    public boolean crearMantenimientos(Mantenimientos m) {
        String sql = "INSERT INTO mantenimientos (etiqueta_mantenimientos, tipo, descripcion, fecha_programada, " +
                "duracion_estimada_horas, estado, fecha_finalizacion, resultado, actividades) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int nuevoId;
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    llenarCampos(pstmt, m);
                    pstmt.executeUpdate();
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        keys.next();
                        nuevoId = keys.getInt(1);
                    }
                }
                insertarInfraestructura(conn, nuevoId, m);
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

    public boolean actualizarMantenimientos(Mantenimientos m) {
        String sql = "UPDATE mantenimientos SET etiqueta_mantenimientos = ?, tipo = ?, descripcion = ?, " +
                "fecha_programada = ?, duracion_estimada_horas = ?, estado = ?, fecha_finalizacion = ?, " +
                "resultado = ?, actividades = ? WHERE id_mantenimientos = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    llenarCampos(pstmt, m);
                    pstmt.setInt(10, m.getIdMantenimiento());
                    pstmt.executeUpdate();
                }
                // la infraestructura puede haber cambiado: se borra y se vuelve a insertar
                borrarInfraestructura(conn, m.getIdMantenimiento());
                insertarInfraestructura(conn, m.getIdMantenimiento(), m);
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

    public boolean borrarMantenimientos(int idMantenimiento) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // primero la tabla hija (la FK es ON DELETE RESTRICT), luego el mantenimiento
                borrarInfraestructura(conn, idMantenimiento);
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM mantenimientos WHERE id_mantenimientos = ?")) {
                    pstmt.setInt(1, idMantenimiento);
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

    // Parámetros 1..9, en el mismo orden para INSERT y UPDATE
    private void llenarCampos(PreparedStatement pstmt, Mantenimientos m) throws SQLException {
        pstmt.setString(1, m.getEtiqueta());
        pstmt.setString(2, m.getTipo());
        pstmt.setString(3, m.getDescripcion());
        pstmt.setTimestamp(4, Timestamp.valueOf(m.getFechaProgramada()));
        pstmt.setDouble(5, m.getDuracionEstimadaHoras());
        pstmt.setString(6, m.getEstado());
        if (m.getFechaFinalizacion() == null) {
            pstmt.setNull(7, Types.TIMESTAMP);
        } else {
            pstmt.setTimestamp(7, Timestamp.valueOf(m.getFechaFinalizacion()));
        }
        pstmt.setString(8, m.getResultado());
        pstmt.setString(9, m.getActividades());
    }

    private void insertarInfraestructura(Connection conn, int idMantenimiento, Mantenimientos m) throws SQLException {
        if (m.getTipoInfra() == null) {
            return; // sin infraestructura asociada
        }
        boolean esSegmento = "Segmento".equals(m.getTipoInfra());
        String sql = esSegmento
                ? "INSERT INTO mantenimiento_infraestructura (id_mantenimiento, id_segmento) VALUES (?, ?)"
                : "INSERT INTO mantenimiento_infraestructura (id_mantenimiento, id_estacion) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMantenimiento);
            pstmt.setInt(2, m.getIdInfra());
            pstmt.executeUpdate();
        }
    }

    private void borrarInfraestructura(Connection conn, int idMantenimiento) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM mantenimiento_infraestructura WHERE id_mantenimiento = ?")) {
            pstmt.setInt(1, idMantenimiento);
            pstmt.executeUpdate();
        }
    }

    private Mantenimientos mapear(ResultSet rs) throws SQLException {
        Mantenimientos m = new Mantenimientos();
        m.setIdMantenimiento(rs.getInt("id_mantenimientos"));
        m.setEtiqueta(rs.getString("etiqueta_mantenimientos"));
        m.setTipo(rs.getString("tipo"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setFechaProgramada(rs.getTimestamp("fecha_programada").toLocalDateTime());
        m.setDuracionEstimadaHoras(rs.getDouble("duracion_estimada_horas"));
        m.setEstado(rs.getString("estado"));
        Timestamp fin = rs.getTimestamp("fecha_finalizacion");
        if (fin != null) {
            m.setFechaFinalizacion(fin.toLocalDateTime());
        }
        m.setResultado(rs.getString("resultado"));
        m.setActividades(rs.getString("actividades"));

        int idSegmento = rs.getInt("id_segmento");
        boolean haySegmento = !rs.wasNull();
        int idEstacion = rs.getInt("id_estacion");
        boolean hayEstacion = !rs.wasNull();
        if (haySegmento) {
            m.setTipoInfra("Segmento");
            m.setIdInfra(idSegmento);
            m.setEtiquetaInfra(rs.getString("etiqueta_segmentos"));
        } else if (hayEstacion) {
            m.setTipoInfra("Estación");
            m.setIdInfra(idEstacion);
            m.setEtiquetaInfra(rs.getString("etiqueta_station"));
        }
        return m;
    }
}
