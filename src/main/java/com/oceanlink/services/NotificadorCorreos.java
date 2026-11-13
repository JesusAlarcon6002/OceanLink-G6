package com.oceanlink.services;

import com.oceanlink.beans.Incidencias;
import com.oceanlink.beans.Mantenimientos;
import com.oceanlink.beans.Solicitudes;
import com.oceanlink.daos.NotificacionDao;
import com.oceanlink.daos.NotificacionDao.SegmentoUso;
import com.oceanlink.daos.NotificacionDao.ServicioAfectado;
import com.oceanlink.util.Html;
import com.oceanlink.util.Rol;

import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Las 4 notificaciones por correo de OceanLink. Cada método:
 *   1) busca a quién avisar (usuarios activos de ciertos roles),
 *   2) arma el mensaje HTML,
 *   3) lo manda con CorreoService en segundo plano.
 * Todo va dentro de try/catch: un problema con el correo nunca debe impedir guardar el dato principal.
 *
 *  - Incidencia registrada        -> Network Operator, Supervisor y Capacity Planner
 *  - Cambio de estado de solicitud -> Capacity Planner y Supervisor
 *  - Mantenimiento programado      -> Maintenance Coordinator, Network Operator y Supervisor
 *  - Utilización crítica (> 95 %)  -> Capacity Planner y Supervisor
 */
public final class NotificadorCorreos {

    /** Utilización a partir de la cual un segmento se considera crítico (regla del plan de proyecto). */
    public static final double UMBRAL_CRITICO = 95.0;

    private static final DecimalFormat NUM = new DecimalFormat("#,##0.##");

    private NotificadorCorreos() {
    }

    // ---------------------------------------------------------------- 1. Incidencia registrada
    public static void incidenciaRegistrada(Incidencias inc) {
        try {
            NotificacionDao dao = new NotificacionDao();
            List<String> para = dao.correosPorRoles(Rol.NETWORK_OPERATOR.getNombreBd(),
                    Rol.SUPERVISOR.getNombreBd(), Rol.CAPACITY_PLANNER.getNombreBd());
            List<ServicioAfectado> afectados = dao.serviciosAfectados(inc.getTipoInfra(), inc.getIdInfra());

            StringBuilder cuerpo = new StringBuilder();
            cuerpo.append(filas(
                    "Etiqueta", inc.getEtiqueta(),
                    "Título", inc.getTitulo(),
                    "Severidad", inc.getSeveridad(),
                    "Estado", inc.getEstado(),
                    "Detectada", inc.getFechaDeteccionTexto(),
                    "Infraestructura afectada", inc.getTipoInfra() == null ? "No indicada"
                            : inc.getTipoInfra() + " (id " + inc.getIdInfra() + ")",
                    "Descripción", inc.getDescripcion()));
            cuerpo.append(tablaAfectados(afectados));
            CorreoService.enviarAsync(para,
                    "[OceanLink] Incidencia " + valor(inc.getSeveridad()) + ": " + valor(inc.getEtiqueta()),
                    plantilla("Nueva incidencia registrada", cuerpo.toString()));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la notificación de incidencia: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 2. Cambio de estado de solicitud
    public static void solicitudCambioEstado(Solicitudes sol, String estadoAnterior) {
        try {
            if (sol == null || estadoAnterior == null || estadoAnterior.equals(sol.getEstado())) {
                return;
            }
            List<String> para = new NotificacionDao().correosPorRoles(
                    Rol.CAPACITY_PLANNER.getNombreBd(), Rol.SUPERVISOR.getNombreBd());
            String cuerpo = filas(
                    "Solicitud", sol.getEtiqueta(),
                    "Cliente", sol.getCliente() == null ? "" : sol.getCliente().getNombre(),
                    "Ruta solicitada", (sol.getEstacionOrigen() == null ? "?" : sol.getEstacionOrigen().getEtiqueta_station())
                            + " → " + (sol.getEstacionDestino() == null ? "?" : sol.getEstacionDestino().getEtiqueta_station()),
                    "Capacidad solicitada", NUM.format(sol.getCapacidadSolicitadaGbps()) + " Gbps",
                    "Estado anterior", estadoAnterior,
                    "Estado nuevo", sol.getEstado(),
                    "Observaciones", sol.getObservaciones());
            CorreoService.enviarAsync(para,
                    "[OceanLink] Solicitud " + valor(sol.getEtiqueta()) + ": " + estadoAnterior + " → " + valor(sol.getEstado()),
                    plantilla("Cambió el estado de una solicitud de capacidad", cuerpo));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la notificación de solicitud: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 3. Mantenimiento programado
    public static void mantenimientoProgramado(Mantenimientos m) {
        try {
            NotificacionDao dao = new NotificacionDao();
            List<String> para = dao.correosPorRoles(Rol.MAINTENANCE_COORDINATOR.getNombreBd(),
                    Rol.NETWORK_OPERATOR.getNombreBd(), Rol.SUPERVISOR.getNombreBd());
            List<ServicioAfectado> afectados = dao.serviciosAfectados(m.getTipoInfra(), m.getIdInfra());

            StringBuilder cuerpo = new StringBuilder();
            cuerpo.append(filas(
                    "Etiqueta", m.getEtiqueta(),
                    "Tipo", m.getTipo(),
                    "Fecha programada", m.getFechaProgramadaTexto(),
                    "Duración estimada", NUM.format(m.getDuracionEstimadaHoras()) + " horas",
                    "Estado", m.getEstado(),
                    "Infraestructura", m.getTipoInfra() == null ? "No indicada"
                            : m.getTipoInfra() + " (id " + m.getIdInfra() + ")",
                    "Descripción", m.getDescripcion()));
            cuerpo.append(tablaAfectados(afectados));
            CorreoService.enviarAsync(para,
                    "[OceanLink] Mantenimiento " + valor(m.getTipo()).toLowerCase() + " programado: " + valor(m.getEtiqueta()),
                    plantilla("Se programó un mantenimiento", cuerpo.toString()));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la notificación de mantenimiento: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 4. Utilización crítica
    /** Revisa los segmentos indicados y avisa si alguno pasó del 95 % de utilización (utilizada + reservada). */
    public static void alertaUtilizacionCritica(List<Integer> idsSegmentos) {
        try {
            NotificacionDao dao = new NotificacionDao();
            List<SegmentoUso> criticos = dao.segmentosSobreUmbral(idsSegmentos, UMBRAL_CRITICO);
            if (criticos.isEmpty()) {
                return;
            }
            List<String> para = dao.correosPorRoles(Rol.CAPACITY_PLANNER.getNombreBd(), Rol.SUPERVISOR.getNombreBd());

            StringBuilder cuerpo = new StringBuilder();
            cuerpo.append("<p style=\"margin:0 0 12px\">Los siguientes segmentos superaron el ")
                    .append((int) UMBRAL_CRITICO).append(" % de utilización:</p>");
            cuerpo.append("<table style=\"border-collapse:collapse;width:100%;font-size:13px\">")
                    .append("<tr style=\"background:#e2e8f0\"><th align=\"left\" style=\"padding:6px\">Segmento</th>")
                    .append("<th align=\"right\" style=\"padding:6px\">Total (Gbps)</th>")
                    .append("<th align=\"right\" style=\"padding:6px\">Usada + reservada (Gbps)</th>")
                    .append("<th align=\"right\" style=\"padding:6px\">Utilización</th></tr>");
            for (SegmentoUso s : criticos) {
                cuerpo.append("<tr><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(s.etiqueta))
                        .append("</td><td align=\"right\" style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(NUM.format(s.total))
                        .append("</td><td align=\"right\" style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(NUM.format(s.utilizada + s.reservada))
                        .append("</td><td align=\"right\" style=\"padding:6px;border-bottom:1px solid #e2e8f0;color:#b91c1c\"><b>")
                        .append(NUM.format(s.porcentaje())).append(" %</b></td></tr>");
            }
            cuerpo.append("</table>");
            CorreoService.enviarAsync(para,
                    "[OceanLink] Alerta: utilización crítica en " + criticos.size() + " segmento(s)",
                    plantilla("Utilización crítica de capacidad", cuerpo.toString()));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la alerta de utilización: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- plantillas

    private static String valor(String s) {
        return s == null ? "" : s;
    }

    /** Pares etiqueta/valor -> tabla HTML. Todo valor se escapa (nadie puede inyectar HTML en el correo). */
    private static String filas(String... paresEtiquetaValor) {
        StringBuilder sb = new StringBuilder("<table style=\"border-collapse:collapse;width:100%;font-size:14px\">");
        for (int i = 0; i + 1 < paresEtiquetaValor.length; i += 2) {
            String v = paresEtiquetaValor[i + 1];
            if (v == null || v.trim().isEmpty()) {
                continue;
            }
            sb.append("<tr><td style=\"padding:6px 10px 6px 0;color:#475569;vertical-align:top;white-space:nowrap\">")
                    .append(Html.esc(paresEtiquetaValor[i])).append("</td><td style=\"padding:6px 0\"><b>")
                    .append(Html.esc(v)).append("</b></td></tr>");
        }
        return sb.append("</table>").toString();
    }

    private static String tablaAfectados(List<ServicioAfectado> afectados) {
        StringBuilder sb = new StringBuilder("<h3 style=\"margin:18px 0 8px;font-size:15px\">Servicios y clientes potencialmente afectados</h3>");
        if (afectados.isEmpty()) {
            return sb.append("<p style=\"margin:0;color:#475569\">No se encontraron servicios activos sobre esta infraestructura.</p>").toString();
        }
        Set<String> clientes = new LinkedHashSet<>();
        sb.append("<table style=\"border-collapse:collapse;width:100%;font-size:13px\">")
                .append("<tr style=\"background:#e2e8f0\"><th align=\"left\" style=\"padding:6px\">Servicio</th>")
                .append("<th align=\"left\" style=\"padding:6px\">Cliente</th>")
                .append("<th align=\"right\" style=\"padding:6px\">Capacidad (Gbps)</th></tr>");
        for (ServicioAfectado a : afectados) {
            clientes.add(a.cliente);
            sb.append("<tr><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(a.servicio))
                    .append("</td><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(a.cliente))
                    .append("</td><td align=\"right\" style=\"padding:6px;border-bottom:1px solid #e2e8f0\">")
                    .append(NUM.format(a.capacidadGbps)).append("</td></tr>");
        }
        sb.append("</table><p style=\"margin:8px 0 0;color:#475569\">")
                .append(afectados.size()).append(" servicio(s), ").append(clientes.size()).append(" cliente(s).</p>");
        return sb.toString();
    }

    /** Marco común de todos los correos (cabecera azul de OceanLink + pie). */
    static String plantilla(String titulo, String contenido) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:640px;margin:0 auto;color:#0f172a\">"
                + "<div style=\"background:#1e3a5f;color:#ffffff;padding:16px 20px;border-radius:8px 8px 0 0\">"
                + "<div style=\"font-size:12px;letter-spacing:2px;color:#93c5fd\">OCEAN LINK</div>"
                + "<div style=\"font-size:18px;font-weight:bold;margin-top:4px\">" + Html.esc(titulo) + "</div></div>"
                + "<div style=\"border:1px solid #cbd5e1;border-top:none;padding:18px 20px;border-radius:0 0 8px 8px\">"
                + contenido
                + "<p style=\"margin:22px 0 0;font-size:12px;color:#64748b\">Mensaje automático del sistema OceanLink. No respondas a este correo.</p>"
                + "</div></div>";
    }
}
