<%--
  Panel "ACCIONES RÁPIDAS" que la maqueta muestra a la derecha de las pantallas del rol Admin.
  Se incluye dentro del .middle-row, después del panel principal:
  <jsp:include page="/WEB-INF/includes/acciones-admin.jsp"/>
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<% String ctxAcciones = request.getContextPath(); %>
        <div class="quick-actions">
          <h2>ACCIONES RÁPIDAS</h2>
          <a class="qa-btn primary" href="<%=ctxAcciones%>/UsuariosServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR USUARIO</a>
          <a class="qa-btn primary" href="<%=ctxAcciones%>/EstacionesServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR ESTACIONES</a>
          <a class="qa-btn primary" href="<%=ctxAcciones%>/SegmentosServlet?action=formCrear" style="text-decoration:none;display:block;">REGISTRAR SEGMENTOS</a>
        </div>
