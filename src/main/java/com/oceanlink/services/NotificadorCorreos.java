package com.oceanlink.services;

import com.oceanlink.beans.Incidencias;
import com.oceanlink.beans.Mantenimientos;
import com.oceanlink.beans.Servicios;
import com.oceanlink.beans.Solicitudes;
import com.oceanlink.daos.NotificacionDao;
import com.oceanlink.daos.NotificacionDao.SegmentoUso;
import com.oceanlink.daos.NotificacionDao.ServicioAfectado;
import com.oceanlink.util.Html;
import com.oceanlink.util.Rol;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Notificaciones por correo REAL de OceanLink. Cada método busca a quién avisar, arma el mensaje HTML y lo
 * manda con CorreoService en segundo plano. Todo va dentro de try/catch: un problema con el correo nunca
 * debe impedir guardar el dato principal.
 *
 * AL PERSONAL (usuarios activos, por rol):
 *  - Incidencia registrada          -> Network Operator, Supervisor y Capacity Planner
 *  - Cambio de estado de solicitud  -> Capacity Planner y Supervisor
 *  - Mantenimiento programado       -> Maintenance Coordinator, Network Operator y Supervisor
 *  - Utilización crítica (> 95 %)   -> Capacity Planner y Supervisor
 *
 * A LOS CLIENTES (al correo del cliente, un mensaje por cliente con SUS servicios; solo clientes Activos):
 *  - Su solicitud cambia de estado (aprobada, rechazada...)
 *  - Su servicio cambia de estado (activado, suspendido, finalizado)
 *  - Incidencia (severidad Media o más) que afecta a sus servicios activos, y cuando se restablece
 *  - Mantenimiento programado que afecta a sus servicios activos
 *
 * A LOS USUARIOS NUEVOS: correo de bienvenida con su rol (sin contraseña).
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
            if (esSignificativa(inc.getSeveridad())) {
                avisarClientes(afectados,
                        "[OceanLink] Incidencia en la red que podría afectar su servicio",
                        "Incidencia en la red",
                        "Detectamos una incidencia en la red que podría afectar los siguientes servicios de su empresa. "
                                + "Nuestro equipo técnico ya está trabajando en ella y le avisaremos apenas se restablezca.",
                        filas("Incidencia", inc.getTitulo(),
                                "Severidad", inc.getSeveridad(),
                                "Estado", inc.getEstado(),
                                "Detectada", inc.getFechaDeteccionTexto()));
            }
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

            // aviso al cliente dueño de la solicitud
            if (sol.getCliente() != null) {
                com.oceanlink.beans.Clientes cli = new NotificacionDao().clienteActivoConCorreo(sol.getCliente().getIdCliente());
                if (cli != null) {
                    String correoCliente = cli.getCorreo();
                    String ruta = (sol.getEstacionOrigen() == null ? "?" : sol.getEstacionOrigen().getEtiqueta_station())
                            + " → " + (sol.getEstacionDestino() == null ? "?" : sol.getEstacionDestino().getEtiqueta_station());
                    String detalle = filas("Solicitud", sol.getEtiqueta(),
                            "Ruta", ruta,
                            "Capacidad solicitada", NUM.format(sol.getCapacidadSolicitadaGbps()) + " Gbps",
                            "Estado anterior", estadoAnterior,
                            "Estado actual", sol.getEstado());
                    CorreoService.enviarAsync(Collections.singletonList(correoCliente),
                            "[OceanLink] Su solicitud " + valor(sol.getEtiqueta()) + " ahora está: " + valor(sol.getEstado()),
                            plantilla("Actualización de su solicitud de capacidad",
                                    saludo(cli.getNombre()) + "<p style=\"margin:0 0 12px\">" + Html.esc(textoEstadoSolicitud(sol.getEstado())) + "</p>" + detalle));
                }
            }
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
            avisarClientes(afectados,
                    "[OceanLink] Mantenimiento programado que podría afectar su servicio",
                    "Mantenimiento programado",
                    "Programamos un mantenimiento de la red que podría afectar temporalmente los siguientes servicios de su empresa.",
                    filas("Tipo", m.getTipo(),
                            "Fecha programada", m.getFechaProgramadaTexto(),
                            "Duración estimada", NUM.format(m.getDuracionEstimadaHoras()) + " horas"));
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

    // ---------------------------------------------------------------- 5. Incidencia restablecida (clientes)
    /** Cuando una incidencia pasa a "Servicio restaurado" o "Cerrada", avisa a los clientes que habían sido avisados. */
    public static void incidenciaResuelta(Incidencias inc, String estadoAnterior) {
        try {
            List<String> resueltos = Arrays.asList("Servicio restaurado", "Cerrada");
            if (inc == null || estadoAnterior == null || resueltos.contains(estadoAnterior)
                    || !resueltos.contains(inc.getEstado()) || !esSignificativa(inc.getSeveridad())) {
                return;
            }
            List<ServicioAfectado> afectados = new NotificacionDao().serviciosAfectados(inc.getTipoInfra(), inc.getIdInfra());
            avisarClientes(afectados,
                    "[OceanLink] Servicio restablecido",
                    "Servicio restablecido",
                    "Le informamos que la incidencia que podía afectar los siguientes servicios de su empresa ya fue resuelta. Gracias por su paciencia.",
                    filas("Incidencia", inc.getTitulo(),
                            "Detectada", inc.getFechaDeteccionTexto(),
                            "Estado", inc.getEstado()));
        } catch (Exception e) {
            System.err.println("[Correo] Falló el aviso de servicio restablecido: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 6. Cambio de estado de un servicio (cliente)
    public static void servicioCambioEstado(Servicios s, String estadoAnterior) {
        try {
            if (s == null || estadoAnterior == null || estadoAnterior.equals(s.getEstado()) || s.getCliente() == null) {
                return;
            }
            com.oceanlink.beans.Clientes cli = new NotificacionDao().clienteActivoConCorreo(s.getCliente().getIdCliente());
            if (cli == null) {
                return;
            }
            String correoCliente = cli.getCorreo();
            String mensaje;
            switch (valor(s.getEstado())) {
                case "Activo": mensaje = "Su servicio ya está activo."; break;
                case "Suspendido": mensaje = "Su servicio fue suspendido temporalmente."; break;
                case "Finalizado": mensaje = "Su servicio ha finalizado."; break;
                default: mensaje = "El estado de su servicio cambió.";
            }
            String detalle = filas("Servicio", s.getEtiqueta(),
                    "Capacidad contratada", NUM.format(s.getCapacidadContratadaGbps()) + " Gbps",
                    "Estado anterior", estadoAnterior,
                    "Estado actual", s.getEstado(),
                    "Vigencia hasta", s.getFechaExpiracion() == null ? null : s.getFechaExpiracionTexto());
            CorreoService.enviarAsync(Collections.singletonList(correoCliente),
                    "[OceanLink] Su servicio " + valor(s.getEtiqueta()) + ": " + valor(s.getEstado()),
                    plantilla("Actualización de su servicio",
                            saludo(cli.getNombre()) + "<p style=\"margin:0 0 12px\">" + Html.esc(mensaje) + "</p>" + detalle));
        } catch (Exception e) {
            System.err.println("[Correo] Falló el aviso de servicio: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 7. Bienvenida a un usuario nuevo
    public static void usuarioCreado(String nombreCompleto, String correo, String usuario, String rol) {
        try {
            if (correo == null || correo.trim().isEmpty()) {
                return;
            }
            String cuerpo = saludo(nombreCompleto)
                    + "<p style=\"margin:0 0 12px\"><b>Tu cuenta en OceanLink ya está habilitada.</b> OceanLink es el sistema de gestión de infraestructura submarina y ya puedes ingresar.</p>"
                    + filas("Usuario", usuario, "Correo de acceso", correo, "Rol", rol)
                    + "<p style=\"margin:14px 0 0\">Ingresa con tu correo y la contraseña que te entregó el administrador. "
                    + "Por seguridad, esa contraseña no se envía por correo.</p>";
            CorreoService.enviarAsync(Collections.singletonList(correo.trim()),
                    "[OceanLink] Tu cuenta ya está habilitada", plantilla("Tu cuenta ya está habilitada", cuerpo));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la bienvenida de usuario: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 10. Servicio creado (aprobado) para un cliente
    /** Se creó un servicio a partir de una solicitud aprobada: avisa al cliente si quedó Activo o Pendiente. */
    public static void servicioRegistrado(Servicios s, Solicitudes sol) {
        try {
            if (s == null || sol == null || sol.getCliente() == null) return;
            com.oceanlink.beans.Clientes cli = new NotificacionDao().clienteActivoConCorreo(sol.getCliente().getIdCliente());
            if (cli == null) return;
            boolean activo = "Activo".equals(s.getEstado());
            String mensaje = activo
                    ? "Su solicitud fue aprobada y su servicio <b>ya está activo</b>."
                    : "Su solicitud fue aprobada y su servicio fue registrado. Está <b>pendiente de activación</b>; le avisaremos cuando quede activo.";
            String detalle = filas("Servicio", s.getEtiqueta(),
                    "Capacidad contratada", NUM.format(sol.getCapacidadSolicitadaGbps()) + " Gbps",
                    "Estado", s.getEstado(),
                    "Activación", s.getFechaActivacion() == null ? null : s.getFechaActivacionTexto(),
                    "Vigencia hasta", s.getFechaExpiracion() == null ? null : s.getFechaExpiracionTexto());
            CorreoService.enviarAsync(Collections.singletonList(cli.getCorreo()),
                    "[OceanLink] Su servicio " + valor(s.getEtiqueta()) + (activo ? " ya está activo" : " fue registrado"),
                    plantilla(activo ? "Su servicio está activo" : "Su servicio fue registrado",
                            saludo(cli.getNombre()) + "<p style=\"margin:0 0 12px\">" + mensaje + "</p>" + detalle));
        } catch (Exception e) {
            System.err.println("[Correo] Falló el aviso de servicio registrado: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 11. Comunicado manual (lo escribe un administrador)
    /**
     * Envía un comunicado escrito a mano a una lista de correos (uno por destinatario).
     * @return cuántos destinatarios distintos recibirán el correo
     */
    public static int comunicado(List<String> destinatarios, String asunto, String mensaje, String enviadoPor) {
        Set<String> unicos = new LinkedHashSet<>();
        for (String d : destinatarios) {
            if (d != null && !d.trim().isEmpty()) unicos.add(d.trim().toLowerCase(Locale.ROOT));
        }
        if (unicos.isEmpty()) return 0;
        String cuerpo = "<p style=\"margin:0 0 12px;white-space:pre-line\">" + Html.esc(mensaje) + "</p>"
                + "<p style=\"margin:16px 0 0;color:#64748b;font-size:12px\">Comunicado enviado por " + Html.esc(enviadoPor) + ".</p>";
        CorreoService.enviarAsync(new ArrayList<>(unicos), "[OceanLink] " + asunto, plantilla(asunto, cuerpo));
        return unicos.size();
    }

    // ---------------------------------------------------------------- 12. Servicios por vencer
    /**
     * Avisa a los clientes cuyos servicios Activos vencen dentro de "dias" días y manda un resumen al personal.
     * @return cuántos servicios se avisaron
     */
    public static int serviciosPorVencer(int dias, Set<String> yaAvisados) {
        try {
            List<NotificacionDao.ServicioVence> lista = new NotificacionDao().serviciosPorVencer(dias);
            Map<String, List<NotificacionDao.ServicioVence>> porCliente = new LinkedHashMap<>();
            List<NotificacionDao.ServicioVence> nuevos = new ArrayList<>();
            for (NotificacionDao.ServicioVence v : lista) {
                String clave = v.servicio + "|" + dias + "|" + v.expira;
                if (yaAvisados != null && !yaAvisados.add(clave)) continue;   // ya se avisó (control en memoria)
                nuevos.add(v);
                if (v.correoCliente != null && !v.correoCliente.trim().isEmpty()) {
                    porCliente.computeIfAbsent(v.correoCliente.trim().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(v);
                }
            }
            for (Map.Entry<String, List<NotificacionDao.ServicioVence>> e : porCliente.entrySet()) {
                StringBuilder tabla = new StringBuilder("<table style=\"border-collapse:collapse;width:100%;font-size:13px\">"
                        + "<tr style=\"background:#e2e8f0\"><th align=\"left\" style=\"padding:6px\">Servicio</th>"
                        + "<th align=\"right\" style=\"padding:6px\">Capacidad (Gbps)</th><th align=\"left\" style=\"padding:6px\">Vence</th></tr>");
                for (NotificacionDao.ServicioVence v : e.getValue()) {
                    tabla.append("<tr><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(v.servicio))
                            .append("</td><td align=\"right\" style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(NUM.format(v.capacidadGbps))
                            .append("</td><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">")
                            .append(v.expira.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
                }
                tabla.append("</table>");
                String cuerpo = saludo(e.getValue().get(0).cliente)
                        + "<p style=\"margin:0 0 12px\">Le recordamos que " + (e.getValue().size() == 1 ? "el siguiente servicio vence" : "los siguientes servicios vencen")
                        + " en <b>" + dias + " días</b>. Comuníquese con nosotros si desea renovarlo.</p>" + tabla;
                CorreoService.enviarAsync(Collections.singletonList(e.getKey()),
                        "[OceanLink] Su servicio vence en " + dias + " días", plantilla("Servicio por vencer", cuerpo));
            }
            if (!nuevos.isEmpty()) {
                List<String> personal = new NotificacionDao().correosPorRoles("Capacity Planner", "Supervisor");
                StringBuilder sb = new StringBuilder("<p style=\"margin:0 0 12px\">Servicios Activos que vencen en <b>" + dias + " días</b>:</p>"
                        + "<table style=\"border-collapse:collapse;width:100%;font-size:13px\"><tr style=\"background:#e2e8f0\">"
                        + "<th align=\"left\" style=\"padding:6px\">Servicio</th><th align=\"left\" style=\"padding:6px\">Cliente</th>"
                        + "<th align=\"left\" style=\"padding:6px\">Vence</th></tr>");
                for (NotificacionDao.ServicioVence v : nuevos) {
                    sb.append("<tr><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(v.servicio))
                            .append("</td><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(v.cliente))
                            .append("</td><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">")
                            .append(v.expira.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td></tr>");
                }
                sb.append("</table>");
                CorreoService.enviarAsync(personal, "[OceanLink] " + nuevos.size() + " servicio(s) vencen en " + dias + " días",
                        plantilla("Servicios por vencer", sb.toString()));
            }
            return nuevos.size();
        } catch (Exception e) {
            System.err.println("[Correo] Falló el aviso de servicios por vencer: " + e.getMessage());
            return 0;
        }
    }

    // ---------------------------------------------------------------- 8. Seguridad de la cuenta
    /** Un administrador cambió la contraseña de la cuenta. Nunca se incluye la contraseña. */
    public static void contrasenaCambiada(String nombre, String correo) {
        try {
            if (correo == null || correo.trim().isEmpty()) return;
            String cuerpo = saludo(nombre)
                    + "<p style=\"margin:0 0 12px\">La contraseña de tu cuenta de OceanLink fue cambiada por un administrador.</p>"
                    + "<p style=\"margin:0\">Si no esperabas este cambio, avisa de inmediato al administrador del sistema.</p>";
            CorreoService.enviarAsync(Collections.singletonList(correo.trim()),
                    "[OceanLink] Se cambió la contraseña de tu cuenta", plantilla("Contraseña actualizada", cuerpo));
        } catch (Exception e) {
            System.err.println("[Correo] Falló el aviso de cambio de contraseña: " + e.getMessage());
        }
    }

    /** La cuenta pasó a Activo (habilitada) o a Inactivo (deshabilitada). */
    public static void cuentaCambioEstado(String nombre, String correo, String estadoNuevo) {
        try {
            if (correo == null || correo.trim().isEmpty()) return;
            boolean activa = "Activo".equals(estadoNuevo);
            String cuerpo = saludo(nombre)
                    + "<p style=\"margin:0\">" + (activa
                    ? "<b>Tu cuenta de OceanLink fue habilitada nuevamente.</b> Ya puedes iniciar sesión."
                    : "<b>Tu cuenta de OceanLink fue deshabilitada.</b> Ya no podrás iniciar sesión. Si crees que es un error, comunícate con el administrador.")
                    + "</p>";
            CorreoService.enviarAsync(Collections.singletonList(correo.trim()),
                    "[OceanLink] Tu cuenta fue " + (activa ? "habilitada" : "deshabilitada"),
                    plantilla(activa ? "Cuenta habilitada" : "Cuenta deshabilitada", cuerpo));
        } catch (Exception e) {
            System.err.println("[Correo] Falló el aviso de estado de cuenta: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 9. Clientes
    /** Bienvenida a un cliente recién registrado. */
    public static void clienteRegistrado(String nombre, String correo, String estado) {
        try {
            if (correo == null || correo.trim().isEmpty() || !"Activo".equals(estado)) return;
            String cuerpo = saludo(nombre)
                    + "<p style=\"margin:0 0 12px\">Su empresa fue registrada como cliente de OceanLink. "
                    + "Desde ahora le avisaremos por este correo sobre sus solicitudes de capacidad, el estado de sus servicios, "
                    + "incidencias en la red y mantenimientos programados.</p>";
            CorreoService.enviarAsync(Collections.singletonList(correo.trim()),
                    "[OceanLink] Bienvenido/a como cliente de OceanLink", plantilla("Registro de cliente", cuerpo));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la bienvenida de cliente: " + e.getMessage());
        }
    }

    /** Confirmación al cliente de que se recibió su solicitud de capacidad. */
    public static void solicitudRecibida(Solicitudes sol) {
        try {
            if (sol == null || sol.getCliente() == null) return;
            com.oceanlink.beans.Clientes cli = new NotificacionDao().clienteActivoConCorreo(sol.getCliente().getIdCliente());
            if (cli == null) return;
            String detalle = filas("Solicitud", sol.getEtiqueta(),
                    "Capacidad solicitada", NUM.format(sol.getCapacidadSolicitadaGbps()) + " Gbps",
                    "Estado", sol.getEstado());
            CorreoService.enviarAsync(Collections.singletonList(cli.getCorreo()),
                    "[OceanLink] Recibimos su solicitud " + valor(sol.getEtiqueta()),
                    plantilla("Solicitud recibida", saludo(cli.getNombre())
                            + "<p style=\"margin:0 0 12px\">Recibimos su solicitud de capacidad. Nuestro equipo la evaluará y le avisaremos cuando cambie de estado.</p>"
                            + detalle));
        } catch (Exception e) {
            System.err.println("[Correo] Falló la confirmación de solicitud: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- apoyo para avisos a clientes

    /** Las incidencias de severidad Baja no se comunican a los clientes (para no alarmar por nada). */
    private static boolean esSignificativa(String severidad) {
        return severidad != null && !"Baja".equals(severidad);
    }

    private static String saludo(String nombre) {
        return "<p style=\"margin:0 0 12px\">Estimado/a " + Html.esc(valor(nombre)) + ",</p>";
    }

    private static String textoEstadoSolicitud(String estado) {
        switch (valor(estado)) {
            case "Aprobada": return "Su solicitud fue aprobada. Coordinaremos con usted la activación del servicio.";
            case "Rechazada": return "Lamentablemente no pudimos atender su solicitud en esta oportunidad.";
            case "En evaluación": return "Su solicitud está siendo evaluada por nuestro equipo.";
            default: return "El estado de su solicitud cambió.";
        }
    }

    /**
     * Manda UN correo por cliente (al correo del cliente) con la lista de SUS servicios activos afectados.
     * Se omiten servicios no activos, clientes inactivos y clientes sin correo.
     */
    private static void avisarClientes(List<ServicioAfectado> afectados, String asunto, String titulo,
                                       String introduccion, String detalle) {
        Map<String, List<ServicioAfectado>> porCorreo = new LinkedHashMap<>();
        for (ServicioAfectado a : afectados) {
            if (!"Activo".equals(a.estadoServicio) || !a.clienteActivo
                    || a.correoCliente == null || a.correoCliente.trim().isEmpty()) {
                continue;
            }
            porCorreo.computeIfAbsent(a.correoCliente.trim().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(a);
        }
        for (Map.Entry<String, List<ServicioAfectado>> e : porCorreo.entrySet()) {
            StringBuilder cuerpo = new StringBuilder(saludo(e.getValue().get(0).cliente));
            cuerpo.append("<p style=\"margin:0 0 12px\">").append(Html.esc(introduccion)).append("</p>").append(detalle);
            cuerpo.append("<h3 style=\"margin:16px 0 8px;font-size:15px\">Sus servicios</h3>")
                    .append("<table style=\"border-collapse:collapse;width:100%;font-size:13px\">")
                    .append("<tr style=\"background:#e2e8f0\"><th align=\"left\" style=\"padding:6px\">Servicio</th>")
                    .append("<th align=\"right\" style=\"padding:6px\">Capacidad (Gbps)</th></tr>");
            for (ServicioAfectado a : e.getValue()) {
                cuerpo.append("<tr><td style=\"padding:6px;border-bottom:1px solid #e2e8f0\">").append(Html.esc(a.servicio))
                        .append("</td><td align=\"right\" style=\"padding:6px;border-bottom:1px solid #e2e8f0\">")
                        .append(NUM.format(a.capacidadGbps)).append("</td></tr>");
            }
            cuerpo.append("</table>");
            CorreoService.enviarAsync(Collections.singletonList(e.getKey()), asunto, plantilla(titulo, cuerpo.toString()));
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
