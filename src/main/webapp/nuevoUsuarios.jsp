<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Roles" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda UsuariosServlet (action=formCrear)
    ArrayList<Roles> listaRoles = (ArrayList<Roles>) request.getAttribute("listaRoles");
    String ctx = request.getContextPath();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Registrar usuario"/>
    <jsp:param name="encabezado" value="REGISTRAR USUARIO"/>
    <jsp:param name="subtitulo" value="Crea una nueva cuenta de usuario"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="usuarios"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/UsuariosServlet?action=crear" id="form-usuario">
          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;">NUEVO USUARIO</span>
          </div>
          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2">
              <div class="form-field">
                <label for="etiqueta">USUARIO *</label>
                <input type="text" id="etiqueta" name="etiqueta" placeholder="nombre.apellido" required>
              </div>
              <div class="form-field">
                <label for="correo">CORREO *</label>
                <input type="email" id="correo" name="correo" placeholder="correo@oceanlink.net" required>
              </div>
              <div class="form-field">
                <label for="nombre">NOMBRES</label>
                <input type="text" id="nombre" name="nombre">
              </div>
              <div class="form-field">
                <label for="apellido">APELLIDO</label>
                <input type="text" id="apellido" name="apellido">
              </div>
              <div class="form-field">
                <label for="rol">ROL *</label>
                <select id="rol" name="idRol" required>
                  <option value="">Seleccionar rol...</option>
                  <% if (listaRoles != null) {
                      for (Roles r : listaRoles) { %>
                  <option value="<%=r.getIdRoles()%>"><%=Html.esc(r.getNombreRoles())%></option>
                  <%   }
                     } %>
                </select>
              </div>
              <div class="form-field">
                <label for="telefono">TELÉFONO</label>
                <input type="tel" id="telefono" name="telefono">
              </div>
              <div class="form-field">
                <label for="passwd">CONTRASEÑA *</label>
                <input type="password" id="passwd" name="passwd" required>
              </div>
              <div class="form-field">
                <label for="passwd2">CONFIRMAR CONTRASEÑA *</label>
                <input type="password" id="passwd2" name="passwd2" required>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR</button>
            <a href="<%=ctx%>/UsuariosServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<script>
  // Las dos contraseñas deben coincidir antes de enviar el formulario
  document.getElementById('form-usuario').addEventListener('submit', function (e) {
    if (document.getElementById('passwd').value !== document.getElementById('passwd2').value) {
      e.preventDefault();
      alert('Las contraseñas no coinciden.');
    }
  });
</script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
