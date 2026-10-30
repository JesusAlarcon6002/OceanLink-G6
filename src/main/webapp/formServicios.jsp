<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.oceanlink.beans.Rutas" %>
<%@ page import="com.oceanlink.beans.Servicios" %>
<%@ page import="com.oceanlink.beans.Solicitudes" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Tres modos que decide ServiciosServlet: "elegir" (qué solicitud convertir), "crear" y "editar".
    String modo = (String) request.getAttribute("modo");
    boolean elegir = "elegir".equals(modo);
    boolean editando = "editar".equals(modo);
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");
    ArrayList<String> errores = (ArrayList<String>) request.getAttribute("errores");

    Servicios sv = (Servicios) request.getAttribute("servicio");
    Solicitudes sol = (Solicitudes) request.getAttribute("solicitud");
    List<Rutas> rutas = (List<Rutas>) request.getAttribute("rutasCompatibles");
    List<Solicitudes> elegibles = (List<Solicitudes>) request.getAttribute("solicitudesElegibles");
    List<String> estados = (List<String>) request.getAttribute("estadosDisponibles");
    Double capReservada = (Double) request.getAttribute("capacidadReservada");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar Servicio"/>
    <jsp:param name="encabezado" value="REGISTRAR/EDITAR SERVICIO"/>
    <jsp:param name="subtitulo" value="Servicio de un cliente sobre una ruta"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="servicios"/>
</jsp:include>

<% if (elegir) { %>
        <div class="form-panel-dark" style="min-height:auto;">
          <div class="form-panel-header"><span style="color:#fff;font-size:16px;font-weight:700;">ELIGE LA SOLICITUD</span></div>
          <p style="color:#c9d4e3;margin:0 0 12px;">Un servicio nace de una solicitud <strong>Aprobada</strong> con capacidad ya reservada. Si la tuya no aparece, primero reserva su capacidad desde Solicitudes → Capacidad.</p>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>SOLICITUD</th><th>CLIENTE</th><th>RUTA</th><th>GBPS</th><th>ACCIÓN</th></tr></thead>
              <tbody>
                <% if (elegibles == null || elegibles.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="5">No hay solicitudes aprobadas pendientes de servicio.</td></tr>
                <% } else for (Solicitudes s : elegibles) { %>
                <tr>
                  <td><%=Html.esc(s.getEtiqueta())%></td>
                  <td><%=Html.esc(s.getCliente().getNombre())%></td>
                  <td><%=Html.esc(s.getEstacionOrigen().getEtiqueta_station())%> → <%=Html.esc(s.getEstacionDestino().getEtiqueta_station())%></td>
                  <td><%=gbps.format(s.getCapacidadSolicitadaGbps())%></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/ServiciosServlet?action=formCrear&solicitud=<%=s.getIdSolicitud()%>">Crear servicio</a></td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <div class="form-actions">
            <a href="<%=ctx%>/ServiciosServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">VOLVER</a>
          </div>
        </div>
<% } else { %>
        <form class="form-panel-dark" method="post" action="<%=ctx%>/ServiciosServlet?action=guardar">
          <input type="hidden" name="id" value="<%= editando ? sv.getIdServicio() : "" %>">
          <% if (!editando) { %><input type="hidden" name="solicitud" value="<%=sol.getIdSolicitud()%>"><% } %>

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%= editando ? Html.esc(sv.getEtiqueta()) : "NUEVO SERVICIO" %></span>
          </div>

          <% if (errores != null && !errores.isEmpty()) { %>
          <div class="msg msg-error">
            No se pudo guardar. Revisa lo siguiente:
            <ul><% for (String e : errores) { %><li><%=Html.esc(e)%></li><% } %></ul>
          </div>
          <% } %>

          <p class="form-section-label">INFORMACIÓN GENERAL</p>
          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;flex:1 1 100%;">
              <div class="form-field">
                <label for="etiqueta">ETIQUETA *</label>
                <input type="text" id="etiqueta" name="etiqueta" maxlength="100" required placeholder="SRV-001"
                       value="<%=Html.esc(sv.getEtiqueta())%>">
              </div>
              <div class="form-field">
                <label>CLIENTE</label>
                <input type="text" disabled value="<%= editando ? Html.esc(sv.getCliente().getNombre()) : Html.esc(sol.getCliente().getNombre()) %>">
              </div>
              <div class="form-field">
                <label for="ruta">RUTA *</label>
                <% if (editando) { %>
                <input type="text" disabled value="<%=Html.esc(sv.getEtiquetaRuta())%>">
                <% } else { %>
                <select id="ruta" name="ruta" required>
                  <% if (rutas == null || rutas.isEmpty()) { %>
                  <option value="">No hay una ruta registrada con los segmentos reservados</option>
                  <% } else for (Rutas r : rutas) { %>
                  <option value="<%=r.getIdRuta()%>" <%= r.getIdRuta() == sv.getIdRuta() ? "selected" : "" %>><%=Html.esc(r.getEtiqueta())%> — <%=Html.esc(r.getSegmentosTexto())%></option>
                  <% } %>
                </select>
                <% } %>
              </div>
              <div class="form-field">
                <label>CAPACIDAD CONTRATADA (GBPS)</label>
                <input type="text" disabled value="<%= editando ? gbps.format(sv.getCapacidadContratadaGbps()) : gbps.format(capReservada == null ? 0 : capReservada) %>">
              </div>
              <div class="form-field">
                <label for="fechaActivacion">FECHA DE ACTIVACIÓN</label>
                <input type="date" id="fechaActivacion" name="fechaActivacion" value="<%=sv.getFechaActivacionInput()%>">
              </div>
              <div class="form-field">
                <label for="fechaExpiracion">FECHA DE EXPIRACIÓN</label>
                <input type="date" id="fechaExpiracion" name="fechaExpiracion" value="<%=sv.getFechaExpiracionInput()%>">
              </div>
              <div class="form-field">
                <label for="estado">ESTADO</label>
                <select id="estado" name="estado">
                  <% for (String e : estados) { %>
                  <option value="<%=e%>" <%= e.equals(sv.getEstado()) ? "selected" : "" %>><%=e%></option>
                  <% } %>
                </select>
              </div>
              <div class="form-field">
                <label>SOLICITUD DE ORIGEN</label>
                <input type="text" disabled value="<%= editando ? Html.esc(sv.getEtiquetaSolicitud()) : Html.esc(sol.getEtiqueta()) %>">
              </div>
            </div>

            <div class="info-card-dark" style="flex:1 1 100%;max-width:none;box-sizing:border-box;">
              <h4>CÓMO SE MUEVE LA CAPACIDAD</h4>
              <p><strong>Pendiente</strong>: la capacidad está reservada en los segmentos. <strong>Activo</strong>: pasa a utilizada (se anota "Inicio de servicio" en el historial). <strong>Suspendido</strong>: sigue ocupada. <strong>Finalizado</strong>: se libera y ya no se puede reactivar. Activar un servicio no se permite si la ruta está caída o en mantenimiento.</p>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save" <%= (!editando && (rutas == null || rutas.isEmpty())) ? "disabled" : "" %>>GUARDAR</button>
            <a href="<%=ctx%>/ServiciosServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>
<% } %>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
