-- =====================================================================
-- OceanLink - cambios de esquema para Rutas, Reservas, Servicios e Historial
-- Ejecutar UNA vez por persona en MySQL Workbench (se puede repetir sin problema:
-- si el cambio ya está hecho, no hace nada).
--
-- 1) servicios.id_solicitud: cada servicio recuerda de qué solicitud de capacidad nació.
-- 2) historial_utilizacion.id_servicio acepta NULL: las reservas se registran
--    ANTES de que exista el servicio, así que en ese momento no hay servicio aún.
-- =====================================================================
USE proyecto_siete;

-- 1) servicios.id_solicitud (con su llave foránea)
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = 'proyecto_siete' AND TABLE_NAME = 'servicios' AND COLUMN_NAME = 'id_solicitud');
SET @sql := IF(@existe = 0,
  'ALTER TABLE servicios
      ADD COLUMN id_solicitud INT NULL AFTER id_ruta,
      ADD CONSTRAINT fk_servicios_solicitud FOREIGN KEY (id_solicitud)
          REFERENCES solicitudes_capacidad (idsolicitudes_capacidad) ON DELETE RESTRICT ON UPDATE CASCADE',
  'SELECT ''servicios.id_solicitud ya existia'' AS aviso');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

-- 2) historial_utilizacion.id_servicio acepta NULL
ALTER TABLE historial_utilizacion MODIFY id_servicio INT NULL;

-- Comprobación
SELECT COLUMN_NAME, IS_NULLABLE, TABLE_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'proyecto_siete'
  AND ((TABLE_NAME = 'servicios' AND COLUMN_NAME = 'id_solicitud')
    OR (TABLE_NAME = 'historial_utilizacion' AND COLUMN_NAME = 'id_servicio'));
