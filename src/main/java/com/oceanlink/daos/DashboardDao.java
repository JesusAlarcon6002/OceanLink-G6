package com.oceanlink.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Indicadores (KPI) del dashboard del Supervisor. Todo se calcula con consultas a la BD. */
public class DashboardDao extends DaoBase {

    /** Umbrales de utilización del plan de proyecto: < 80 Normal, 80–95 Alta, > 95 Crítica. */
    public static final double UMBRAL_ALTA = 80.0;
    public static final double UMBRAL_CRITICA = 95.0;

    public static String nivel(double porcentaje) {
        if (porcentaje > UMBRAL_CRITICA) return "Crítica";
        if (porcentaje >= UMBRAL_ALTA) return "Alta";
        return "Normal";
    }

    public static class SegmentoKpi {
        public final String etiqueta;
        public final String estado;
        public final double total;
        public final double utilizada;
        public final double reservada;

        public SegmentoKpi(String etiqueta, String estado, double total, double utilizada, double reservada) {
            this.etiqueta = etiqueta;
            this.estado = estado;
            this.total = total;
            this.utilizada = utilizada;
            this.reservada = reservada;
        }

        public double disponible() {
            return Math.max(0, total - utilizada - reservada);
        }

        public double porcentaje() {
            return total > 0 ? (utilizada + reservada) / total * 100.0 : 0;
        }

        public String nivel() {
            return DashboardDao.nivel(porcentaje());
        }
    }

    public static class ProximoMantenimiento {
        public final String etiqueta;
        public final String tipo;
        public final String fecha;

        public ProximoMantenimiento(String etiqueta, String tipo, String fecha) {
            this.etiqueta = etiqueta;
            this.tipo = tipo;
            this.fecha = fecha;
        }
    }

    /** Todos los indicadores juntos. */
    public static class Datos {
        public int totalSegmentos;
        public int segmentosOperativos;
        public int segmentosNoOperativos;
        public Map<String, Integer> segmentosPorEstado = new LinkedHashMap<>();
        public double capacidadTotal;
        public double capacidadUtilizada;
        public double capacidadReservada;
        public int incidenciasActivas;
        public Map<String, Integer> incidenciasActivasPorSeveridad = new LinkedHashMap<>();
        public int mantenimientosProgramados;
        public int serviciosActivos;
        public int serviciosAfectados;
        public int clientesAfectados;
        public List<SegmentoKpi> segmentos = new ArrayList<>();
        public Map<String, Double> contratadoPorCliente = new LinkedHashMap<>();
        public List<ProximoMantenimiento> proximosMantenimientos = new ArrayList<>();

        public double capacidadDisponible() {
            return Math.max(0, capacidadTotal - capacidadUtilizada - capacidadReservada);
        }

        public double porcentajeRed() {
            return capacidadTotal > 0 ? (capacidadUtilizada + capacidadReservada) / capacidadTotal * 100.0 : 0;
        }

        public int segmentosConAltaUtilizacion() {
            int n = 0;
            for (SegmentoKpi s : segmentos) {
                if (s.porcentaje() >= UMBRAL_ALTA) n++;
            }
            return n;
        }
    }

    private static final String ACTIVAS = "i.estado NOT IN ('Servicio restaurado','Cerrada')";

    public Datos obtenerDatos() {
        Datos d = new Datos();
        for (String e : new String[]{"Operativo", "Degradado", "En mantenimiento", "Fuera de servicio"}) {
            d.segmentosPorEstado.put(e, 0);
        }
        for (String s : new String[]{"Critica", "Alta", "Media", "Baja"}) {
            d.incidenciasActivasPorSeveridad.put(s, 0);
        }
        try (Connection conn = getConnection()) {
            cargarSegmentos(conn, d);
            cargarIncidencias(conn, d);
            cargarServicios(conn, d);
            cargarMantenimientos(conn, d);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return d;
    }

    private void cargarSegmentos(Connection conn, Datos d) throws SQLException {
        String sql = "SELECT etiqueta_segmentos, estado, capacidad_total_gbps, capacidad_utilizada_gbps, capacidad_reservada_gbps " +
                "FROM segmentos ORDER BY etiqueta_segmentos";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                SegmentoKpi s = new SegmentoKpi(rs.getString(1), rs.getString(2),
                        rs.getDouble(3), rs.getDouble(4), rs.getDouble(5));
                d.segmentos.add(s);
                d.totalSegmentos++;
                if ("Operativo".equals(s.estado)) d.segmentosOperativos++;
                else d.segmentosNoOperativos++;
                d.segmentosPorEstado.merge(s.estado, 1, Integer::sum);
                d.capacidadTotal += s.total;
                d.capacidadUtilizada += s.utilizada;
                d.capacidadReservada += s.reservada;
            }
        }
        // los más utilizados primero (para la tabla y el gráfico)
        d.segmentos.sort((a, b) -> Double.compare(b.porcentaje(), a.porcentaje()));
    }

    private void cargarIncidencias(Connection conn, Datos d) throws SQLException {
        String sql = "SELECT i.severidad, COUNT(*) FROM incidencias i WHERE " + ACTIVAS + " GROUP BY i.severidad";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                d.incidenciasActivasPorSeveridad.merge(rs.getString(1), rs.getInt(2), Integer::sum);
                d.incidenciasActivas += rs.getInt(2);
            }
        }
    }

    private void cargarServicios(Connection conn, Datos d) throws SQLException {
        try (Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM servicios WHERE estado = 'Activo'")) {
                if (rs.next()) d.serviciosActivos = rs.getInt(1);
            }
            // contratado por cliente (solo servicios activos)
            String porCliente = "SELECT c.nombres, SUM(s.capacidad_contratada_gbps) AS gbps FROM servicios s " +
                    "JOIN clientes c ON c.id_clientes = s.id_cliente WHERE s.estado = 'Activo' " +
                    "GROUP BY c.id_clientes, c.nombres ORDER BY gbps DESC";
            try (ResultSet rs = st.executeQuery(porCliente)) {
                while (rs.next()) d.contratadoPorCliente.put(rs.getString(1), rs.getDouble(2));
            }
            // servicios cuya ruta pasa por un segmento con incidencia activa (directa, o en una de sus estaciones)
            String afectados = "SELECT COUNT(DISTINCT s.id_servicios), COUNT(DISTINCT s.id_cliente) " +
                    "FROM servicios s JOIN ruta_segmento rs ON rs.id_ruta = s.id_ruta " +
                    "WHERE s.estado IN ('Activo','Pendiente') AND (" +
                    "rs.id_segmento IN (SELECT isg.id_segmento FROM incidencia_segmento isg " +
                    "JOIN incidencias i ON i.id_incidencias = isg.id_incidencia WHERE " + ACTIVAS + ") " +
                    "OR rs.id_segmento IN (SELECT sg.id_segmentos FROM segmentos sg " +
                    "JOIN incidencia_landing_station ils ON (sg.id_estacion_origen = ils.landing_stations_idlanding_stations " +
                    "OR sg.id_estacion_destino = ils.landing_stations_idlanding_stations) " +
                    "JOIN incidencias i ON i.id_incidencias = ils.incidencias_id_incidencias WHERE " + ACTIVAS + "))";
            try (ResultSet rs = st.executeQuery(afectados)) {
                if (rs.next()) {
                    d.serviciosAfectados = rs.getInt(1);
                    d.clientesAfectados = rs.getInt(2);
                }
            }
        }
    }

    private void cargarMantenimientos(Connection conn, Datos d) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM mantenimientos WHERE estado = 'Programado'")) {
            if (rs.next()) d.mantenimientosProgramados = rs.getInt(1);
        }
        String sql = "SELECT etiqueta_mantenimientos, tipo, fecha_programada FROM mantenimientos " +
                "WHERE estado = 'Programado' ORDER BY fecha_programada LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String f = rs.getString(3);
                d.proximosMantenimientos.add(new ProximoMantenimiento(rs.getString(1), rs.getString(2),
                        f == null ? "" : f.substring(0, Math.min(16, f.length()))));
            }
        }
    }
}
