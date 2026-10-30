<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Rutas" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda RutasServlet (action=lista)
    ArrayList<Rutas> listaRutas = (ArrayList<Rutas>) request.getAttribute("listaRutas");
    if (listaRutas == null) {
        listaRutas = new ArrayList<>();
    }
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");

    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("creado".equals(ok)) mensajeOk = "Ruta registrada correctamente.";
    else if ("actualizado".equals(ok)) mensajeOk = "Ruta actualizada correctamente.";
    else if ("borrado".equals(ok)) mensajeOk = "Ruta eliminada.";
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Rutas"/>
    <jsp:param name="encabezado" value="RUTAS"/>
    <jsp:param name="subtitulo" value="Caminos de segmentos entre estaciones"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="rutas"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <% if (mensajeOk != null) { %>
          <p class="msg msg-ok"><%=mensajeOk%></p>
          <% } %>
          <% if ("borrar".equals(err)) { %>
          <p class="msg msg-error">No se pudo eliminar la ruta.</p>
          <% } else if ("servicios".equals(err)) { %>
          <p class="msg msg-error">No se puede eliminar: la ruta tiene servicios asociados. Márcala como Inactiva para que no se use en nuevos servicios.</p>
          <% } %>

          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar ruta, estación o segmento...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por estado...</option>
              <option value="estado:Operativa">Operativa</option>
              <option value="estado:Degradada">Degradada</option>
              <option value="estado:En mantenimiento">En mantenimiento</option>
              <option value="estado:Caída">Caída</option>
              <option value="estado:Inactiva">Inactiva</option>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/RutasServlet?action=formCrear" style="text-decoration:none;">REGISTRAR RUTA</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ID</th><th>ORIGEN → DESTINO</th><th>SEGMENTOS (EN ORDEN)</th><th>GBPS</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaRutas.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay rutas registradas.</td></tr>
                <% }
                   for (Rutas r : listaRutas) {
                       String est = r.getEstadoEfectivo();
                       String claseEstado = "badge-success";                          // Operativa / Activa
                       if ("Degradada".equals(est)) claseEstado = "badge-warning";
                       else if ("En mantenimiento".equals(est)) claseEstado = "badge-info";
                       else if ("Caída".equals(est)) claseEstado = "badge-danger";
                       else if ("Inactiva".equals(est)) claseEstado = "badge-neutral";
                %>
                <tr data-fila data-estado="<%=Html.esc(est)%>">
                  <td><%=Html.esc(r.getEtiqueta())%></td>
                  <td><%=Html.esc(r.getEstacionOrigen().getEtiqueta_station())%> → <%=Html.esc(r.getEstacionDestino().getEtiqueta_station())%></td>
                  <td><%=Html.esc(r.getSegmentosTexto())%></td>
                  <td><%=gbps.format(r.getCapacidadDisponible())%></td>
                  <td>
                    <span class="badge <%=claseEstado%>" <%= r.getMotivo() != null ? "title=\"" + Html.esc(r.getMotivo()) + "\"" : "" %>><%=Html.esc(est)%></span>
                  </td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/RutasServlet?action=editar&id=<%=r.getIdRuta()%>">Editar</a>
                      <form method="post" action="<%=ctx%>/RutasServlet?action=borrar"
                            onsubmit="return confirm('¿Seguro que quieres eliminar esta ruta?');">
                        <input type="hidden" name="id" value="<%=r.getIdRuta()%>">
                        <button type="submit" class="btn-delete-red">Borrar</button>
                      </form>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="rutas">Mostrando <%=listaRutas.size()%> de <%=listaRutas.size()%> rutas</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
