<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.beans.UsuarioSesion" %>
<%@ page import="com.oceanlink.util.Permisos" %>
<%@ page import="com.oceanlink.util.Rol" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    String ctx = request.getContextPath();
    UsuarioSesion u = (UsuarioSesion) session.getAttribute("usuario");
    boolean lectura = u.getRol() == Rol.SUPERVISOR;
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Inicio"/>
    <jsp:param name="encabezado" value="BIENVENIDO A OCEANLINK"/>
    <jsp:param name="subtitulo" value="Sistema de gestión de infraestructura submarina"/>
    <jsp:param name="activo" value="inicio"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="infra-header">
            <h2>MÓDULOS DISPONIBLES · <%=Html.esc(u.getRol().getNombreBd().toUpperCase())%></h2>
          </div>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>MÓDULO</th><th>ACCESO</th><th>ACCIÓN</th></tr></thead>
              <tbody>
              <% for (Permisos.Modulo m : Permisos.todos()) {
                   if (!Permisos.veModulo(u.getRol(), m)) continue; %>
                <tr>
                  <td><%=Html.esc(m.nombre)%></td>
                  <td><span class="badge <%= lectura ? "badge-warning" : "badge-success" %>"><%= lectura ? "Solo lectura" : "Completo" %></span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%><%=m.ruta%>">Abrir</a></td>
                </tr>
              <% } %>
              </tbody>
            </table>
          </div>
        </div>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
