<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda SegmentosServlet (action=lista)
    ArrayList<Segmentos> listaSegmentos = (ArrayList<Segmentos>) request.getAttribute("listaSegmentos");
    if (listaSegmentos == null) {
        listaSegmentos = new ArrayList<>();
    }
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("#,##0.##");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Segmentos"/>
    <jsp:param name="encabezado" value="SEGMENTOS"/>
    <jsp:param name="subtitulo" value="Tramos de cable entre Landing Stations"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="segmentos"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar segmentos...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por estado...</option>
              <option value="estado:Operativo">Operativo</option>
              <option value="estado:Degradado">Degradado</option>
              <option value="estado:En mantenimiento">En mantenimiento</option>
              <option value="estado:Fuera de servicio">Fuera de servicio</option>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/SegmentosServlet?action=formCrear" style="text-decoration:none;">REGISTRAR SEGMENTO</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead>
                <tr><th>NOMBRE</th><th>ORIGEN</th><th>DESTINO</th><th>ESTADO</th><th>CAPACIDAD</th><th>ACCIONES</th></tr>
              </thead>
              <tbody id="tabla-datos">
                <% if (listaSegmentos.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay segmentos registrados.</td></tr>
                <% }
                   for (Segmentos sg : listaSegmentos) {
                       String claseEstado = "badge-success";                        // Operativo
                       if ("Degradado".equals(sg.getEstado())) claseEstado = "badge-warning";
                       else if ("En mantenimiento".equals(sg.getEstado())) claseEstado = "badge-info";
                       else if ("Fuera de servicio".equals(sg.getEstado())) claseEstado = "badge-danger";
                %>
                <tr data-fila data-estado="<%=Html.esc(sg.getEstado())%>">
                  <td><%=Html.esc(sg.getEtiquetaSegmento())%></td>
                  <td><%=Html.esc(sg.getIdEstacionOrigen().getEtiqueta_station())%></td>
                  <td><%=Html.esc(sg.getIdEstacionDestino().getEtiqueta_station())%></td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(sg.getEstado())%></span></td>
                  <td><%=gbps.format(sg.getCapacidadTotalGbps())%> Gbps</td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/SegmentosServlet?action=editar&id=<%=sg.getIdSegmento()%>">Editar</a>
                      <a class="btn-delete-red" href="<%=ctx%>/SegmentosServlet?action=borrar&id=<%=sg.getIdSegmento()%>"
                         onclick="return confirm('¿Seguro que quieres eliminar este segmento?');">Borrar</a>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="segmentos">Mostrando <%=listaSegmentos.size()%> de <%=listaSegmentos.size()%> segmentos</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
