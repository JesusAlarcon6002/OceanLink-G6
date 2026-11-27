-- =====================================================================
-- OceanLink - cambios de esquema para Rutas, Reservas, Servicios e Historial
-- Ejecutar UNA vez por persona en MySQL Workbench (se puede repetir sin problema:
-- si el cambio ya está hecho, no hace nada).
--
-- 1) servicios.id_solicitud: cada servicio recuerda de qué solicitud de capacidad nació.
-- 2) historial_utilizacion.id_servicio acepta NULL: las reservas se registran
--    ANTES de que exista el servicio, así que en ese momento no hay servicio aún.
-- 3) solicitudes_registro: quien quiere una cuenta manda una solicitud y un Network Admin
--    la acepta o la rechaza (tabla nueva).
-- 4) notificaciones: bandeja de la campana de cada usuario (avisos del sistema y mensajes
--    entre usuarios) (tabla nueva).
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

-- 3) solicitudes_registro
CREATE TABLE IF NOT EXISTS solicitudes_registro (
  id_solicitud_registro INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  correo VARCHAR(150) NOT NULL,
  telefono VARCHAR(20) NULL,
  password_hash VARCHAR(255) NOT NULL,          -- la clave que eligió, ya con hash (nunca en texto plano)
  estado ENUM('Pendiente','Aprobada','Rechazada') NOT NULL DEFAULT 'Pendiente',
  fecha_solicitud DATETIME NOT NULL,
  fecha_resolucion DATETIME NULL,
  id_admin_resuelve INT NULL,
  motivo_rechazo VARCHAR(255) NULL,
  PRIMARY KEY (id_solicitud_registro),
  KEY idx_solreg_estado (estado),
  KEY idx_solreg_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) notificaciones (campana)
CREATE TABLE IF NOT EXISTS notificaciones (
  id_notificacion INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,                      -- quien la recibe
  tipo ENUM('Sistema','Mensaje') NOT NULL DEFAULT 'Sistema',
  asunto VARCHAR(200) NOT NULL,
  mensaje TEXT NOT NULL,
  id_remitente INT NULL,                        -- solo en los mensajes entre usuarios
  leida TINYINT(1) NOT NULL DEFAULT 0,
  fecha DATETIME NOT NULL,
  PRIMARY KEY (id_notificacion),
  KEY idx_notif_usuario (id_usuario, leida, fecha),
  CONSTRAINT fk_notif_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (idusuarios) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_notif_remitente FOREIGN KEY (id_remitente) REFERENCES usuarios (idusuarios) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comprobación
SELECT COLUMN_NAME, IS_NULLABLE, TABLE_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'proyecto_siete'
  AND ((TABLE_NAME = 'servicios' AND COLUMN_NAME = 'id_solicitud')
    OR (TABLE_NAME = 'historial_utilizacion' AND COLUMN_NAME = 'id_servicio'));

SELECT TABLE_NAME AS tablas_nuevas FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'proyecto_siete' AND TABLE_NAME IN ('solicitudes_registro', 'notificaciones');
