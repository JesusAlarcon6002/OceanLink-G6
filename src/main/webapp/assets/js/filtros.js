/* ============================================
   OceanLink - Búsqueda y filtro de las tablas (en el navegador, sin recargar)

   Cómo se usa en un JSP de lista:
     <input id="buscar" ...>                          caja de búsqueda
     <select id="filtro">                             (opcional) filtro
        <option value="">Filtrar...</option>
        <option value="estado:Activo">Activo</option> "campo:valor"
     </select>
     <tbody id="tabla-datos">
        <tr data-fila data-estado="Activo"> ... </tr>   cada fila lleva data-fila y sus data-campo
     </tbody>
     <p id="nota-pie" data-unidad="usuarios"></p>     texto "Mostrando X de Y usuarios"
   ============================================ */
(function () {
  var buscar = document.getElementById('buscar');
  var filtro = document.getElementById('filtro');
  var filas = document.querySelectorAll('#tabla-datos tr[data-fila]');
  var nota = document.getElementById('nota-pie');
  if (!buscar || !filas.length) {
    return;
  }

  function aplicar() {
    var texto = buscar.value.toLowerCase();
    // el filtro viene como "campo:valor", ej: ["estado", "En curso"]
    var partes = filtro && filtro.value ? filtro.value.split(':') : null;
    var visibles = 0;
    filas.forEach(function (fila) {
      var coincideTexto = fila.textContent.toLowerCase().indexOf(texto) !== -1;
      var coincideFiltro = !partes || fila.dataset[partes[0]] === partes[1];
      var mostrar = coincideTexto && coincideFiltro;
      fila.style.display = mostrar ? '' : 'none';
      if (mostrar) {
        visibles++;
      }
    });
    if (nota) {
      nota.textContent = 'Mostrando ' + visibles + ' de ' + filas.length + ' ' + (nota.dataset.unidad || 'registros');
    }
  }

  buscar.addEventListener('input', aplicar);
  if (filtro) {
    filtro.addEventListener('change', aplicar);
  }
})();
