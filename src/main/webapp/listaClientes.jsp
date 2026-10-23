<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.oceanlink.beans.Clientes" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    // Lo manda ClientesServlet (action=lista)
    ArrayList<Clientes> listaClientes = (ArrayList<Clientes>) request.getAttribute("listaClientes");
    if (listaClientes == null) {
        listaClientes = new ArrayList<>();
    }
    String ctx = request.getContextPath();

    String ok = request.getParameter("ok");
    String err = request.getParameter("err");
    String mensajeOk = null;
    if ("creado".equals(ok)) mensajeOk = "Cliente registrado correctamente.";
    else if ("actualizado".equals(ok)) mensajeOk = "Cliente actualizado correctamente.";
    else if ("borrado".equals(ok)) mensajeOk = "Cliente eliminado.";

    int activos = 0;
    for (Clientes c : listaClientes) {
        if ("Activo".equals(c.getEstado())) activos++;
    }
%>
<jsp:include page="/WEB-INF/includes/inicio.jsp">
    <jsp:param name="titulo" value="Clientes"/>
    <jsp:param name="encabezado" value="CLIENTES"/>
    <jsp:param name="subtitulo" value="Lista de clientes"/>
    <jsp:param name="rol" value="capacity-planner"/>
    <jsp:param name="activo" value="clientes"/>
</jsp:include>

        <div class="form-panel-dark" style="min-height:auto;">
          <div class="infra-header">
            <h2>CLIENTES</h2>
            <span class="ok-count"><%=activos%>/<%=listaClientes.size()%> activos</span>
          </div>

          <% if (mensajeOk != null) { %>
          <p class="msg msg-ok"><%=mensajeOk%></p>
          <% } %>
          <% if ("borrar".equals(err)) { %>
          <p class="msg msg-error">No se pudo eliminar el cliente. Si tiene servicios o solicitudes de capacidad asociados, no se puede borrar: márcalo como Inactivo.</p>
          <% } %>

          <div class="panel-toolbar">
            <input class="search-input" type="text" id="buscar" placeholder="Buscar cliente, correo o teléfono...">
            <select class="filter-select" id="filtro">
              <option value="">Filtrar por estado...</option>
              <option value="estado:Activo">Activo</option>
              <option value="estado:Inactivo">Inactivo</option>
            </select>
            <a class="btn-register-toolbar" href="<%=ctx%>/ClientesServlet?action=formCrear" style="text-decoration:none;">REGISTRAR CLIENTE</a>
          </div>

          <div class="dark-table-wrap">
            <table>
              <thead><tr><th>ID</th><th>CLIENTE</th><th>CORREO</th><th>TELÉFONO</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody id="tabla-datos">
                <% if (listaClientes.isEmpty()) { %>
                <tr class="fila-vacia"><td colspan="6">Aún no hay clientes registrados.</td></tr>
                <% }
                   for (Clientes c : listaClientes) {
                       String claseEstado = "Activo".equals(c.getEstado()) ? "badge-success" : "badge-neutral";
                %>
                <tr data-fila data-estado="<%=Html.esc(c.getEstado())%>">
                  <td><%= c.getEtiqueta() == null ? "CLI-" + c.getIdCliente() : Html.esc(c.getEtiqueta()) %></td>
                  <td><%=Html.esc(c.getNombre())%></td>
                  <td><%= c.getCorreo() == null ? "—" : Html.esc(c.getCorreo()) %></td>
                  <td><%= c.getTelefono() == null ? "—" : Html.esc(c.getTelefono()) %></td>
                  <td><span class="badge <%=claseEstado%>"><%=Html.esc(c.getEstado())%></span></td>
                  <td>
                    <div class="acciones-fila">
                      <a class="btn-edit-white" href="<%=ctx%>/ClientesServlet?action=editar&id=<%=c.getIdCliente()%>">Editar</a>
                      <form method="post" action="<%=ctx%>/ClientesServlet?action=borrar"
                            onsubmit="return confirm('¿Seguro que quieres eliminar este cliente?');">
                        <input type="hidden" name="id" value="<%=c.getIdCliente()%>">
                        <button type="submit" class="btn-delete-red">Borrar</button>
                      </form>
                    </div>
                  </td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
          <p class="table-footer-note" id="nota-pie" data-unidad="clientes">Mostrando <%=listaClientes.size()%> de <%=listaClientes.size()%> clientes</p>
        </div>

<jsp:include page="/WEB-INF/includes/acciones-clientes.jsp"/>
<script src="<%=ctx%>/assets/js/filtros.js"></script>
<jsp:include page="/WEB-INF/includes/fin.jsp"/>
