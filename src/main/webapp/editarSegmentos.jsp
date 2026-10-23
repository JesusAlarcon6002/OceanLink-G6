<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda SegmentosServlet (action=editar)
    Segmentos sg = (Segmentos) request.getAttribute("segmentos");
    ArrayList<Landing_Stations> listaLanding_Stations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    String ctx = request.getContextPath();
    int idOrigen = sg.getIdEstacionOrigen().getId_landing_stations();
    int idDestino = sg.getIdEstacionDestino().getId_landing_stations();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Editar segmento"/>
    <jsp:param name="encabezado" value="EDITAR SEGMENTO"/>
    <jsp:param name="subtitulo" value="Modifica los datos del tramo de cable"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="segmentos"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/SegmentosServlet?action=crear" id="form-segmento">
          <input type="hidden" name="id" value="<%=sg.getIdSegmento()%>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%=Html.esc(sg.getEtiquetaSegmento())%></span>
          </div>
          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;">
              <div class="form-field">
                <label for="etiquetaSegmento">NOMBRE *</label>
                <input type="text" id="etiquetaSegmento" name="etiquetaSegmento" value="<%=Html.esc(sg.getEtiquetaSegmento())%>" required>
              </div>
              <div class="form-field">
                <label for="capacidadTotal">CAPACIDAD TOTAL (GBPS) *</label>
                <input type="number" id="capacidadTotal" name="capacidadTotal" step="0.01" min="0"
                       value="<%=sg.getCapacidadTotalGbps()%>" required>
              </div>
              <div class="form-field">
                <label for="estacionOrigen">EST. ORIGEN *</label>
                <select id="estacionOrigen" name="estacionOrigen" required>
                  <% if (listaLanding_Stations != null) {
                      for (Landing_Stations ls : listaLanding_Stations) { %>
                  <option value="<%=ls.getId_landing_stations()%>" <%= ls.getId_landing_stations() == idOrigen ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <%   }
                     } %>
                </select>
              </div>
              <div class="form-field">
                <label for="estacionDestino">EST. DESTINO *</label>
                <select id="estacionDestino" name="estacionDestino" required>
                  <% if (listaLanding_Stations != null) {
                      for (Landing_Stations ls : listaLanding_Stations) { %>
                  <option value="<%=ls.getId_landing_stations()%>" <%= ls.getId_landing_stations() == idDestino ? "selected" : "" %>><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <%   }
                     } %>
                </select>
              </div>
              <div class="form-field">
                <label for="estado">ESTADO DEL SEGMENTO</label>
                <select id="estado" name="estado">
                  <option value="Operativo" <%= "Operativo".equals(sg.getEstado()) ? "selected" : "" %>>Operativo</option>
                  <option value="Degradado" <%= "Degradado".equals(sg.getEstado()) ? "selected" : "" %>>Degradado</option>
                  <option value="En mantenimiento" <%= "En mantenimiento".equals(sg.getEstado()) ? "selected" : "" %>>En mantenimiento</option>
                  <option value="Fuera de servicio" <%= "Fuera de servicio".equals(sg.getEstado()) ? "selected" : "" %>>Fuera de servicio</option>
                </select>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR CAMBIOS</button>
            <a href="<%=ctx%>/SegmentosServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<script>
  // Un segmento no puede salir y llegar a la misma estación
  document.getElementById('form-segmento').addEventListener('submit', function (e) {
    if (document.getElementById('estacionOrigen').value === document.getElementById('estacionDestino').value) {
      e.preventDefault();
      alert('La estación de origen y la de destino deben ser distintas.');
    }
  });
</script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
