<%--
  Página de inicio TEMPORAL: mientras no exista el login, desde aquí se entra
  a cada módulo. Cuando se haga el login, cada rol verá solo los suyos.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<% String ctx = request.getContextPath(); %>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Inicio"/>
    <jsp:param name="encabezado" value="BIENVENIDO A OCEANLINK"/>
    <jsp:param name="subtitulo" value="Sistema de gestión de infraestructura submarina"/>
    <jsp:param name="rol" value="admin"/>
    <jsp:param name="activo" value="inicio"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="infra-header">
            <h2>MÓDULOS DISPONIBLES</h2>
          </div>
          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>MÓDULO</th><th>ESTADO</th><th>ACCIÓN</th></tr></thead>
              <tbody>
                <tr>
                  <td>Usuarios</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/UsuariosServlet">Abrir</a></td>
                </tr>
                <tr>
                  <td>Landing Stations</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/EstacionesServlet">Abrir</a></td>
                </tr>
                <tr>
                  <td>Segmentos</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/SegmentosServlet">Abrir</a></td>
                </tr>
                <tr>
                  <td>Mantenimientos</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/MantenimientosServlet">Abrir</a></td>
                </tr>
                <tr>
                  <td>Clientes</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/ClientesServlet">Abrir</a></td>
                </tr>
                <tr>
                  <td>Incidencias</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/IncidenciasServlet">Abrir</a></td>
                </tr>
                <tr>
                  <td>Solicitudes de capacidad</td>
                  <td><span class="badge badge-success">Disponible</span></td>
                  <td><a class="btn-edit-white" href="<%=ctx%>/SolicitudesServlet">Abrir</a></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

<jsp:include page="/WEB-INF/includes/fin.jsp"/>
