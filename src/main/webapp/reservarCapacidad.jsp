<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.oceanlink.beans.Rutas" %>
<%@ page import="com.oceanlink.beans.Solicitudes" %>
<%@ page import="com.oceanlink.daos.ReservasDao" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda ReservasServlet
    Solicitudes sol = (Solicitudes) request.getAttribute("solicitud");
    List<ReservasDao.Reserva> reservas = (List<ReservasDao.Reserva>) request.getAttribute("reservas");
    List<Rutas> rutas = (List<Rutas>) request.getAttribute("rutasPosibles");
    boolean tieneServicio = Boolean.TRUE.equals(request.getAttribute("tieneServicio"));
    if (reservas == null) reservas = new ArrayList<>();
    if (rutas == null) rutas = new ArrayList<>();
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");
    boolean reservada = !reservas.isEmpty();
    String estadoSol = sol.getEstado() == null ? "" : sol.getEstado();
    boolean reservable = "Registrada".equals(estadoSol) || "En evaluación".equals(estadoSol) || "Aprobada".equals(estadoSol);

    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("reservado".equals(ok)) mensajeOk = "Capacidad reservada. La solicitud quedó Aprobada.";
    else if ("liberado".equals(ok)) mensajeOk = "Reserva liberada. La solicitud volvió a En evaluación.";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Reservar capacidad"/>
    <jsp:param name="encabezado" value="RESERVAR CAPACIDAD"/>
    <jsp:param name="subtitulo" value="Asigna una ruta a la solicitud"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="solicitudes"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <% if (mensajeOk != null) { %><p class="msg msg-ok"><%=mensajeOk%></p><% } %>
          <% if (err != null && !err.isEmpty()) { %><p class="msg msg-error"><%=Html.esc(err)%></p><% } %>

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%=Html.esc(sol.getEtiqueta())%> · <%=Html.esc(sol.getCliente().getNombre())%></span>
          </div>
          <p style="color:#c9d4e3;margin:0 0 14px;">
            <%=Html.esc(sol.getEstacionOrigen().getEtiqueta_station())%> → <%=Html.esc(sol.getEstacionDestino().getEtiqueta_station())%>
            · <strong><%=gbps.format(sol.getCapacidadSolicitadaGbps())%> Gbps</strong>
            · Estado: <strong><%=Html.esc(estadoSol)%></strong>
          </p>

          <% if (reservada) { %>
          <p class="form-section-label">CAPACIDAD RESERVADA</p>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>SEGMENTO</th><th>GBPS RESERVADOS</th></tr></thead>
              <tbody>
                <% for (ReservasDao.Reserva r : reservas) { %>
                <tr><td><%=Html.esc(r.etiquetaSegmento)%></td><td><%=gbps.format(r.capacidad)%></td></tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <div class="form-actions">
            <% if (tieneServicio) { %>
            <p class="msg msg-ok" style="margin:0;">Ya existe un servicio creado desde esta solicitud, no se puede liberar.</p>
            <% } else { %>
            <a class="btn-form-save" style="text-decoration:none;display:inline-flex;align-items:center;" href="<%=ctx%>/ServiciosServlet?action=formCrear&solicitud=<%=sol.getIdSolicitud()%>">CREAR SERVICIO</a>
            <form method="post" action="<%=ctx%>/ReservasServlet?action=liberar" style="margin:0;"
                  onsubmit="return confirm('¿Liberar la capacidad reservada de esta solicitud?');">
              <input type="hidden" name="solicitud" value="<%=sol.getIdSolicitud()%>">
              <button type="submit" class="btn-delete-red">LIBERAR RESERVA</button>
            </form>
            <% } %>
            <a href="<%=ctx%>/SolicitudesServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">VOLVER</a>
          </div>

          <% } else { %>
          <p class="form-section-label">RUTAS REGISTRADAS ENTRE ESTAS ESTACIONES</p>
          <% if (!reservable) { %>
          <p class="msg msg-error">Una solicitud en estado <%=Html.esc(estadoSol)%> no admite nuevas reservas.</p>
          <% } %>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>RUTA</th><th>SEGMENTOS</th><th>GBPS LIBRES</th><th>ESTADO</th><th>ACCIÓN</th></tr></thead>
              <tbody>
                <% if (rutas.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="5">No hay rutas registradas entre estas estaciones.</td></tr>
                <% }
                   for (Rutas r : rutas) {
                       boolean alcanza = r.getCapacidadDisponible() + 1e-9 >= sol.getCapacidadSolicitadaGbps();
                       boolean puede = reservable && r.isUsableParaNuevos() && alcanza;
                       String est = r.getEstadoEfectivo();
                       String clase = "badge-success";
                       if ("Degradada".equals(est)) clase = "badge-warning";
                       else if ("En mantenimiento".equals(est)) clase = "badge-info";
                       else if ("Caída".equals(est)) clase = "badge-danger";
                       else if ("Inactiva".equals(est)) clase = "badge-neutral";
                %>
                <tr>
                  <td><%=Html.esc(r.getEtiqueta())%></td>
                  <td><%=Html.esc(r.getSegmentosTexto())%></td>
                  <td><%=gbps.format(r.getCapacidadDisponible())%></td>
                  <td><span class="badge <%=clase%>" <%= r.getMotivo() != null ? "title=\"" + Html.esc(r.getMotivo()) + "\"" : "" %>><%=Html.esc(est)%></span></td>
                  <td>
                    <% if (puede) { %>
                    <form method="post" action="<%=ctx%>/ReservasServlet?action=reservar" style="margin:0;"
                          onsubmit="return confirm('¿Reservar <%=gbps.format(sol.getCapacidadSolicitadaGbps())%> Gbps en esta ruta?');">
                      <input type="hidden" name="solicitud" value="<%=sol.getIdSolicitud()%>">
                      <input type="hidden" name="ruta" value="<%=r.getIdRuta()%>">
                      <button type="submit" class="btn-mini">Reservar</button>
                    </form>
                    <% } else { %>
                    <span style="opacity:.7;font-size:12px;"><%= !r.isUsableParaNuevos() ? "No disponible" : (!alcanza ? "Sin capacidad" : "—") %></span>
                    <% } %>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <div class="form-actions">
            <a class="btn-form-save" style="text-decoration:none;display:inline-flex;align-items:center;"
               href="<%=ctx%>/RutasServlet?action=formCrear&solicitud=<%=sol.getIdSolicitud()%>">REGISTRAR / SUGERIR RUTA</a>
            <a href="<%=ctx%>/SolicitudesServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">VOLVER</a>
          </div>
          <% } %>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
