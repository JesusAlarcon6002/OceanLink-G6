<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Mantenimientos" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda MantenimientosServlet (action=lista) con request.setAttribute
    ArrayList<Mantenimientos> listaMantenimientos = (ArrayList<Mantenimientos>) request.getAttribute("listaMantenimientos");
    if (listaMantenimientos == null) {
        listaMantenimientos = new ArrayList<>();
    }
    String ctx = request.getContextPath();

    // Mensajes que llegan por la URL después de un Post/Redirect/Get (ej: ?ok=creado)
    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("creado".equals(ok)) mensajeOk = "Mantenimiento registrado correctamente.";
    else if ("actualizado".equals(ok)) mensajeOk = "Mantenimiento actualizado correctamente.";
    else if ("borrado".equals(ok)) mensajeOk = "Mantenimiento eliminado.";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Mantenimientos"/>
    <jsp:param name="encabezado" value="MANTENIMIENTOS"/>
    <jsp:param name="subtitulo" value="Lista de mantenimientos"/>
    <jsp:param name="rol" value="maintenance-coordinator"/>
    <jsp:param name="activo" value="historial-mant"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="infra-header">
            <h2>MANTENIMIENTOS</h2>
          </div>

          <% if (mensajeOk != null) { %>
          <p class="msg msg-ok"><%=mensajeOk%></p>
          <% } %>
          <% if ("borrar".equals(err)) { %>
          <p class="msg msg-error">No se pudo eliminar el mantenimiento.</p>
          <% } %>

          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar mantenimiento...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar...</option>
              <optgroup label="Tipo">
                <option value="tipo:Preventivo">Preventivo</option>
                <option value="tipo:Correctivo">Correctivo</option>
              </optgroup>
              <optgroup label="Estado">
                <option value="estado:Programado">Programado</option>
                <option value="estado:En curso">En curso</option>
                <option value="estado:Finalizado">Finalizado</option>
              </optgroup>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/MantenimientosServlet?action=formCrear" style="text-decoration:none;">REGISTRAR MANTENIMIENTO</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ID</th><th>TIPO</th><th>INFRAESTRUCTURA</th><th>FECHA PROGRAMADA</th><th>DURACIÓN</th><th>ESTADO</th><th>ACCIÓN</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaMantenimientos.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="7">Aún no hay mantenimientos registrados.</td></tr>
                <% }
                   for (Mantenimientos m : listaMantenimientos) {
                       String claseEstado = "badge-info";                        // Programado
                       if ("En curso".equals(m.getEstado())) claseEstado = "badge-warning";
                       else if ("Finalizado".equals(m.getEstado())) claseEstado = "badge-success";
                %>
                <tr data-fila data-tipo="<%=Html.esc(m.getTipo())%>" data-estado="<%=Html.esc(m.getEstado())%>">
                  <td><%=Html.esc(m.getEtiqueta())%></td>
                  <td><%=Html.esc(m.getTipo())%></td>
                  <td><%= m.getEtiquetaInfra() == null ? "—" : Html.esc(m.getEtiquetaInfra()) %></td>
                  <td><%=Html.esc(m.getFechaProgramadaTexto())%></td>
                  <td><%=m.getDuracionEstimadaHoras()%> h</td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(m.getEstado())%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/MantenimientosServlet?action=editar&id=<%=m.getIdMantenimiento()%>">Editar</a>
                      <%-- Borrar va por POST (no por link) para que un robot o un clic accidental no elimine datos --%>
                      <form method="post" action="<%=ctx%>/MantenimientosServlet?action=borrar"
                            onsubmit="return confirm('¿Seguro que quieres eliminar este mantenimiento?');">
                        <input type="hidden" name="id" value="<%=m.getIdMantenimiento()%>">
                        <button type="submit" class="btn-delete-red">Borrar</button>
                      </form>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="mantenimientos">Mostrando <%=listaMantenimientos.size()%> de <%=listaMantenimientos.size()%> mantenimientos</p>
        </div>

<script src="<%=ctx%>/assets/js/filtros.js"></script>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
