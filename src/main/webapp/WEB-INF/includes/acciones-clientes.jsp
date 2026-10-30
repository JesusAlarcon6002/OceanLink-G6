<%--
  Panel "ACCIONES RÁPIDAS" del rol Capacity Planner (pantallas de Clientes y Solicitudes).
  Los otros botones de la maqueta (reservar capacidad, registrar servicio) se agregan cuando existan sus módulos.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<% String ctxCli = request.getContextPath(); %>
        <div class="quick-actions">
          <h2>ACCIONES RÁPIDAS</h2>
          <a class="qa-btn primary" href="<%=ctxCli%>/SolicitudesServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR SOLICITUD DE CAP.</a>
          <a class="qa-btn primary" href="<%=ctxCli%>/ClientesServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR CLIENTE</a>
          <a class="qa-btn primary" href="<%=ctxCli%>/RutasServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR RUTA</a>
          <a class="qa-btn primary" href="<%=ctxCli%>/ServiciosServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR SERVICIO</a>
        </div>
