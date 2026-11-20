<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.oceanlink.util.Html" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String aviso = (String) request.getAttribute("aviso");
    boolean paso2 = Boolean.TRUE.equals(request.getAttribute("paso2"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OceanLink · Solicitar cuenta</title>
<link rel="icon" type="image/png" href="<%=ctx%>/assets/img/favicon.png">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="<%=ctx%>/assets/css/base.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/dashboard-admin.css" rel="stylesheet">
<link href="<%=ctx%>/assets/css/oceanlink-jsp.css" rel="stylesheet">
</head>
<body class="pagina-login">
<main class="login-wrap">
<%
    String[] campos = com.oceanlink.servlets.RegistrarseServlet.campos(request.getAttribute("datos"));
    String dominios = (String) request.getAttribute("dominios");
%>
  <div class="login-card">
    <div class="login-brand">
      <img src="<%=ctx%>/assets/img/logo-oceanlink.png" alt="Ocean Link">
      <h1>OCEAN LINK</h1>
      <p>Solicitar una cuenta</p>
    </div>
    <% if (error != null) { %><div class="login-aviso login-error" role="alert"><%=Html.esc(error)%></div><% } %>
    <% if (aviso != null) { %><div class="login-aviso login-ok"><%=Html.esc(aviso)%></div><% } %>

    <% if (!paso2) { %>
    <form method="post" action="<%=ctx%>/RegistrarseServlet" style="display:contents;" autocomplete="off">
      <input type="hidden" name="accion" value="enviar">
      <label for="nombre">Nombre</label>
      <input type="text" id="nombre" name="nombre" maxlength="60" required autofocus value="<%=Html.esc(campos[0])%>">
      <label for="apellido">Apellido</label>
      <input type="text" id="apellido" name="apellido" maxlength="60" required value="<%=Html.esc(campos[1])%>">
      <label for="correo">Correo</label>
      <input type="email" id="correo" name="correo" maxlength="150" required value="<%=Html.esc(campos[2])%>">
      <small>Dominios admitidos: <%=Html.esc(dominios == null || dominios.isEmpty() ? "cualquiera" : dominios)%>. Te enviaremos un código para confirmarlo.</small>
      <label for="telefono">Teléfono (opcional)</label>
      <input type="text" id="telefono" name="telefono" maxlength="20" value="<%=Html.esc(campos[3])%>">
      <label for="clave">Contraseña</label>
      <input type="password" id="clave" name="clave" required minlength="8" maxlength="64" autocomplete="new-password">
      <label for="clave2">Repite la contraseña</label>
      <input type="password" id="clave2" name="clave2" required minlength="8" maxlength="64" autocomplete="new-password">
      <small>8 a 64 caracteres, con al menos una letra y un número.</small>
      <button type="submit" class="login-btn">ENVIAR CÓDIGO</button>
    </form>
    <% } else { %>
    <form method="post" action="<%=ctx%>/RegistrarseServlet" style="display:contents;" autocomplete="off">
      <input type="hidden" name="accion" value="confirmar">
      <label>Correo</label>
      <input type="email" value="<%=Html.esc(campos[2])%>" disabled>
      <label for="codigo">Código de 6 dígitos</label>
      <input type="text" id="codigo" name="codigo" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" required autofocus placeholder="000000">
      <small>Al confirmar, tu solicitud queda pendiente hasta que un administrador la revise.</small>
      <button type="submit" class="login-btn">CONFIRMAR Y ENVIAR SOLICITUD</button>
    </form>
    <% } %>
    <div class="login-links"><a href="<%=ctx%>/LoginServlet">Volver a iniciar sesión</a></div>
  </div>
</main>
</body>
</html>
