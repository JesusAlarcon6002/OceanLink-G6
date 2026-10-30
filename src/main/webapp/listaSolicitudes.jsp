<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Solicitudes" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda SolicitudesServlet (action=lista)
    ArrayList<Solicitudes> listaSolicitudes = (ArrayList<Solicitudes>) request.getAttribute("listaSolicitudes");
    if (listaSolicitudes == null) {
        listaSolicitudes = new ArrayList<>();
    }
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");

    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("creado".equals(ok)) mensajeOk = "Solicitud registrada correctamente.";
    else if ("actualizado".equals(ok)) mensajeOk = "Solicitud actualizada correctamente.";
    else if ("borrado".equals(ok)) mensajeOk = "Solicitud eliminada.";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Solicitudes de capacidad"/>
    <jsp:param name="encabezado" value="SOLICITUDES DE CAPACIDAD"/>
    <jsp:param name="subtitulo" value="Solicitudes de los clientes"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="solicitudes"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <% if (mensajeOk != null) { %>
          <p class="msg msg-ok"><%=mensajeOk%></p>
          <% } %>
          <% if ("borrar".equals(err)) { %>
          <p class="msg msg-error">No se pudo eliminar la solicitud.</p>
          <% } else if ("reservas".equals(err)) { %>
          <p class="msg msg-error">No se puede eliminar: la solicitud ya tiene capacidad reservada en segmentos. Cambia su estado a Rechazada si ya no se va a atender.</p>
          <% } %>

          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar solicitud o cliente...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por estado...</option>
              <option value="estado:Registrada">Registrada</option>
              <option value="estado:En evaluación">En evaluación</option>
              <option value="estado:Aprobada">Aprobada</option>
              <option value="estado:Rechazada">Rechazada</option>
              <option value="estado:Provisionada">Provisionada</option>
              <option value="estado:Activa">Activa</option>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/SolicitudesServlet?action=formCrear" style="text-decoration:none;">REGISTRAR SOLICITUD</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ID</th><th>CLIENTE</th><th>RUTA</th><th>GBPS</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaSolicitudes.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay solicitudes registradas.</td></tr>
                <% }
                   for (Solicitudes sol : listaSolicitudes) {
                       String est = sol.getEstado() == null ? "" : sol.getEstado();
                       String claseEstado = "badge-neutral";                          // Registrada
                       if ("En evaluación".equals(est)) claseEstado = "badge-warning";
                       else if ("Aprobada".equals(est) || "Activa".equals(est)) claseEstado = "badge-success";
                       else if ("Provisionada".equals(est)) claseEstado = "badge-info";
                       else if ("Rechazada".equals(est)) claseEstado = "badge-danger";
                %>
                <tr data-fila data-estado="<%=Html.esc(est)%>">
                  <td><%=Html.esc(sol.getEtiqueta())%></td>
                  <td><%=Html.esc(sol.getCliente().getNombre())%></td>
                  <td><%=Html.esc(sol.getEstacionOrigen().getEtiqueta_station())%> → <%=Html.esc(sol.getEstacionDestino().getEtiqueta_station())%></td>
                  <td><%=gbps.format(sol.getCapacidadSolicitadaGbps())%></td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(est)%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/SolicitudesServlet?action=editar&id=<%=sol.getIdSolicitud()%>">Editar</a>
                      <a class="btn-edit-white" href="<%=ctx%>/ReservasServlet?solicitud=<%=sol.getIdSolicitud()%>">Capacidad</a>
                      <form method="post" action="<%=ctx%>/SolicitudesServlet?action=borrar"
                            onsubmit="return confirm('¿Seguro que quieres eliminar esta solicitud?');">
                        <input type="hidden" name="id" value="<%=sol.getIdSolicitud()%>">
                        <button type="submit" class="btn-delete-red">Borrar</button>
                      </form>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="solicitudes">Mostrando <%=listaSolicitudes.size()%> de <%=listaSolicitudes.size()%> solicitudes</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
