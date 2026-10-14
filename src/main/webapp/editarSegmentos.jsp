<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.oceanlink.beans.Segmentos" %>
<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="java.util.ArrayList" %>

<%
    Segmentos sg = (Segmentos) request.getAttribute("segmentos");
    ArrayList<Landing_Stations> listaLanding_Stations= (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"
          crossorigin="anonymous">
    <title>Editar un segmento</title>
</head>
<body>
<div class='container mt-4'>
    <h1 class='mb-3'>Editar segmento</h1>

    <form method="post" action="<%=request.getContextPath()%>/UsuariosServlet?action=crear">

        <input type="hidden" name="id" value="<%=sg.getIdSegmento()%>">

        <div class="form-group">
            <label for="etiquetaSegmento">Segmento</label>
            <input type="text" class="form-control" id="etiquetaSegmento" name="etiquetaSegmento">
        </div>

        <div class="form-group">
            <label for="estacionOrigen">Estación Origen</label>
            <select class="form-control" id="estacionOrigen" name="estacionOrigen">
                <% if (listaLanding_Stations != null) {
                    for (Landing_Stations ls : listaLanding_Stations) { %>
                <option value="<%= ls.getId_landing_stations() %>"><%= ls.getEtiqueta_station()%></option>
                <%     }
                } %>
            </select>
        </div>

        <div class="form-group">
            <label for="estacionDestino">Estación Destino</label>
            <select class="form-control" id="estacionDestino" name="estacionDestino">
                <% if (listaLanding_Stations != null) {
                    for (Landing_Stations ls : listaLanding_Stations) { %>
                <option value="<%= ls.getId_landing_stations() %>"><%= ls.getEtiqueta_station()%></option>
                <%     }
                } %>
            </select>
        </div>

        <div class="form-group">
            <label for="capacidadTotal">Capacidad Total (Gbps)</label>
            <input type="number" step="0.01" min="0" class="form-control" id="capacidadTotal" name="capacidadTotal">
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