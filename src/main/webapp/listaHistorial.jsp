<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Historial" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda HistorialServlet. Solo lectura: las filas las escribe el sistema al reservar, liberar o cambiar un servicio.
    ArrayList<Historial> listaHistorial = (ArrayList<Historial>) request.getAttribute("listaHistorial");
    if (listaHistorial == null) {
        listaHistorial = new ArrayList<>();
    }
    String ctx = request.getContextPath();
    DecimalFormat gbps = new DecimalFormat("+#,##0.##;-#,##0.##");
    DecimalFormat num = new DecimalFormat("#,##0.##");
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Historial de utilización"/>
    <jsp:param name="encabezado" value="HISTORIAL DE UTILIZACIÓN"/>
    <jsp:param name="subtitulo" value="Movimientos de capacidad por segmento"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="historial"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar segmento, solicitud o servicio...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por movimiento...</option>
              <option value="tipo:Reserva de capacidad">Reserva de capacidad</option>
              <option value="tipo:Liberación de capacidad">Liberación de capacidad</option>
              <option value="tipo:Inicio de servicio">Inicio de servicio</option>
              <option value="tipo:Fin de servicio">Fin de servicio</option>
              <option value="tipo:Ajuste técnico">Ajuste técnico</option>
            </select>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>FECHA</th><th>SEGMENTO</th><th>MOVIMIENTO</th><th>CAMBIO</th><th>USO %</th><th>LIBRES</th><th>REF.</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaHistorial.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="7">Aún no hay movimientos registrados.</td></tr>
                <% }
                   for (Historial h : listaHistorial) {
                       String ref = h.getEtiquetaServicio() != null ? h.getEtiquetaServicio()
                               : (h.getEtiquetaSolicitud() == null ? "—" : h.getEtiquetaSolicitud());
                %>
                <tr data-fila data-tipo="<%=Html.esc(h.getTipoMovimiento())%>">
                  <td><%=Html.esc(h.getFechaTexto())%></td>
                  <td><%=Html.esc(h.getEtiquetaSegmento())%></td>
                  <td><%=Html.esc(h.getTipoMovimiento())%></td>
                  <td><%=gbps.format(h.getCapacidadCambioGbps())%></td>
                  <td><%=num.format(h.getPorcentajeUtilizacion())%>%</td>
                  <td><%=num.format(h.getCapacidadResultanteGbps())%></td>
                  <td><%=Html.esc(ref)%></td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="movimientos">Mostrando <%=listaHistorial.size()%> de <%=listaHistorial.size()%> movimientos</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
