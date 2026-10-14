<%@ page import="com.oceanlink.beans.Landing_Stations" %>
<%@ page import="java.util.ArrayList" %>
<%
    ArrayList<Landing_Stations> listaLanding_Stations = (ArrayList<Landing_Stations>) request.getAttribute("listaLanding_Stations");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
          integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
          rel="stylesheet" crossorigin="anonymous">
    <title>Crear un nuevo Segmento</title>
</head>
<body>
<div class='container'>
    <h1 class='mb-3'>Crear un nuevo Segmento</h1>
    <form method="POST" action="<%=request.getContextPath()%>/SegmentosServlet?action=formCrear">
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

        <a href="<%=request.getContextPath()%>/SegmentosServlet" class="btn btn-danger">Regresar</a>
        <button type="submit" class="btn btn-primary">Submit</button>
    </form>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
        crossorigin="anonymous"></script>
</body>
</html>