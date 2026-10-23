<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Incidencias" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda IncidenciasServlet (action=lista)
    ArrayList<Incidencias> listaIncidencias = (ArrayList<Incidencias>) request.getAttribute("listaIncidencias");
    if (listaIncidencias == null) {
        listaIncidencias = new ArrayList<>();
    }
    String ctx = request.getContextPath();

    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("creado".equals(ok)) mensajeOk = "Incidencia registrada correctamente.";
    else if ("actualizado".equals(ok)) mensajeOk = "Incidencia actualizada correctamente.";
    else if ("borrado".equals(ok)) mensajeOk = "Incidencia eliminada.";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Incidencias"/>
    <jsp:param name="encabezado" value="INCIDENCIAS"/>
    <jsp:param name="subtitulo" value="Incidencias de la red"/>
    <jsp:param name="rol" value="network-operator"/>
    <jsp:param name="activo" value="incidencias"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="infra-header">
            <h2>INCIDENCIAS</h2>
          </div>

          <% if (mensajeOk != null) { %>
          <p class="msg msg-ok"><%=mensajeOk%></p>
          <% } %>
          <% if ("borrar".equals(err)) { %>
          <p class="msg msg-error">No se pudo eliminar la incidencia.</p>
          <% } %>

          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar incidencia...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar...</option>
              <optgroup label="Severidad">
                <option value="severidad:Critica">Crítica</option>
                <option value="severidad:Alta">Alta</option>
                <option value="severidad:Media">Media</option>
                <option value="severidad:Baja">Baja</option>
              </optgroup>
              <optgroup label="Estado">
                <option value="estado:Detectada">Detectada</option>
                <option value="estado:En analisis">En análisis</option>
                <option value="estado:Reparacion programada">Reparación programada</option>
                <option value="estado:En reparacion">En reparación</option>
                <option value="estado:Servicio restaurado">Servicio restaurado</option>
                <option value="estado:Cerrada">Cerrada</option>
              </optgroup>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/IncidenciasServlet?action=formCrear" style="text-decoration:none;">REGISTRAR INCIDENCIA</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ID</th><th>TÍTULO</th><th>INFRAESTRUCTURA</th><th>SEVERIDAD</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaIncidencias.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay incidencias registradas.</td></tr>
                <% }
                   for (Incidencias inc : listaIncidencias) {
                       String claseEstado = "badge-danger";                              // Detectada
                       String est = inc.getEstado() == null ? "" : inc.getEstado();
                       if ("En analisis".equals(est)) claseEstado = "badge-warning";
                       else if ("Reparacion programada".equals(est) || "En reparacion".equals(est)) claseEstado = "badge-info";
                       else if ("Servicio restaurado".equals(est)) claseEstado = "badge-success";
                       else if ("Cerrada".equals(est)) claseEstado = "badge-neutral";
                %>
                <tr data-fila data-severidad="<%=Html.esc(inc.getSeveridad())%>" data-estado="<%=Html.esc(inc.getEstado())%>">
                  <td><%=Html.esc(inc.getEtiqueta())%></td>
                  <td><%=Html.esc(inc.getTitulo())%></td>
                  <td><%= inc.getEtiquetaInfra() == null ? "—" : Html.esc(inc.getEtiquetaInfra()) %></td>
                  <td><%=Html.esc(Incidencias.textoVisible(inc.getSeveridad()))%></td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(Incidencias.textoVisible(inc.getEstado()))%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/IncidenciasServlet?action=editar&id=<%=inc.getIdIncidencia()%>">Editar</a>
                      <form method="post" action="<%=ctx%>/IncidenciasServlet?action=borrar"
                            onsubmit="return confirm('¿Seguro que quieres eliminar esta incidencia?');">
                        <input type="hidden" name="id" value="<%=inc.getIdIncidencia()%>">
                        <button type="submit" class="btn-delete-red">Borrar</button>
                      </form>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="incidencias">Mostrando <%=listaIncidencias.size()%> de <%=listaIncidencias.size()%> incidencias</p>
        </div>

<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
