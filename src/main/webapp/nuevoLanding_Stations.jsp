<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    // Lo manda Landing_StationsServlet (/EstacionesServlet, action=formCrear)
    String ctx = request.getContextPath();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar estación"/>
    <jsp:param name="encabezado" value="REGISTRAR ESTACIÓN"/>
    <jsp:param name="subtitulo" value="Registra una nueva Landing Station"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="stations"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/EstacionesServlet?action=crear">
          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;">NUEVA ESTACIÓN</span>
          </div>
          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2" style="grid-template-columns:1fr 1fr;">
              <div class="form-field">
                <label for="etiquetaLandingStation">NOMBRE *</label>
                <input type="text" id="etiquetaLandingStation" name="etiquetaLandingStation" placeholder="LS-06" required>
              </div>
              <div class="form-field">
                <label for="ubicacion">UBICACIÓN *</label>
                <input type="text" id="ubicacion" name="ubicacion" placeholder="Ciudad / región" required>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">REGISTRAR</button>
            <a href="<%=ctx%>/EstacionesServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
