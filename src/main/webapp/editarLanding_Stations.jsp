<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda Landing_StationsServlet (/EstacionesServlet, action=editar)
    Landing_Stations ls = (Landing_Stations) request.getAttribute("landingStations");
    String ctx = request.getContextPath();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Editar estación"/>
    <jsp:param name="encabezado" value="EDITAR ESTACIÓN"/>
    <jsp:param name="subtitulo" value="Modifica los datos de la Landing Station"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="stations"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/EstacionesServlet?action=crear">
          <input type="hidden" name="id" value="<%=ls.getId_landing_stations()%>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%=Html.esc(ls.getEtiqueta_station())%></span>
          </div>
          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;">
              <div class="form-field">
                <label for="etiquetaLandingStation">NOMBRE *</label>
                <input type="text" id="etiquetaLandingStation" name="etiquetaLandingStation"
                       value="<%=Html.esc(ls.getEtiqueta_station())%>" required>
              </div>
              <div class="form-field">
                <label for="ubicacion">UBICACIÓN *</label>
                <input type="text" id="ubicacion" name="ubicacion" value="<%=Html.esc(ls.getUbicacion())%>" required>
              </div>
              <div class="form-field">
                <label for="estado">ESTADO DE LA ESTACIÓN</label>
                <select id="estado" name="estado">
                  <option value="Operativo" <%= "Operativo".equals(ls.getEstado()) ? "selected" : "" %>>Operativo</option>
                  <option value="En mantenimiento" <%= "En mantenimiento".equals(ls.getEstado()) ? "selected" : "" %>>En mantenimiento</option>
                  <option value="Fuera de servicio" <%= "Fuera de servicio".equals(ls.getEstado()) ? "selected" : "" %>>Fuera de servicio</option>
                </select>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR CAMBIOS</button>
            <a href="<%=ctx%>/EstacionesServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
