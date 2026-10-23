<%--
  Panel "ACCIONES RÁPIDAS" del rol Capacity Planner (pantallas de Clientes).
  Por ahora solo está Clientes; los otros botones de la maqueta se agregan cuando existan sus módulos.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<% String ctxCli = request.getContextPath(); %>
        <div class="quick-actions">
          <h2>ACCIONES RÁPIDAS</h2>
          <a class="qa-btn primary" href="<%=ctxCli%>/ClientesServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR CLIENTE</a>
        </div>
