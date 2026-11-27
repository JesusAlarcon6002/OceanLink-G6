# Cambios: Perfil, modo claro, roles, móvil, Mantenimientos y Network Operator

## Perfil (`/PerfilServlet`, todos los roles) — Configuración se unió aquí
- Datos personales (nombre, apellido, teléfono editables; correo, usuario y rol de solo lectura).
- Cambiar contraseña: misma lógica del login y la recuperación (política 8–64 con letras y números, pide la actual, máx. 5 intentos cada 15 min, aviso por correo).
- Apariencia: **modo oscuro / modo claro** (también con el botón sol/luna del encabezado) y "ocultar el menú lateral al entrar". Se guarda en el navegador.
- `/ConfiguracionServlet` ya solo redirige al Perfil; `configuracion.jsp` quedó sin uso (se puede borrar).
- Roles y permisos: cada rol con sus módulos como etiquetas (completo / solo lectura).

## Modo claro
- `assets/css/tema-claro.css` (solo se aplica con `<html data-tema="claro">`). El menú lateral queda oscuro en ambos modos. El dashboard ajusta los colores de los gráficos.

## Maintenance Coordinator
- Menú: Mantenimientos (completo), **Historial de mantenimientos** (`?action=historial`, solo finalizados, solo lectura) e **Incidencias (consulta)** (solo lectura).
- Permisos: nuevo concepto de "lectores" en `Permisos` (solo GET de lista / ver / historial).

## Network Operator
- Ahora tiene **Dashboard de la red** y **Reportes de incidencias** (CSV / Excel / PDF). Solo puede ver el reporte de incidencias.
- Su pantalla de inicio ya lista esos módulos.

## Sincronía mantenimientos ↔ infraestructura
- Cada minuto (`MantenimientosListener`): un mantenimiento Programado cuya fecha y hora ya llegaron pasa solo a **En curso**, y su segmento o estación queda **En mantenimiento**.
- Al guardar un mantenimiento como En curso, la infraestructura se marca al instante; al finalizarlo (o desactivarlo) vuelve a **Operativo** (salvo que tenga otro mantenimiento en curso).
- La hora de "ahora" se mide en hora de Lima.

## Otros
- Select y cajas de texto de Solicitudes de registro con la paleta del proyecto.
- La conexión JDBC fuerza `characterEncoding=UTF-8` (tildes y ñ).
- Móvil/tablet (hasta 900 px): menú lateral desplegable, formularios y tablas adaptados, menú compacto en pantallas bajas.

No requiere cambios en la base de datos.
