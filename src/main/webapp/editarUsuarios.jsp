<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.oceanlink.beans.Usuarios" %>
<%@ page import="com.oceanlink.beans.Roles" %>
<%@ page import="java.util.ArrayList" %>

<%
    Usuarios usr = (Usuarios) request.getAttribute("usuarios");
    ArrayList<Roles> listaRoles = (ArrayList<Roles>) request.getAttribute("listaRoles");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
          crossorigin="anonymous">
    <title>Editar un usuario</title>
</head>
<body>
<div class='container mt-4'>
    <h1 class='mb-3'>Editar usuario</h1>

    <form method="post" action="<%=request.getContextPath()%>/UsuariosServlet?action=crear">

        <input type="hidden" name="id" value="<%=usr.getIdUsuario()%>">

        <div class="mb-3">
            <label for="etiqueta" class="form-label">Etiqueta / Nombre de Usuario</label>
            <input type="text" class="form-control" id="etiqueta" name="etiqueta" value="<%=usr.getEtiquetaUsuario()%>">
        </div>

        <div class="mb-3">
            <label for="nombre" class="form-label">Nombre</label>
            <input type="text" class="form-control" id="nombre" name="nombre" value="<%=usr.getNombre()%>">
        </div>

        <div class="mb-3">
            <label for="apellido" class="form-label">Apellido</label>
            <input type="text" class="form-control" id="apellido" name="apellido" value="<%=usr.getApellido()%>">
        </div>

        <div class="mb-3">
            <label for="correo" class="form-label">Correo Electrónico</label>
            <input type="email" class="form-control" id="correo" name="correo" value="<%=usr.getCorreo()%>">
        </div>

        <div class="mb-3">
            <label for="telefono" class="form-label">Telefono</label>
            <input type="text" class="form-control" id="telefono" name="telefono" value="<%=usr.getTelefono()%>">
        </div>

        <div class="mb-3">
            <label for="passwd" class="form-label">Nueva Contraseña</label>
            <input type="password" class="form-control" id="passwd" name="passwd" value="<%=usr.getPasswordHash()%>">
        </div>

        <div class="mb-3">
            <label for="rol" class="form-label">Rol</label>
            <select name="idRol" id="rol" class="form-select">
                <% if (listaRoles != null) {
                    for (Roles r : listaRoles) {
                        boolean seleccionado = (r.getIdRoles() == usr.getIdroles().getIdRoles());
                %>
                <option value="<%= r.getIdRoles() %>" <%= seleccionado ? "selected" : "" %>>
                    <%= r.getNombreRoles() %>
                </option>
                <%     }
                } %>
            </select>
        </div>

        <a href="<%=request.getContextPath()%>/UsuariosServlet" class="btn btn-danger">Regresar</a>
        <button type="submit" class="btn btn-primary">Guardar Cambios</button>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
        crossorigin="anonymous"></script>
</body>
</html>