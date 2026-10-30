<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Servicios" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda ServiciosServlet (action=lista)
    ArrayList<Servicios> listaServicios = (ArrayList<Servicios>) request.getAttribute("listaServicios");
    if (listaServicios == null) {
        listaServicios = new ArrayList<>();
    }
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");

    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("creado".equals(ok)) mensajeOk = "Servicio creado correctamente.";
    else if ("actualizado".equals(ok)) mensajeOk = "Servicio actualizado correctamente.";
    else if ("borrado".equals(ok)) mensajeOk = "Servicio eliminado. La capacidad sigue reservada en la solicitud.";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Servicios"/>
    <jsp:param name="encabezado" value="SERVICIOS"/>
    <jsp:param name="subtitulo" value="Servicios contratados por los clientes"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="servicios"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <% if (mensajeOk != null) { %>
          <p class="msg msg-ok"><%=mensajeOk%></p>
          <% } %>
          <% if (err != null && !err.isEmpty()) { %>
          <p class="msg msg-error"><%=Html.esc(err)%></p>
          <% } %>

          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar servicio, cliente o ruta...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por estado...</option>
              <option value="estado:Pendiente">Pendiente</option>
              <option value="estado:Activo">Activo</option>
              <option value="estado:Suspendido">Suspendido</option>
              <option value="estado:Finalizado">Finalizado</option>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/ServiciosServlet?action=formCrear" style="text-decoration:none;">REGISTRAR SERVICIO</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ID</th><th>CLIENTE</th><th>RUTA</th><th>GBPS</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaServicios.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay servicios registrados.</td></tr>
                <% }
                   for (Servicios s : listaServicios) {
                       String est = s.getEstado() == null ? "" : s.getEstado();
                       String clase = "badge-neutral";                                // Finalizado
                       if ("Activo".equals(est)) clase = "badge-success";
                       else if ("Pendiente".equals(est)) clase = "badge-warning";
                       else if ("Suspendido".equals(est)) clase = "badge-danger";
                %>
                <tr data-fila data-estado="<%=Html.esc(est)%>">
                  <td><%=Html.esc(s.getEtiqueta())%></td>
                  <td><%=Html.esc(s.getCliente().getNombre())%></td>
                  <td><%=Html.esc(s.getEtiquetaRuta())%></td>
                  <td><%=gbps.format(s.getCapacidadContratadaGbps())%></td>
                  <td><span class="badge <%=clase%>"><%=Html.esc(est)%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/ServiciosServlet?action=editar&id=<%=s.getIdServicio()%>">Editar</a>
                      <% if ("Pendiente".equals(est)) { %>
                      <form method="post" action="<%=ctx%>/ServiciosServlet?action=borrar"
                            onsubmit="return confirm('¿Seguro que quieres eliminar este servicio?');">
                        <input type="hidden" name="id" value="<%=s.getIdServicio()%>">
                        <button type="submit" class="btn-delete-red">Borrar</button>
                      </form>
                      <% } %>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="servicios">Mostrando <%=listaServicios.size()%> de <%=listaServicios.size()%> servicios</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
