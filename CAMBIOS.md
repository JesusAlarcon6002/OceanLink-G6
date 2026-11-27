# OceanLink – ajustes (borrado lógico, modal, fecha, select)

Descomprime encima del proyecto (rama feat/correo-reportes o la que uses), recarga/recompila y listo. No hay cambios en la BD.

## 1. Select de segmentos de la ruta
`.fila-seg select` en `oceanlink-jsp.css`: azul oscuro del proyecto, borde azul claro, flecha propia y opciones oscuras (antes salía el select blanco del navegador).

## 2. Fecha de solicitud
`formSolicitudes.jsp` solo la muestra (sin poder cambiarla). `SolicitudesServlet` ignora lo que mande el navegador: al registrar usa la fecha/hora actual y al editar conserva la que ya tenía.

## 3. Borrado lógico (el botón Borrar ahora hace UPDATE de estado)
| CRUD | Estado al "borrar" | Protección que se mantiene |
|---|---|---|
| Clientes | Inactivo | – |
| Usuarios | Inactivo (ya no puede iniciar sesión) | no puede desactivarse a sí mismo |
| Estaciones | Fuera de servicio | – |
| Segmentos | Fuera de servicio | – |
| Rutas | Inactiva (conserva sus segmentos) | no si tiene servicios |
| Solicitudes | Rechazada | no si tiene capacidad reservada |
| Servicios | Finalizado | solo si está Pendiente |
| Incidencias | Cerrada (+ fecha de restauración si no tenía) | – |
| Mantenimientos | Finalizado (+ fecha de finalización si no tenía) | – |
El botón Borrar ya no aparece en filas que ya están en ese estado. Los mensajes dicen "desactivado/a".

## 4. Ventana de confirmación propia
Nuevo `assets/js/confirmar.js` (cargado desde `fin.jsp`, o sea en todas las pantallas) + estilos `.ol-modal*` en `oceanlink-jsp.css`. Los formularios usan `data-confirmar="..."` en vez de `onsubmit="return confirm(...)"`. Esc o clic afuera cancelan. Reservar/Liberar capacidad usan la misma ventana en azul/verde.

## Pendientes a tener en cuenta
- Solicitudes no tiene un estado "inactivo" en la BD, por eso usa Rechazada. Si prefieren "Anulada" hay que agregarlo al ENUM (cambio de BD).
- Los desplegables de cliente/estación/ruta de otros formularios siguen listando los registros desactivados.
- Probado: compila, JSP renderizadas, modal probado en navegador (abrir/Esc/aceptar), valores de estado válidos contra el ENUM. No probado con MySQL/Tomcat reales.
