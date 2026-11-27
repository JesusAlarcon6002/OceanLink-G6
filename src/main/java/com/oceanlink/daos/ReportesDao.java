package com.oceanlink.daos;

import com.oceanlink.beans.Reporte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reportes con filtros (RF38). Cada método arma un WHERE solo con los filtros que llegaron
 * y usa PreparedStatement para todos los valores.
 */
public class ReportesDao extends DaoBase {

    private static final DecimalFormat NUM = new DecimalFormat("0.##");

    private static String f(String v) {
        return v == null ? "" : v;
    }

    private static String fecha(String v, int largo) {
        return v == null ? "" : v.substring(0, Math.min(largo, v.length()));
    }

    /** Utilización por segmento. Filtros: estado del segmento y nivel (Normal / Alta / Crítica). */
    public Reporte utilizacion(String estado, String nivel) {
        Reporte r = new Reporte("Utilización de capacidad por segmento", Arrays.asList(
                "Segmento", "Estado", "Total (Gbps)", "Utilizada (Gbps)", "Reservada (Gbps)", "Disponible (Gbps)", "Utilización (%)", "Nivel"));
        StringBuilder sql = new StringBuilder("SELECT etiqueta_segmentos, estado, capacidad_total_gbps, capacidad_utilizada_gbps, " +
                "capacidad_reservada_gbps FROM segmentos WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (!vacio(estado)) {
            sql.append(" AND estado = ?");
            p.add(estado);
        }
        sql.append(" ORDER BY etiqueta_segmentos");
        consultar(sql.toString(), p, rs -> {
            double total = rs.getDouble(3), usada = rs.getDouble(4), res = rs.getDouble(5);
            double pct = total > 0 ? (usada + res) / total * 100.0 : 0;
            String niv = DashboardDao.nivel(pct);
            if (!vacio(nivel) && !nivel.equals(niv)) return;
            r.agregarFila(Arrays.asList(rs.getString(1), rs.getString(2), NUM.format(total), NUM.format(usada),
                    NUM.format(res), NUM.format(Math.max(0, total - usada - res)), NUM.format(pct), niv));
        });
        return r;
    }

    /** Incidencias. Filtros: estado, severidad y rango de fechas de detección (desde / hasta, formato yyyy-MM-dd). */
    public Reporte incidencias(String estado, String severidad, String desde, String hasta) {
        Reporte r = new Reporte("Incidencias", Arrays.asList(
                "Etiqueta", "Título", "Severidad", "Estado", "Detectada", "Restaurada", "Infraestructura"));
        StringBuilder sql = new StringBuilder(
                "SELECT i.etiqueta_incidencias, i.titulo, i.severidad, i.estado, i.fecha_deteccion, i.fecha_restauracion, " +
                "COALESCE(sg.etiqueta_segmentos, ls.etiqueta_station, '') AS infra " +
                "FROM incidencias i " +
                "LEFT JOIN incidencia_segmento isg ON isg.id_incidencia = i.id_incidencias " +
                "LEFT JOIN segmentos sg ON sg.id_segmentos = isg.id_segmento " +
                "LEFT JOIN incidencia_landing_station ils ON ils.incidencias_id_incidencias = i.id_incidencias " +
                "LEFT JOIN landing_stations ls ON ls.id_landing_stations = ils.landing_stations_idlanding_stations " +
                "WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (!vacio(estado)) {
            sql.append(" AND i.estado = ?");
            p.add(estado);
        }
        if (!vacio(severidad)) {
            sql.append(" AND i.severidad = ?");
            p.add(severidad);
        }
        LocalDate d = parsear(desde), h = parsear(hasta);
        if (d != null) {
            sql.append(" AND i.fecha_deteccion >= ?");
            p.add(d.toString() + " 00:00:00");
        }
        if (h != null) {
            sql.append(" AND i.fecha_deteccion < ?");
            p.add(h.plusDays(1).toString() + " 00:00:00");
        }
        sql.append(" ORDER BY i.fecha_deteccion DESC");
        consultar(sql.toString(), p, rs -> r.agregarFila(Arrays.asList(rs.getString(1), rs.getString(2), rs.getString(3),
                rs.getString(4), fecha(rs.getString(5), 16), fecha(rs.getString(6), 16), f(rs.getString(7)))));
        return r;
    }

    /** Servicios por cliente. Filtros: cliente (id) y estado del servicio. */
    public Reporte servicios(int idCliente, String estado) {
        Reporte r = new Reporte("Servicios y capacidad contratada por cliente", Arrays.asList(
                "Servicio", "Cliente", "Ruta", "Capacidad contratada (Gbps)", "Estado", "Activación"));
        StringBuilder sql = new StringBuilder(
                "SELECT s.etiqueta_servicio, c.nombres, ru.etiqueta_rutas, s.capacidad_contratada_gbps, s.estado, s.fecha_activacion " +
                "FROM servicios s JOIN clientes c ON c.id_clientes = s.id_cliente " +
                "JOIN rutas ru ON ru.idrutas = s.id_ruta WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (idCliente > 0) {
            sql.append(" AND s.id_cliente = ?");
            p.add(idCliente);
        }
        if (!vacio(estado)) {
            sql.append(" AND s.estado = ?");
            p.add(estado);
        }
        sql.append(" ORDER BY c.nombres, s.etiqueta_servicio");
        consultar(sql.toString(), p, rs -> r.agregarFila(Arrays.asList(rs.getString(1), rs.getString(2), rs.getString(3),
                NUM.format(rs.getDouble(4)), rs.getString(5), f(rs.getString(6)))));
        return r;
    }

    /** Mantenimientos. Filtros: estado, tipo y rango de fecha programada. */
    public Reporte mantenimientos(String estado, String tipo, String desde, String hasta) {
        Reporte r = new Reporte("Mantenimientos", Arrays.asList(
                "Etiqueta", "Tipo", "Estado", "Programado", "Duración (h)", "Finalizado", "Resultado"));
        StringBuilder sql = new StringBuilder(
                "SELECT etiqueta_mantenimientos, tipo, estado, fecha_programada, duracion_estimada_horas, fecha_finalizacion, resultado " +
                "FROM mantenimientos WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (!vacio(estado)) {
            sql.append(" AND estado = ?");
            p.add(estado);
        }
        if (!vacio(tipo)) {
            sql.append(" AND tipo = ?");
            p.add(tipo);
        }
        LocalDate d = parsear(desde), h = parsear(hasta);
        if (d != null) {
            sql.append(" AND fecha_programada >= ?");
            p.add(d.toString() + " 00:00:00");
        }
        if (h != null) {
            sql.append(" AND fecha_programada < ?");
            p.add(h.plusDays(1).toString() + " 00:00:00");
        }
        sql.append(" ORDER BY fecha_programada DESC");
        consultar(sql.toString(), p, rs -> r.agregarFila(Arrays.asList(rs.getString(1), rs.getString(2), rs.getString(3),
                fecha(rs.getString(4), 16), NUM.format(rs.getDouble(5)), fecha(rs.getString(6), 16), f(rs.getString(7)))));
        return r;
    }

    // ---- utilidades ----

    private interface FilaLector {
        void leer(ResultSet rs) throws SQLException;
    }

    private void consultar(String sql, List<Object> params, FilaLector lector) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lector.leer(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static LocalDate parsear(String s) {
        try {
            return vacio(s) ? null : LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
