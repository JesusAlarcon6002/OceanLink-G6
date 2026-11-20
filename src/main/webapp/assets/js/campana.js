/* Campana del encabezado: muestra cuántas notificaciones/mensajes sin leer tiene el usuario.
   Pregunta a BandejaServlet?action=resumen al cargar y cada 60 s (solo si la pestaña está visible). */
(function () {
  var ctx = (document.currentScript && document.currentScript.dataset.ctx) || "";
  function actualizar() {
    if (document.hidden) return;
    var num = document.getElementById("campana-num");
    if (!num) return;
    fetch(ctx + "/BandejaServlet?action=resumen", { credentials: "same-origin", cache: "no-store" })
      .then(function (r) { return r.ok ? r.json() : null; })
      .then(function (d) {
        if (!d) return;
        num.textContent = d.noLeidas > 99 ? "99+" : String(d.noLeidas);
        num.hidden = !(d.noLeidas > 0);
      })
      .catch(function () { /* sin conexión: se deja como estaba */ });
  }
  document.addEventListener("DOMContentLoaded", actualizar);
  setInterval(actualizar, 60000);
})();
