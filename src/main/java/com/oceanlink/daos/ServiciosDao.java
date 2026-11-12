package com.oceanlink.daos;

import com.oceanlink.beans.Clientes;
import com.oceanlink.beans.Rutas;
import com.oceanlink.beans.Servicios;
import com.oceanlink.util.GrafoRutas;
import com.oceanlink.util.MovimientosCapacidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicios. La capacidad del servicio vive en los segmentos según su estado:
 *   Pendiente  -> RESERVADA     Activo / Suspendido -> UTILIZADA     Finalizado -> libre (estado final)
 * Cada cambio de estado mueve la capacidad y escribe historial_utilizacion, todo en UNA transacción.
 * Los segmentos del servicio se leen de solicitud_segmento (se borra al finalizar).
 * Los métodos de escritura devuelven null si todo salió bien, o el mensaje de error.
 */
public class ServiciosDao extends DaoBase {

    private static final String SELECT_BASE =
            "SELECT sv.id_servicios, sv.etiqueta_servicio, sv.capacidad_contratada_gbps, sv.fecha_activacion, " +
            "sv.fecha_expiracion, sv.estado, sv.id_solicitud, sc.etiqueta_solicitud, " +
            "c.id_clientes, c.nombres, r.idrutas, r.etiqueta_rutas " +
            "FROM servicios sv " +
            "JOIN clientes c ON c.id_clientes = sv.id_cliente " +
            "JOIN rutas r ON r.idrutas = sv.id_ruta " +
            "LEFT JOIN solicitudes_capacidad sc ON sc.idsolicitudes_capacidad = sv.id_solicitud ";

    // ------------------------------------------------------------------ lectura

    public ArrayList<Servicios> obtenerListaServicios() {
        return consultar("ORDER BY sv.id_servicios DESC", null);
    }

    public Servicios obtenerServicios(int id) {
        ArrayList<Servicios> l = consultar("WHERE sv.id_servicios = ?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    private ArrayList<Servicios> consultar(String condicion, Integer parametro) {
        ArrayList<Servicios> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_BASE + condicion)) {
            if (parametro != null) pstmt.setInt(1, parametro);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Servicios s = new Servicios();
                    s.setIdServicio(rs.getInt(1));
                    s.setEtiqueta(rs.getString(2));
                    s.setCapacidadContratadaGbps(rs.getDouble(3));
                    Date fa = rs.getDate(4);
                    s.setFechaActivacion(fa == null ? null : fa.toLocalDate());
                    Date fe = rs.getDate(5);
                    s.setFechaExpiracion(fe == null ? null : fe.toLocalDate());
                    s.setEstado(rs.getString(6));
                    s.setIdSolicitud(rs.getInt(7));
                    s.setEtiquetaSolicitud(rs.getString(8));
                    Clientes c = new Clientes();
                    c.setIdCliente(rs.getInt(9));
                    c.setNombre(rs.getString(10));
                    s.setCliente(c);
                    s.setIdRuta(rs.getInt(11));
                    s.setEtiquetaRuta(rs.getString(12));
                    lista.add(s);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean existeEtiqueta(String etiqueta, int idExcluir) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM servicios WHERE etiqueta_servicio = ? AND id_servicios <> ?")) {
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

    /** Rutas Activas cuyos segmentos son EXACTAMENTE los reservados por la solicitud. */
    public List<Rutas> rutasCompatibles(int idSolicitud, int idOrigen, int idDestino) {
        Set<Integer> reservados = new HashSet<>();
        for (ReservasDao.Reserva r : new ReservasDao().obtenerReservas(idSolicitud)) reservados.add(r.idSegmento);
        List<Rutas> resultado = new ArrayList<>();
        if (reservados.isEmpty()) return resultado;
        for (Rutas r : new RutasDao().obtenerRutasEntre(idOrigen, idDestino)) {
            if ("Activa".equals(r.getEstado()) && new HashSet<>(r.getIdsSegmentos()).equals(reservados)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    // ------------------------------------------------------------------ crear

    /** Crea el servicio desde una solicitud Aprobada con reservas. estadoInicial: Pendiente o Activo. */
    public String crearServicios(Servicios s, int idSolicitud, String estadoInicial) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String error = crear(conn, s, idSolicitud, estadoInicial);
                if (error != null) {
                    conn.rollback();
                    return error;
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "Error de base de datos al crear el servicio. Revisa la consola del servidor.";
        }
    }

    private String crear(Connection conn, Servicios s, int idSolicitud, String estadoInicial) throws SQLException {
        int idCliente;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT estado, id_cliente FROM solicitudes_capacidad WHERE idsolicitudes_capacidad = ? FOR UPDATE")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return "La solicitud no existe.";
                if (!"Aprobada".equals(rs.getString(1))) return "Solo se puede crear un servicio desde una solicitud Aprobada con capacidad reservada.";
                idCliente = rs.getInt(2);
            }
        }
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM servicios WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return "Esa solicitud ya tiene un servicio.";
            }
        }
        List<Integer> idsReservados = new ArrayList<>();
        double capacidad = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id_segmento, capacidad_reservada_gbps FROM solicitud_segmento WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    idsReservados.add(rs.getInt(1));
                    capacidad = rs.getDouble(2);
                }
            }
        }
        if (idsReservados.isEmpty()) return "La solicitud no tiene capacidad reservada.";

        // la ruta elegida debe estar Activa y usar exactamente los segmentos reservados
        Set<Integer> idsRuta = new HashSet<>();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT rs.id_segmento, r.estado FROM ruta_segmento rs JOIN rutas r ON r.idrutas = rs.id_ruta WHERE rs.id_ruta = ?")) {
            pstmt.setInt(1, s.getIdRuta());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (!"Activa".equals(rs.getString(2))) return "La ruta elegida está Inactiva.";
                    idsRuta.add(rs.getInt(1));
                }
            }
        }
        if (!idsRuta.equals(new HashSet<>(idsReservados))) return "La ruta elegida no coincide con los segmentos reservados de la solicitud.";

        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO servicios (etiqueta_servicio, id_cliente, id_ruta, capacidad_contratada_gbps, fecha_activacion, " +
                "estado, fecha_expiracion, id_solicitud) VALUES (?,?,?,?,?,'Pendiente',?,?)", Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, s.getEtiqueta());
            pstmt.setInt(2, idCliente);
            pstmt.setInt(3, s.getIdRuta());
            pstmt.setDouble(4, capacidad);
            pstmt.setDate(5, s.getFechaActivacion() == null ? null : Date.valueOf(s.getFechaActivacion()));
            pstmt.setDate(6, s.getFechaExpiracion() == null ? null : Date.valueOf(s.getFechaExpiracion()));
            pstmt.setInt(7, idSolicitud);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                keys.next();
                s.setIdServicio(keys.getInt(1));
            }
        }
        setEstadoSolicitud(conn, idSolicitud, "Provisionada");
        if ("Activo".equals(estadoInicial)) {
            String error = cambiarCapacidad(conn, s.getIdServicio(), idSolicitud, capacidad, "Pendiente", "Activo");
            if (error != null) return error;
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE servicios SET estado = 'Activo' WHERE id_servicios = ?")) {
                pstmt.setInt(1, s.getIdServicio());
                pstmt.executeUpdate();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ actualizar

    /** Cambia etiqueta, fechas y (si hace falta) el estado, moviendo la capacidad. */
    public String actualizarServicios(Servicios s) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String error = actualizar(conn, s);
                if (error != null) {
                    conn.rollback();
                    return error;
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "Error de base de datos al guardar el servicio. Revisa la consola del servidor.";
        }
    }

    private String actualizar(Connection conn, Servicios s) throws SQLException {
        String estadoActual;
        int idSolicitud;
        double capacidad;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT estado, id_solicitud, capacidad_contratada_gbps FROM servicios WHERE id_servicios = ? FOR UPDATE")) {
            pstmt.setInt(1, s.getIdServicio());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return "El servicio no existe.";
                estadoActual = rs.getString(1);
                idSolicitud = rs.getInt(2);
                capacidad = rs.getDouble(3);
            }
        }
        String nuevo = s.getEstado();
        if (!estadoActual.equals(nuevo)) {
            if (idSolicitud <= 0) return "Este servicio no está ligado a una solicitud; no se puede mover su capacidad.";
            String error = cambiarCapacidad(conn, s.getIdServicio(), idSolicitud, capacidad, estadoActual, nuevo);
            if (error != null) return error;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE servicios SET etiqueta_servicio = ?, fecha_activacion = ?, fecha_expiracion = ?, estado = ? WHERE id_servicios = ?")) {
            pstmt.setString(1, s.getEtiqueta());
            pstmt.setDate(2, s.getFechaActivacion() == null ? null : Date.valueOf(s.getFechaActivacion()));
            pstmt.setDate(3, s.getFechaExpiracion() == null ? null : Date.valueOf(s.getFechaExpiracion()));
            pstmt.setString(4, nuevo);
            pstmt.setInt(5, s.getIdServicio());
            pstmt.executeUpdate();
        }
        return null;
    }

    // ------------------------------------------------------------------ borrar

    /** Solo se puede borrar un servicio Pendiente (todavía no movió capacidad). Después se Finaliza. */
    public String borrarServicios(int idServicio) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idSolicitud;
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT estado, id_solicitud FROM servicios WHERE id_servicios = ? FOR UPDATE")) {
                    pstmt.setInt(1, idServicio);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "El servicio no existe.";
                        }
                        if (!"Pendiente".equals(rs.getString(1))) {
                            conn.rollback();
                            return "Solo se puede eliminar un servicio Pendiente. Para uno en uso, cámbialo a Finalizado.";
                        }
                        idSolicitud = rs.getInt(2);
                    }
                }
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM servicios WHERE id_servicios = ?")) {
                    pstmt.setInt(1, idServicio);
                    pstmt.executeUpdate();
                }
                if (idSolicitud > 0) setEstadoSolicitud(conn, idSolicitud, "Aprobada");   // la reserva sigue vigente
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return "No se pudo eliminar el servicio (¿tiene historial de utilización?).";
        }
    }

    // ------------------------------------------------------------------ capacidad

    private void setEstadoSolicitud(Connection conn, int idSolicitud, String estado) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE solicitudes_capacidad SET estado = ? WHERE idsolicitudes_capacidad = ?")) {
            pstmt.setString(1, estado);
            pstmt.setInt(2, idSolicitud);
            pstmt.executeUpdate();
        }
    }

    /**
     * Mueve la capacidad de los segmentos del servicio según la transición de estado.
     * Permitidas: Pendiente->Activo|Finalizado, Activo->Suspendido|Finalizado, Suspendido->Activo|Finalizado.
     */
    private String cambiarCapacidad(Connection conn, int idServicio, int idSolicitud, double capacidad,
                                    String desde, String hasta) throws SQLException {
        boolean valida = ("Pendiente".equals(desde) && ("Activo".equals(hasta) || "Finalizado".equals(hasta)))
                || ("Activo".equals(desde) && ("Suspendido".equals(hasta) || "Finalizado".equals(hasta)))
                || ("Suspendido".equals(desde) && ("Activo".equals(hasta) || "Finalizado".equals(hasta)));
        if (!valida) return "No se puede pasar un servicio de " + desde + " a " + hasta + ".";

        List<Integer> ids = new ArrayList<>();
        List<Double> caps = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id_segmento, capacidad_reservada_gbps FROM solicitud_segmento WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                    caps.add(rs.getDouble(2));
                }
            }
        }
        if (ids.isEmpty()) return "No se encontraron los segmentos del servicio (¿se liberó la reserva?).";
        List<GrafoRutas.Tramo> tramos = MovimientosCapacidad.bloquearSegmentos(conn, ids);

        // Activar un servicio es "nuevo": no se permite sobre una ruta caída ni en mantenimiento.
        // Reanudar uno suspendido solo se bloquea si la ruta está caída.
        if ("Activo".equals(hasta)) {
            List<GrafoRutas.Estacion> estaciones = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id_landing_stations, etiqueta_station, estado FROM landing_stations")) {
                while (rs.next()) estaciones.add(new GrafoRutas.Estacion(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
            GrafoRutas.Evaluacion ev = new GrafoRutas(tramos, estaciones).evaluar(tramos);
            if ("Pendiente".equals(desde) && !ev.usableParaNuevos()) {
                return "No se puede activar el servicio: " + (ev.motivo == null ? "la ruta no está operativa." : ev.motivo + ".");
            }
            if ("Suspendido".equals(desde) && ev.caida) {
                return "No se puede reanudar el servicio: " + ev.motivo + ".";
            }
        }

        if ("Pendiente".equals(desde) && "Activo".equals(hasta)) {
            for (int i = 0; i < ids.size(); i++) {
                MovimientosCapacidad.aplicar(conn, ids.get(i), caps.get(i), -caps.get(i));
                MovimientosCapacidad.registrar(conn, ids.get(i), idSolicitud, idServicio, MovimientosCapacidad.INICIO, caps.get(i));
            }
            setEstadoSolicitud(conn, idSolicitud, "Activa");
        } else if ("Pendiente".equals(desde)) {                    // Pendiente -> Finalizado: libera la reserva
            for (int i = 0; i < ids.size(); i++) {
                MovimientosCapacidad.aplicar(conn, ids.get(i), 0, -caps.get(i));
                MovimientosCapacidad.registrar(conn, ids.get(i), idSolicitud, idServicio, MovimientosCapacidad.LIBERACION, -caps.get(i));
            }
            borrarReservas(conn, idSolicitud);
            setEstadoSolicitud(conn, idSolicitud, "En evaluación");
        } else if ("Finalizado".equals(hasta)) {                   // Activo/Suspendido -> Finalizado
            for (int i = 0; i < ids.size(); i++) {
                MovimientosCapacidad.aplicar(conn, ids.get(i), -caps.get(i), 0);
                MovimientosCapacidad.registrar(conn, ids.get(i), idSolicitud, idServicio, MovimientosCapacidad.FIN, -caps.get(i));
            }
            borrarReservas(conn, idSolicitud);
        }
        // Activo <-> Suspendido: la capacidad sigue ocupada, no se mueve nada.
        return null;
    }

    private void borrarReservas(Connection conn, int idSolicitud) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM solicitud_segmento WHERE id_solicitud = ?")) {
            pstmt.setInt(1, idSolicitud);
            pstmt.executeUpdate();
        }
    }
}
