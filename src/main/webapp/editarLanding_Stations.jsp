<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="java.util.ArrayList" %>

<%
    Landing_Stations ls = (Landing_Stations) request.getAttribute("landingStations");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
          crossorigin="anonymous">
    <title>Editar un Landing Station</title>
</head>
<body>
<div class='container mt-4'>
    <h1 class='mb-3'>Editar Landing Station</h1>

    <form method="post" action="<%=request.getContextPath()%>/EstacionesServlet?action=crear">

        <input type="hidden" name="id" value="<%=ls.getId_landing_stations()%>">

        <div class="form-group">
            <label for="etiquetaLandingStation">Landing Station</label>
            <input type="text" class="form-control" id="etiquetaLandingStation" name="etiquetaLandingStation" value="<%=ls.getEtiqueta_station()%>">
        </div>

        <div class="form-group">
            <label for="ubicacion">Ubicación</label>
            <input type="text" class="form-control" id="ubicacion" name="ubicacion" value="<%=ls.getUbicacion()%>">
        </div>

        <div class="form-group">
            <label for="estado">Estado</label>
            <select class="form-control" id="estado" name="estado">
                <option value="Operativo" <%= "Operativo".equals(ls.getEstado()) ? "selected" : "" %>>
                    Operativo
                </option>

                <option value="Fuera de servicio" <%= "Fuera de servicio".equals(ls.getEstado()) ? "selected" : "" %>>
                    Fuera de servicio
                </option>

                <option value="En mantenimiento" <%="En mantenimiento".equals(ls.getEstado()) ? "selected" : "" %>>
                    En mantenimiento
                </option>
            </select>
        </div>

        <a href="<%=request.getContextPath()%>/EstacionesServlet" class="btn btn-danger">Regresar</a>
        <button type="submit" class="btn btn-primary">Guardar Cambios</button>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
        crossorigin="anonymous"></script>
</body>
</html>