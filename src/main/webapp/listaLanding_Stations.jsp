<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda Landing_StationsServlet (/EstacionesServlet, action=lista)
    ArrayList<Landing_Stations> listaLanding_Stations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    if (listaLanding_Stations == null) {
        listaLanding_Stations = new ArrayList<>();
    }
    String ctx = request.getContextPath();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Landing Stations"/>
    <jsp:param name="encabezado" value="LANDING STATIONS"/>
    <jsp:param name="subtitulo" value="Estaciones de amarre de los cables submarinos"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="stations"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar estaciones...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por estado...</option>
              <option value="estado:Operativo">Operativo</option>
              <option value="estado:En mantenimiento">En mantenimiento</option>
              <option value="estado:Fuera de servicio">Fuera de servicio</option>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/EstacionesServlet?action=formCrear" style="text-decoration:none;">REGISTRAR ESTACIONES</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead>
                <tr><th>NOMBRE</th><th>UBICACIÓN</th><th>SEGMENTOS</th><th>ESTADO</th><th>ACCIONES</th></tr>
              </thead>
              <tbody id="tabla-datos">
                <% if (listaLanding_Stations.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="5">Aún no hay estaciones registradas.</td></tr>
                <% }
                   for (Landing_Stations station : listaLanding_Stations) {
                       String claseEstado = "badge-success";                       // Operativo
                       if ("En mantenimiento".equals(station.getEstado())) claseEstado = "badge-warning";
                       else if ("Fuera de servicio".equals(station.getEstado())) claseEstado = "badge-danger";
                %>
                <tr data-fila data-estado="<%=Html.esc(station.getEstado())%>">
                  <td><%=Html.esc(station.getEtiqueta_station())%></td>
                  <td><%=Html.esc(station.getUbicacion())%></td>
                  <td><%=station.getTotalSegmentos()%></td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(station.getEstado())%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/EstacionesServlet?action=editar&id=<%=station.getId_landing_stations()%>">Editar</a>
                      <a class="btn-delete-red" href="<%=ctx%>/EstacionesServlet?action=borrar&id=<%=station.getId_landing_stations()%>"
                         onclick="return confirm('¿Seguro que quieres eliminar esta estación?');">Borrar</a>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="estaciones">Mostrando <%=listaLanding_Stations.size()%> de <%=listaLanding_Stations.size()%> estaciones</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
