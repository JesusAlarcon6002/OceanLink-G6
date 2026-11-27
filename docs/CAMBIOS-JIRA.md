# OceanLink · Cambios para cerrar el Jira

## IW6-81 · Histórico de utilización de los segmentos
- La pantalla **Historial de utilización** (Capacity Planner y Supervisor) ya existía. Ahora tiene filtros: **segmento**, **desde** y **hasta** (botones *Consultar* / *Limpiar*). Siguen la búsqueda y el filtro por movimiento.
- Archivos: `HistorialDao`, `HistorialServlet`, `listaHistorial.jsp`.

## IW6-13 · Gestionar roles y permisos
- Nueva pantalla **Roles y permisos** (solo Network Admin, solo consulta): por cada rol, usuarios activos, módulos con acceso completo y módulos de solo lectura (sale de la matriz `Permisos`).
- El rol de una persona se cambia en **Usuarios → Editar**; las cuentas nuevas entran por **Solicitudes de registro**.
- Archivos: `RolesServlet`, `RolesDao`, `listaRoles.jsp`, `Permisos`, `sidebar.js` (el ítem "Roles" ya no es un `#`).

## Menú del Capacity Planner
- Se quitó la opción **Capacidad**, que era un enlace vacío. Las reservas de capacidad se hacen desde **Solicitudes**.

## Cómo aplicar
1. Descomprime el zip encima del repo.
2. No hay cambios en la base de datos.
3. `mvn clean package`, sube el war y redespliega (mismos comandos de siempre: scp a `/tmp`, stop tomcat, borrar `OceanLink` y `OceanLink.war`, copiar, chown, start).

## Probar
- Capacity Planner → Historial de utilización: elegir un segmento y un rango de fechas y *Consultar*.
- Ana → Roles y permisos: salen los 5 roles con sus módulos.
- Capacity Planner: el menú ya no tiene "Capacidad".

## Sin probar (no hay BD ni Tomcat en mi entorno)
Compila y pasan las pruebas automáticas, pero las consultas SQL nuevas y los JSP no se corrieron contra MySQL/Tomcat real.
