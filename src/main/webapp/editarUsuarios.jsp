<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Roles" %>
<%@ page import="com.oceanlink.beans.Usuarios" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda UsuariosServlet (action=editar)
    Usuarios usr = (Usuarios) request.getAttribute("usuarios");
    ArrayList<Roles> listaRoles = (ArrayList<Roles>) request.getAttribute("listaRoles");
    String ctx = request.getContextPath();
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Editar usuario"/>
    <jsp:param name="encabezado" value="EDITAR USUARIO"/>
    <jsp:param name="subtitulo" value="Modifica los datos de la cuenta"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="usuarios"/>
</jsp:include>

        <form class="form-panel-dark" method="post" action="<%=ctx%>/UsuariosServlet?action=crear">
          <input type="hidden" name="id" value="<%=usr.getIdUsuario()%>">

          <div class="form-panel-header">
            <span style="color:#fff;font-size:16px;font-weight:700;"><%=Html.esc(usr.getEtiquetaUsuario())%></span>
          </div>
          <p class="form-section-label">INFORMACIÓN GENERAL</p>

          <div class="form-body">
            <div class="form-grid-2">
              <div class="form-field">
                <label for="etiqueta">USUARIO *</label>
                <input type="text" id="etiqueta" name="etiqueta" value="<%=Html.esc(usr.getEtiquetaUsuario())%>" required>
              </div>
              <div class="form-field">
                <label for="correo">CORREO *</label>
                <input type="email" id="correo" name="correo" value="<%=Html.esc(usr.getCorreo())%>" required>
              </div>
              <div class="form-field">
                <label for="nombre">NOMBRES</label>
                <input type="text" id="nombre" name="nombre" value="<%=Html.esc(usr.getNombre())%>">
              </div>
              <div class="form-field">
                <label for="apellido">APELLIDO</label>
                <input type="text" id="apellido" name="apellido" value="<%=Html.esc(usr.getApellido())%>">
              </div>
              <div class="form-field">
                <label for="rol">ROL *</label>
                <select id="rol" name="idRol" required>
                  <% if (listaRoles != null) {
                      for (Roles r : listaRoles) {
                          boolean seleccionado = usr.getIdroles() != null && r.getIdRoles() == usr.getIdroles().getIdRoles(); %>
                  <option value="<%=r.getIdRoles()%>" <%= seleccionado ? "selected" : "" %>><%=Html.esc(r.getNombreRoles())%></option>
                  <%   }
                     } %>
                </select>
              </div>
              <div class="form-field">
                <label for="telefono">TELÉFONO</label>
                <input type="tel" id="telefono" name="telefono" value="<%=Html.esc(usr.getTelefono())%>">
              </div>
              <div class="form-field">
                <label for="passwd">NUEVA CONTRASEÑA</label>
                <%-- vacía = se conserva la contraseña actual (el DAO solo la cambia si escriben una) --%>
                <input type="password" id="passwd" name="passwd" placeholder="Dejar vacío para no cambiarla" autocomplete="new-password">
              </div>
              <div class="form-field">
                <label for="estado">ESTADO DEL USUARIO</label>
                <select id="estado" name="estado">
                  <option value="Activo" <%= "Activo".equals(usr.getEstado()) ? "selected" : "" %>>Activo</option>
                  <option value="Inactivo" <%= "Inactivo".equals(usr.getEstado()) ? "selected" : "" %>>Inactivo</option>
                </select>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-form-save">GUARDAR CAMBIOS</button>
            <a href="<%=ctx%>/UsuariosServlet" class="btn-form-cancel" style="text-decoration:none;display:inline-flex;align-items:center;">CANCELAR</a>
          </div>
        </form>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
