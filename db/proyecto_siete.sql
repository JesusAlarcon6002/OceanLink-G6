-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: proyecto_siete
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id_clientes` int NOT NULL AUTO_INCREMENT,
  `nombres` varchar(150) NOT NULL,
  `correos` varchar(150) DEFAULT NULL,
  `telefonos` varchar(20) DEFAULT NULL,
  `estado` enum('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  `etiqueta_cliente` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_clientes`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `historial_utilizacion`
--

DROP TABLE IF EXISTS `historial_utilizacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_utilizacion` (
  `id_historial_utilizacion` int NOT NULL AUTO_INCREMENT,
  `id_segmento` int NOT NULL,
  `fecha_movimiento` datetime DEFAULT NULL,
  `capacidad_cambio_gbps` decimal(10,2) DEFAULT NULL,
  `porcentaje_utilizacion` decimal(5,2) NOT NULL,
  `tipo_movimiento` enum('Reserva de capacidad','Liberación de capacidad','Ajuste técnico','Inicio de servicio','Fin de servicio') DEFAULT NULL,
  `capacidad_resultante_gbps` decimal(10,2) DEFAULT NULL,
  `id_solicitud` int NOT NULL,
  `id_servicio` int NOT NULL,
  PRIMARY KEY (`id_historial_utilizacion`),
  KEY `fk_historial_segmento_idx` (`id_segmento`),
  KEY `historial_utilizacion_ibfk_2` (`id_solicitud`),
  KEY `historial_utilizacion_ibfk_3` (`id_servicio`),
  CONSTRAINT `fk_historial_segmento` FOREIGN KEY (`id_segmento`) REFERENCES `segmentos` (`id_segmentos`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `historial_utilizacion_ibfk_1` FOREIGN KEY (`id_segmento`) REFERENCES `segmentos` (`id_segmentos`),
  CONSTRAINT `historial_utilizacion_ibfk_2` FOREIGN KEY (`id_solicitud`) REFERENCES `solicitudes_capacidad` (`idsolicitudes_capacidad`),
  CONSTRAINT `historial_utilizacion_ibfk_3` FOREIGN KEY (`id_servicio`) REFERENCES `servicios` (`id_servicios`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidencia_landing_station`
--

DROP TABLE IF EXISTS `incidencia_landing_station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `incidencia_landing_station` (
  `landing_stations_idlanding_stations` int NOT NULL,
  `incidencias_id_incidencias` int NOT NULL,
  PRIMARY KEY (`landing_stations_idlanding_stations`,`incidencias_id_incidencias`),
  KEY `fk_landing_stations_has_incidencias_incidencias1_idx` (`incidencias_id_incidencias`),
  KEY `fk_landing_stations_has_incidencias_landing_stations1_idx` (`landing_stations_idlanding_stations`),
  CONSTRAINT `fk_landing_stations_has_incidencias_incidencias1` FOREIGN KEY (`incidencias_id_incidencias`) REFERENCES `incidencias` (`id_incidencias`),
  CONSTRAINT `fk_landing_stations_has_incidencias_landing_stations1` FOREIGN KEY (`landing_stations_idlanding_stations`) REFERENCES `landing_stations` (`id_landing_stations`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidencia_segmento`
--

DROP TABLE IF EXISTS `incidencia_segmento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `incidencia_segmento` (
  `id_incidencia` int NOT NULL,
  `id_segmento` int NOT NULL,
  PRIMARY KEY (`id_incidencia`,`id_segmento`),
  KEY `fk_incidencia_segmento_segmento_idx` (`id_segmento`),
  CONSTRAINT `fk_incidencia_segmento_incidencia` FOREIGN KEY (`id_incidencia`) REFERENCES `incidencias` (`id_incidencias`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_incidencia_segmento_segmento` FOREIGN KEY (`id_segmento`) REFERENCES `segmentos` (`id_segmentos`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidencias`
--

DROP TABLE IF EXISTS `incidencias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `incidencias` (
  `id_incidencias` int NOT NULL AUTO_INCREMENT,
  `titulo` varchar(150) NOT NULL,
  `descripcion` text NOT NULL,
  `severidad` enum('Baja','Media','Alta','Critica') NOT NULL,
  `estado` enum('Detectada','En analisis','Reparacion programada','En reparacion','Servicio restaurado','Cerrada') NOT NULL DEFAULT 'Detectada',
  `fecha_deteccion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_restauracion` datetime DEFAULT NULL,
  `solucion` text,
  `observaciones` text,
  `etiqueta_incidencias` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_incidencias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `landing_stations`
--

DROP TABLE IF EXISTS `landing_stations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `landing_stations` (
  `id_landing_stations` int NOT NULL AUTO_INCREMENT,
  `etiqueta_station` varchar(100) NOT NULL,
  `ubicacion` varchar(150) NOT NULL,
  `estado` enum('Operativo','Fuera de servicio','En mantenimiento') NOT NULL DEFAULT 'Operativo',
  PRIMARY KEY (`id_landing_stations`),
  UNIQUE KEY `nombre_etiqueta_UNIQUE` (`etiqueta_station`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mantenimiento_infraestructura`
--

DROP TABLE IF EXISTS `mantenimiento_infraestructura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mantenimiento_infraestructura` (
  `id_mantenimiento_infraestructura` int NOT NULL AUTO_INCREMENT,
  `id_mantenimiento` int NOT NULL,
  `id_segmento` int DEFAULT NULL,
  `id_estacion` int DEFAULT NULL,
  PRIMARY KEY (`id_mantenimiento_infraestructura`),
  KEY `fk_mantenimiento_infraestructura_mantenimiento_idx` (`id_mantenimiento`),
  KEY `fk_mantenimiento_infraestructura_segmento_idx` (`id_segmento`),
  KEY `fk_mantenimiento_infraestructura_estacion_idx` (`id_estacion`),
  CONSTRAINT `fk_mantenimiento_infraestructura_estacion` FOREIGN KEY (`id_estacion`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_mantenimiento_infraestructura_mantenimiento` FOREIGN KEY (`id_mantenimiento`) REFERENCES `mantenimientos` (`id_mantenimientos`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_mantenimiento_infraestructura_segmento` FOREIGN KEY (`id_segmento`) REFERENCES `segmentos` (`id_segmentos`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mantenimientos`
--

DROP TABLE IF EXISTS `mantenimientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mantenimientos` (
  `id_mantenimientos` int NOT NULL AUTO_INCREMENT,
  `tipo` enum('Preventivo','Correctivo') NOT NULL,
  `descripcion` text NOT NULL,
  `fecha_programada` datetime NOT NULL,
  `duracion_estimada_horas` decimal(5,2) NOT NULL,
  `estado` enum('Programado','En curso','Finalizado') NOT NULL DEFAULT 'Programado',
  `fecha_finalizacion` datetime DEFAULT NULL,
  `resultado` text,
  `actividades` text,
  `etiqueta_mantenimientos` varchar(45) NOT NULL,
  PRIMARY KEY (`id_mantenimientos`),
  UNIQUE KEY `etiqueta_mantenimientos_UNIQUE` (`etiqueta_mantenimientos`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permisos`
--

DROP TABLE IF EXISTS `permisos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permisos` (
  `idpermisos` int NOT NULL,
  `nombre_permisos` text,
  PRIMARY KEY (`idpermisos`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permisos_has_roles`
--

DROP TABLE IF EXISTS `permisos_has_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permisos_has_roles` (
  `permisos_idpermisos` int NOT NULL,
  `roles_idroles` int NOT NULL,
  PRIMARY KEY (`permisos_idpermisos`,`roles_idroles`),
  KEY `fk_permisos_has_roles_roles1_idx` (`roles_idroles`),
  KEY `fk_permisos_has_roles_permisos1_idx` (`permisos_idpermisos`),
  CONSTRAINT `fk_permisos_has_roles_permisos1` FOREIGN KEY (`permisos_idpermisos`) REFERENCES `permisos` (`idpermisos`),
  CONSTRAINT `fk_permisos_has_roles_roles1` FOREIGN KEY (`roles_idroles`) REFERENCES `roles` (`id_roles`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id_roles` int NOT NULL AUTO_INCREMENT,
  `nombres_rol` varchar(45) NOT NULL,
  PRIMARY KEY (`id_roles`),
  UNIQUE KEY `nombres_rol_UNIQUE` (`nombres_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ruta_segmento`
--

DROP TABLE IF EXISTS `ruta_segmento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ruta_segmento` (
  `id_ruta` int NOT NULL,
  `id_segmento` int NOT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`id_ruta`,`id_segmento`),
  KEY `fk_ruta_segmento_segmento_idx` (`id_segmento`),
  CONSTRAINT `fk_ruta_segmento_ruta` FOREIGN KEY (`id_ruta`) REFERENCES `rutas` (`idrutas`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ruta_segmento_segmento` FOREIGN KEY (`id_segmento`) REFERENCES `segmentos` (`id_segmentos`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rutas`
--

DROP TABLE IF EXISTS `rutas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rutas` (
  `idrutas` int NOT NULL AUTO_INCREMENT,
  `etiqueta_rutas` varchar(100) NOT NULL,
  `id_estacion_origen` int NOT NULL,
  `id_estacion_destino` int NOT NULL,
  `estado` enum('Activa','Inactiva') NOT NULL DEFAULT 'Activa',
  PRIMARY KEY (`idrutas`),
  UNIQUE KEY `etiqueta_UNIQUE` (`etiqueta_rutas`),
  KEY `fk_ruta_origen_idx` (`id_estacion_origen`),
  KEY `fk_ruta_destino_idx` (`id_estacion_destino`),
  CONSTRAINT `fk_ruta_destino` FOREIGN KEY (`id_estacion_destino`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ruta_origen` FOREIGN KEY (`id_estacion_origen`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segmentos`
--

DROP TABLE IF EXISTS `segmentos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `segmentos` (
  `id_segmentos` int NOT NULL AUTO_INCREMENT,
  `etiqueta_segmentos` varchar(100) NOT NULL,
  `id_estacion_origen` int NOT NULL,
  `id_estacion_destino` int NOT NULL,
  `capacidad_total_gbps` decimal(10,2) NOT NULL,
  `capacidad_utilizada_gbps` decimal(10,2) NOT NULL DEFAULT '0.00',
  `capacidad_reservada_gbps` decimal(10,2) NOT NULL DEFAULT '0.00',
  `estado` enum('Operativo','Degradado','En mantenimiento','Fuera de servicio') NOT NULL DEFAULT 'Operativo',
  PRIMARY KEY (`id_segmentos`),
  UNIQUE KEY `etiqueta` (`etiqueta_segmentos`),
  KEY `fk_segmentos_estacion_origen` (`id_estacion_origen`),
  KEY `fk_segmentos_estacion_destino` (`id_estacion_destino`),
  CONSTRAINT `fk_segmentos_estacion_destino` FOREIGN KEY (`id_estacion_destino`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_segmentos_estacion_origen` FOREIGN KEY (`id_estacion_origen`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `servicios`
--

DROP TABLE IF EXISTS `servicios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicios` (
  `id_servicios` int NOT NULL AUTO_INCREMENT,
  `etiqueta_servicio` varchar(100) NOT NULL,
  `id_cliente` int NOT NULL,
  `id_ruta` int NOT NULL,
  `capacidad_contratada_gbps` decimal(10,2) NOT NULL,
  `fecha_activacion` date DEFAULT NULL,
  `estado` enum('Pendiente','Activo','Suspendido','Finalizado') NOT NULL DEFAULT 'Activo',
  `fecha_expiracion` date DEFAULT NULL,
  PRIMARY KEY (`id_servicios`),
  UNIQUE KEY `etiqueta_UNIQUE` (`etiqueta_servicio`),
  KEY `fk_servicios_cliente_idx` (`id_cliente`),
  KEY `fk_servicios_ruta_idx` (`id_ruta`),
  CONSTRAINT `fk_servicios_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_clientes`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_servicios_ruta` FOREIGN KEY (`id_ruta`) REFERENCES `rutas` (`idrutas`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `solicitud_segmento`
--

DROP TABLE IF EXISTS `solicitud_segmento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitud_segmento` (
  `id_solicitud` int NOT NULL,
  `id_segmento` int NOT NULL,
  `capacidad_reservada_gbps` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id_solicitud`,`id_segmento`),
  KEY `fk_solicitud_segmento_segmento_idx` (`id_segmento`),
  CONSTRAINT `fk_solicitud_segmento_segmento` FOREIGN KEY (`id_segmento`) REFERENCES `segmentos` (`id_segmentos`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_solicitud_segmento_solicitud` FOREIGN KEY (`id_solicitud`) REFERENCES `solicitudes_capacidad` (`idsolicitudes_capacidad`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `solicitudes_capacidad`
--

DROP TABLE IF EXISTS `solicitudes_capacidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitudes_capacidad` (
  `idsolicitudes_capacidad` int NOT NULL AUTO_INCREMENT,
  `id_cliente` int NOT NULL,
  `id_estacion_origen` int NOT NULL,
  `id_estacion_destino` int NOT NULL,
  `capacidad_solicitada_gbps` decimal(10,2) NOT NULL,
  `fecha_solicitud` datetime NOT NULL,
  `estado` enum('Registrada','En evaluación','Aprobada','Rechazada','Provisionada','Activa') NOT NULL DEFAULT 'Registrada',
  `observaciones` text,
  `etiqueta_solicitud` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idsolicitudes_capacidad`),
  KEY `fk_solicitud_cliente_idx` (`id_cliente`),
  KEY `fk_solicitud_origen_idx` (`id_estacion_origen`),
  KEY `fk_solicitud_destino_idx` (`id_estacion_destino`),
  CONSTRAINT `fk_solicitud_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_clientes`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_solicitud_destino` FOREIGN KEY (`id_estacion_destino`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_solicitud_origen` FOREIGN KEY (`id_estacion_origen`) REFERENCES `landing_stations` (`id_landing_stations`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `idusuarios` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `correo` varchar(150) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `estado` enum('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  `roles_idroles` int NOT NULL,
  `etiqueta_usuario` varchar(45) NOT NULL,
  PRIMARY KEY (`idusuarios`),
  UNIQUE KEY `correo_UNIQUE` (`correo`),
  UNIQUE KEY `etiqueta_usuario_UNIQUE` (`etiqueta_usuario`),
  KEY `fk_usuarios_roles_idx` (`roles_idroles`),
  CONSTRAINT `fk_usuarios_roles` FOREIGN KEY (`roles_idroles`) REFERENCES `roles` (`id_roles`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-23 10:03:08
