<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" isErrorPage="true" %>
<%
    // Página de error genérica: solo muestra el código y un texto amable. El detalle técnico se queda en el log del servidor.
    String ctx = request.getContextPath();
    Integer codigo = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    int c = codigo == null ? 500 : codigo;
    String titulo;
    String texto;
    if (c == 404) {
        titulo = "404 · PÁGINA NO ENCONTRADA";
        texto = "La página que buscas no existe o fue movida.";
    } else if (c == 405) {
        titulo = "405 · ACCIÓN NO PERMITIDA";
        texto = "Esa acción no se puede hacer de esta forma.";
    } else if (c == 429) {
        titulo = "429 · DEMASIADAS SOLICITUDES";
        texto = "Espera un momento e inténtalo de nuevo.";
    } else {
        titulo = "ERROR DEL SISTEMA";
        texto = "Ocurrió un problema inesperado. Inténtalo de nuevo; si continúa, avisa al administrador.";
    }
    response.setStatus(c);
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · Error</title>
<link rel="icon" type="image/png" href="<%=ctx%>/assets/img/favicon.png">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
</head>
<body class="pagina-login">
<main class="login-wrap">
  <div class="login-card">
    <div class="login-brand">
      <img src="<%=ctx%>/assets/img/logo-oceanlink.png" alt="Ocean Link">
      <h1><%=titulo%></h1>
      <p><%=texto%></p>
    </div>
    <a class="login-btn" style="display:block;text-align:center;text-decoration:none;" href="<%=ctx%>/InicioServlet">VOLVER AL INICIO</a>
  </div>
</main>
</body>
</html>
