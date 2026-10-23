<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda SegmentosServlet (action=formCrear)
    ArrayList<Landing_Stations> listaLanding_Stations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
    String ctx = request.getContextPath();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar segmento"/>
    <jsp:param name="encabezado" value="REGISTRAR SEGMENTO"/>
    <jsp:param name="subtitulo" value="Registra un nuevo tramo de cable"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="segmentos"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/SegmentosServlet?action=crear" id="form-segmento">
          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;">NUEVO SEGMENTO</span>
          </div>
          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;">
              <div class="form-field">
                <label for="etiquetaSegmento">NOMBRE *</label>
                <input type="text" id="etiquetaSegmento" name="etiquetaSegmento" placeholder="SEG-B16" required>
              </div>
              <div class="form-field">
                <label for="capacidadTotal">CAPACIDAD TOTAL (GBPS) *</label>
                <input type="number" id="capacidadTotal" name="capacidadTotal" step="0.01" min="0" placeholder="40" required>
              </div>
              <div class="form-field">
                <label for="estacionOrigen">EST. ORIGEN *</label>
                <select id="estacionOrigen" name="estacionOrigen" required>
                  <% if (listaLanding_Stations != null) {
                      for (Landing_Stations ls : listaLanding_Stations) { %>
                  <option value="<%=ls.getId_landing_stations()%>"><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <%   }
                     } %>
                </select>
              </div>
              <div class="form-field">
                <label for="estacionDestino">EST. DESTINO *</label>
                <select id="estacionDestino" name="estacionDestino" required>
                  <% if (listaLanding_Stations != null) {
                      for (Landing_Stations ls : listaLanding_Stations) { %>
                  <option value="<%=ls.getId_landing_stations()%>"><%=Html.esc(ls.getEtiqueta_station())%></option>
                  <%   }
                     } %>
                </select>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">REGISTRAR</button>
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
