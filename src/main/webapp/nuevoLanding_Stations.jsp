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
    <title>Crear un nuevo Landing Station</title>
</head>
<body>
<div class='container'>
    <h1 class='mb-3'>Crear un nuevo Landing Station</h1>
    <form method="POST" action="<%=request.getContextPath()%>/EstacionesServlet?action=crear">
        <div class="form-group">
            <label for="etiquetaLandingStation">Landing Stations</label>
            <input type="text" class="form-control" id="etiquetaLandingStation" name="etiquetaLandingStation">
        </div>

        <div class="form-group">
            <label for="ubicacion">Ubicación</label>
            <input type="text" class="form-control" id="ubicacion" name="ubicacion">
        </div>

        <a href="<%=request.getContextPath()%>/EstacionesServlet" class="btn btn-danger">Regresar</a>
        <button type="submit" class="btn btn-primary">Submit</button>
    </form>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
        crossorigin="anonymous"></script>
</body>
</html>