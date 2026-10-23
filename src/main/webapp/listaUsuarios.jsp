<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="com.oceanlink.beans.Usuarios" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda UsuariosServlet (action=lista) con request.setAttribute("lista", ...)
    ArrayList<Usuarios> listaUsuarios = (ArrayList<Usuarios>) request.getAttribute("lista");
    if (listaUsuarios == null) {
        listaUsuarios = new ArrayList<>();
    }
    String ctx = request.getContextPath();

    // Roles que existen en la lista, para armar el filtro
    LinkedHashSet<String> roles = new LinkedHashSet<>();
    for (Usuarios u : listaUsuarios) {
        if (u.getIdroles() != null && u.getIdroles().getNombreRoles() != null) {
            roles.add(u.getIdroles().getNombreRoles());
        }
    }
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Usuarios"/>
    <jsp:param name="encabezado" value="GESTIÓN DE USUARIOS"/>
    <jsp:param name="subtitulo" value="Lista de usuarios del sistema"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="usuarios"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar usuario, nombre o correo..." style="width:233px;">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por rol...</option>
              <% for (String rol : roles) { %>
              <option value="rol:<%=Html.esc(rol)%>"><%=Html.esc(rol)%></option>
              <% } %>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/UsuariosServlet?action=formCrear" style="text-decoration:none;">REGISTRAR USUARIO</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead>
                <tr><th>USUARIO</th><th>NOMBRE</th><th>CORREO</th><th>ROL</th><th>ESTADO</th><th>ACCIONES</th></tr>
              </thead>
              <tbody id="tabla-datos">
                <% if (listaUsuarios.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay usuarios registrados.</td></tr>
                <% }
                   for (Usuarios usr : listaUsuarios) {
                       String nombreRol = usr.getIdroles() == null ? "" : usr.getIdroles().getNombreRoles();
                       String claseEstado = "Activo".equals(usr.getEstado()) ? "badge-success" : "badge-neutral";
                %>
                <tr data-fila data-rol="<%=Html.esc(nombreRol)%>">
                  <td><%=Html.esc(usr.getEtiquetaUsuario())%></td>
                  <td><%=Html.esc(usr.getNombreCompleto())%></td>
                  <td><%=Html.esc(usr.getCorreo())%></td>
                  <td><%=Html.esc(nombreRol)%></td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(usr.getEstado())%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/UsuariosServlet?action=editar&id=<%=usr.getIdUsuario()%>">Editar</a>
                      <a class="btn-delete-red" href="<%=ctx%>/UsuariosServlet?action=borrar&id=<%=usr.getIdUsuario()%>"
                         onclick="return confirm('¿Seguro que quieres eliminar este usuario?');">Borrar</a>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="usuarios">Mostrando <%=listaUsuarios.size()%> de <%=listaUsuarios.size()%> usuarios</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
