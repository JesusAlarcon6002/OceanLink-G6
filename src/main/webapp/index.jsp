<%-- Ya no hay contenido aquí: el filtro redirige "/" e index.jsp al login o al inicio del rol. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<% response.sendRedirect(request.getContextPath() + "/InicioServlet"); %>
