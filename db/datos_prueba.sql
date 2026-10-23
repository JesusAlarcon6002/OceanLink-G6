-- =====================================================================
-- OceanLink - datos de prueba (proyecto_siete)
-- Se puede ejecutar varias veces: no duplica lo que ya existe
-- (usa INSERT IGNORE por columnas UNIQUE y WHERE NOT EXISTS).
-- Ejecutar en MySQL Workbench con la base proyecto_siete.
-- Contraseña de TODOS los usuarios de prueba: Demo1234
-- (la app todavía guarda la contraseña en texto plano).
-- =====================================================================
USE proyecto_siete;

-- ---------- ROLES ----------
INSERT IGNORE INTO roles (nombres_rol) VALUES
 ('Network Admin'), ('Capacity Planner'), ('Supervisor'),
 ('Network Operator'), ('Maintenance Coordinator');

-- ---------- USUARIOS ----------
INSERT IGNORE INTO usuarios (nombre, apellido, correo, telefono, password_hash, estado, roles_idroles, etiqueta_usuario) VALUES
 ('Ana',    'Torres',   'ana.torres@oceanlink.net',    '987654321', 'Demo1234', 'Activo',   (SELECT id_roles FROM roles WHERE nombres_rol='Network Admin'),          'USR-ADMIN01'),
 ('Luis',   'Ramírez',  'luis.ramirez@oceanlink.net',  '987111222', 'Demo1234', 'Activo',   (SELECT id_roles FROM roles WHERE nombres_rol='Capacity Planner'),       'USR-CAP01'),
 ('Marta',  'Díaz',     'marta.diaz@oceanlink.net',    '987333444', 'Demo1234', 'Activo',   (SELECT id_roles FROM roles WHERE nombres_rol='Supervisor'),             'USR-SUP01'),
 ('Carlos', 'Quispe',   'carlos.quispe@oceanlink.net', '987555666', 'Demo1234', 'Activo',   (SELECT id_roles FROM roles WHERE nombres_rol='Network Operator'),       'USR-NET01'),
 ('Rosa',   'Mendoza',  'rosa.mendoza@oceanlink.net',  '987777888', 'Demo1234', 'Activo',   (SELECT id_roles FROM roles WHERE nombres_rol='Maintenance Coordinator'),'USR-MAN01'),
 ('Jorge',  'Salazar',  'jorge.salazar@oceanlink.net', NULL,        'Demo1234', 'Inactivo', (SELECT id_roles FROM roles WHERE nombres_rol='Network Operator'),       'USR-NET02');

-- ---------- LANDING STATIONS ----------
INSERT IGNORE INTO landing_stations (etiqueta_station, ubicacion, estado) VALUES
 ('LS-01 Lima',     'Lima, Perú',               'Operativo'),
 ('LS-02 Piura',    'Máncora, Piura, Perú',     'Operativo'),
 ('LS-03 Arequipa', 'Camaná, Arequipa, Perú',   'En mantenimiento'),
 ('LS-04 Tacna',    'Tacna, Perú',              'Operativo'),
 ('LS-05 Arica',    'Arica, Chile',             'Operativo'),
 ('LS-06 Guayaquil','Salinas, Ecuador',         'Fuera de servicio');

-- ---------- SEGMENTOS (usada + reservada <= total) ----------
INSERT IGNORE INTO segmentos (etiqueta_segmentos, id_estacion_origen, id_estacion_destino, capacidad_total_gbps, capacidad_utilizada_gbps, capacidad_reservada_gbps, estado) VALUES
 ('SEG-B1', (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-01 Lima'),     (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-02 Piura'),     400.00, 220.50,  80.00, 'Operativo'),
 ('SEG-B2', (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-01 Lima'),     (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-03 Arequipa'), 300.00, 150.00,  50.00, 'En mantenimiento'),
 ('SEG-B3', (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-03 Arequipa'), (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-04 Tacna'),    200.00,  90.00,  20.00, 'Degradado'),
 ('SEG-B4', (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-04 Tacna'),    (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-05 Arica'),    250.00, 120.00,  40.00, 'Operativo'),
 ('SEG-B5', (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-02 Piura'),    (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station='LS-06 Guayaquil'),350.00,   0.00,   0.00, 'Fuera de servicio');

-- ---------- CLIENTES ----------
INSERT INTO clientes (etiqueta_cliente, nombres, correos, telefonos, estado)
SELECT * FROM (
  SELECT 'CLI-01' AS e, 'Empresa Andina' AS n, 'contacto@empresaandina.com' AS c, '+51 999 111 222' AS t, 'Activo' AS s
  UNION ALL SELECT 'CLI-02', 'Pacífico Telecom',   'ventas@pacificotelecom.pe', '+51 999 333 444', 'Activo'
  UNION ALL SELECT 'CLI-03', 'Grupo Marítimo SA',  'info@grupomaritimo.com',    '014445555',       'Activo'
  UNION ALL SELECT 'CLI-04', 'Costa Digital',      'hola@costadigital.pe',      NULL,              'Inactivo'
  UNION ALL SELECT 'CLI-05', 'Andes Cloud',        'soporte@andescloud.com',    '+51 988 777 666', 'Activo'
) AS nuevos
WHERE NOT EXISTS (SELECT 1 FROM clientes c WHERE c.etiqueta_cliente = nuevos.e);

-- ---------- MANTENIMIENTOS ----------
INSERT IGNORE INTO mantenimientos (etiqueta_mantenimientos, tipo, descripcion, fecha_programada, duracion_estimada_horas, estado, fecha_finalizacion, resultado, actividades) VALUES
 ('MAN-01', 'Preventivo', 'Revisión de repetidores del segmento Lima - Piura',       '2026-10-05 08:00:00',  6.00, 'Finalizado', '2026-10-05 13:30:00', 'Sin novedades',                  'Inspección visual, pruebas de potencia óptica'),
 ('MAN-02', 'Correctivo', 'Reparación de degradación en Arequipa - Tacna',            '2026-10-20 22:00:00', 10.50, 'En curso',   NULL,                  NULL,                             'Medición con OTDR, empalme de fibra'),
 ('MAN-03', 'Preventivo', 'Mantenimiento de equipos de la estación de Arequipa',      '2026-11-02 09:00:00',  8.00, 'Programado', NULL,                  NULL,                             'Limpieza de conectores, revisión eléctrica'),
 ('MAN-04', 'Correctivo', 'Falla en estación de Guayaquil',                           '2026-11-10 07:30:00', 24.00, 'Programado', NULL,                  NULL,                             'Diagnóstico de energía y respaldo'),
 ('MAN-05', 'Preventivo', 'Calibración de amplificadores Tacna - Arica',              '2026-09-15 10:00:00',  5.25, 'Finalizado', '2026-09-15 15:10:00', 'Calibración dentro de rango',  'Ajuste de ganancia, registro de valores');

-- ---------- INFRAESTRUCTURA DE LOS MANTENIMIENTOS (segmento O estación) ----------
INSERT INTO mantenimiento_infraestructura (id_mantenimiento, id_segmento, id_estacion)
SELECT m.id_mantenimientos, s.id_segmentos, NULL
FROM mantenimientos m JOIN segmentos s
  ON (m.etiqueta_mantenimientos='MAN-01' AND s.etiqueta_segmentos='SEG-B1')
  OR (m.etiqueta_mantenimientos='MAN-02' AND s.etiqueta_segmentos='SEG-B3')
  OR (m.etiqueta_mantenimientos='MAN-05' AND s.etiqueta_segmentos='SEG-B4')
WHERE NOT EXISTS (SELECT 1 FROM mantenimiento_infraestructura mi WHERE mi.id_mantenimiento = m.id_mantenimientos);

INSERT INTO mantenimiento_infraestructura (id_mantenimiento, id_segmento, id_estacion)
SELECT m.id_mantenimientos, NULL, l.id_landing_stations
FROM mantenimientos m JOIN landing_stations l
  ON (m.etiqueta_mantenimientos='MAN-03' AND l.etiqueta_station='LS-03 Arequipa')
  OR (m.etiqueta_mantenimientos='MAN-04' AND l.etiqueta_station='LS-06 Guayaquil')
WHERE NOT EXISTS (SELECT 1 FROM mantenimiento_infraestructura mi WHERE mi.id_mantenimiento = m.id_mantenimientos);

-- ---------- INCIDENCIAS ----------
INSERT INTO incidencias (etiqueta_incidencias, titulo, descripcion, severidad, estado, fecha_deteccion, fecha_restauracion, solucion, observaciones)
SELECT * FROM (
  SELECT 'INC-01' AS e, 'Caída de señal en Arequipa - Tacna' AS t, 'Caída de señal parcial, posible daño físico en el cable submarino.' AS d, 'Alta' AS sv, 'En reparacion' AS es,
         '2026-10-18 06:40:00' AS fd, NULL AS fr, NULL AS so, 'Equipo técnico enviado a inspeccionar la zona.' AS ob
  UNION ALL SELECT 'INC-02', 'Degradación de capacidad Lima - Piura', 'Se detecta pérdida de potencia óptica en un repetidor.', 'Media', 'En analisis', '2026-10-21 14:15:00', NULL, NULL, NULL
  UNION ALL SELECT 'INC-03', 'Corte de energía en estación Guayaquil', 'Falla del suministro principal; la estación quedó sin respaldo.', 'Critica', 'Detectada', '2026-10-22 03:05:00', NULL, NULL, 'Pendiente confirmar con el proveedor local.'
  UNION ALL SELECT 'INC-04', 'Alarma de temperatura en Arica', 'Alarma por temperatura alta en el cuarto de equipos.', 'Baja', 'Servicio restaurado', '2026-10-10 09:00:00', '2026-10-10 11:30:00', 'Se reemplazó el ventilador del rack.', NULL
  UNION ALL SELECT 'INC-05', 'Intermitencia en Tacna - Arica', 'Pérdida intermitente de paquetes reportada por clientes.', 'Media', 'Cerrada', '2026-09-28 16:20:00', '2026-09-29 08:00:00', 'Ajuste de ganancia en amplificadores.', 'Caso cerrado tras 24 h de monitoreo.'
) AS nuevas
WHERE NOT EXISTS (SELECT 1 FROM incidencias i WHERE i.etiqueta_incidencias = nuevas.e);

INSERT INTO incidencia_segmento (id_incidencia, id_segmento)
SELECT i.id_incidencias, s.id_segmentos
FROM incidencias i JOIN segmentos s
  ON (i.etiqueta_incidencias='INC-01' AND s.etiqueta_segmentos='SEG-B3')
  OR (i.etiqueta_incidencias='INC-02' AND s.etiqueta_segmentos='SEG-B1')
  OR (i.etiqueta_incidencias='INC-05' AND s.etiqueta_segmentos='SEG-B4')
WHERE NOT EXISTS (SELECT 1 FROM incidencia_segmento x WHERE x.id_incidencia = i.id_incidencias AND x.id_segmento = s.id_segmentos);

INSERT INTO incidencia_landing_station (incidencias_id_incidencias, landing_stations_idlanding_stations)
SELECT i.id_incidencias, l.id_landing_stations
FROM incidencias i JOIN landing_stations l
  ON (i.etiqueta_incidencias='INC-03' AND l.etiqueta_station='LS-06 Guayaquil')
  OR (i.etiqueta_incidencias='INC-04' AND l.etiqueta_station='LS-05 Arica')
WHERE NOT EXISTS (SELECT 1 FROM incidencia_landing_station x WHERE x.incidencias_id_incidencias = i.id_incidencias AND x.landing_stations_idlanding_stations = l.id_landing_stations);

-- ---------- SOLICITUDES DE CAPACIDAD ----------
INSERT INTO solicitudes_capacidad (etiqueta_solicitud, id_cliente, id_estacion_origen, id_estacion_destino, capacidad_solicitada_gbps, fecha_solicitud, estado, observaciones)
SELECT n.e,
       (SELECT id_clientes FROM clientes WHERE etiqueta_cliente = n.cli LIMIT 1),
       (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station = n.o),
       (SELECT id_landing_stations FROM landing_stations WHERE etiqueta_station = n.d),
       n.cap, n.f, n.est, n.obs
FROM (
  SELECT 'SOL-001' AS e, 'CLI-01' AS cli, 'LS-01 Lima' AS o, 'LS-04 Tacna' AS d, 20.00 AS cap, '2026-09-03 10:00:00' AS f, 'Registrada' AS est, 'Solicitud inicial.' AS obs
  UNION ALL SELECT 'SOL-002', 'CLI-02', 'LS-01 Lima',     'LS-02 Piura',   15.00, '2026-09-05 11:30:00', 'En evaluación', 'Revisando disponibilidad del segmento.'
  UNION ALL SELECT 'SOL-003', 'CLI-03', 'LS-02 Piura',    'LS-05 Arica',   10.00, '2026-09-07 09:15:00', 'Aprobada', NULL
  UNION ALL SELECT 'SOL-004', 'CLI-04', 'LS-03 Arequipa', 'LS-04 Tacna',    8.00, '2026-09-09 16:40:00', 'Rechazada', 'Sin capacidad disponible en la ruta.'
  UNION ALL SELECT 'SOL-005', 'CLI-05', 'LS-01 Lima',     'LS-05 Arica',   25.00, '2026-10-01 08:00:00', 'Activa', NULL
) AS n
WHERE NOT EXISTS (SELECT 1 FROM solicitudes_capacidad s WHERE s.etiqueta_solicitud = n.e);

-- ---------- COMPROBACIÓN ----------
SELECT 'roles' AS tabla, COUNT(*) AS filas FROM roles
UNION ALL SELECT 'usuarios', COUNT(*) FROM usuarios
UNION ALL SELECT 'landing_stations', COUNT(*) FROM landing_stations
UNION ALL SELECT 'segmentos', COUNT(*) FROM segmentos
UNION ALL SELECT 'clientes', COUNT(*) FROM clientes
UNION ALL SELECT 'mantenimientos', COUNT(*) FROM mantenimientos
UNION ALL SELECT 'mant_infraestructura', COUNT(*) FROM mantenimiento_infraestructura
UNION ALL SELECT 'incidencias', COUNT(*) FROM incidencias
UNION ALL SELECT 'solicitudes_capacidad', COUNT(*) FROM solicitudes_capacidad;
