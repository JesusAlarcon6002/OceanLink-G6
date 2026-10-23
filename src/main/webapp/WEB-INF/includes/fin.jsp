<%--
  Parte de ABAJO del diseño de OceanLink: cierra el contenido y pinta el resumen
  fijo (stations / segmentos / capacidad) con datos reales de la BD.
  Va siempre después de inicio.jsp.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.oceanlink.beans.Resumen" %>
<%@ page import="com.oceanlink.daos.ResumenDao" %>
<%@ page import="java.text.DecimalFormat" %>
<%
    Resumen resumen = new ResumenDao().obtenerResumen();
    String capacidad = new DecimalFormat("#,##0.##").format(resumen.getCapacidadTotalGbps());
%>
      </div><%-- cierra .middle-row --%>

    </div><%-- cierra .dark-scroll-area --%>

      <div class="summary-row">
        <div class="summary-item">
          <div class="summary-icon bg-blue"><span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 13a10 10 0 0 1 14 0"/><path d="M8.5 16.5a5 5 0 0 1 7 0"/><path d="M2 8.82a15 15 0 0 1 20 0"/><line x1="12" x2="12.01" y1="20" y2="20"/></svg></span></div>
          <div class="summary-text"><p class="num"><%=resumen.getLandingStations()%></p><p class="lbl">Landing Stations</p></div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-icon bg-green"><span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="6" x2="6" y1="3" y2="15"/><circle cx="18" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path d="M18 9a9 9 0 0 1-9 9"/></svg></span></div>
          <div class="summary-text"><p class="num"><%=resumen.getSegmentos()%></p><p class="lbl">Segmentos</p></div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-icon bg-orange"><span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg></span></div>
          <div class="summary-text"><p class="num"><%=capacidad%> Gbps</p><p class="lbl">Capacidad total</p></div>
        </div>
      </div>

    </div><%-- cierra .dark-content --%>
  </div><%-- cierra .dark-dashboard --%>
</div><%-- cierra .app-shell --%>

<script src="<%=request.getContextPath()%>/assets/js/sidebar.js"></script>
</body>
</html>
