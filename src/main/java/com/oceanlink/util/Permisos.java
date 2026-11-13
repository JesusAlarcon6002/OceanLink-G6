package com.oceanlink.util;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de permisos fija (en código): qué rol puede usar cada Servlet.
 *  - "duenios": roles con acceso completo (ver, crear, editar, borrar).
 *  - El Supervisor tiene acceso de SOLO LECTURA (GET de la lista) a todos los módulos menos Usuarios.
 */
public final class Permisos {

    public static final class Modulo {
        public final String ruta;
        public final String nombre;
        public final Set<Rol> duenios;
        public final boolean supervisorLee;

        Modulo(String ruta, String nombre, Set<Rol> duenios, boolean supervisorLee) {
            this.ruta = ruta;
            this.nombre = nombre;
            this.duenios = duenios;
            this.supervisorLee = supervisorLee;
        }
    }

    private static final Map<String, Modulo> MODULOS = new LinkedHashMap<>();

    static {
        add("/UsuariosServlet", "Usuarios", Rol.ADMIN, false);
        add("/EstacionesServlet", "Estaciones de aterrizaje", Rol.ADMIN, true);
        add("/SegmentosServlet", "Segmentos de cable", Rol.ADMIN, true);
        add("/ClientesServlet", "Clientes", Rol.CAPACITY_PLANNER, true);
        add("/SolicitudesServlet", "Solicitudes de capacidad", Rol.CAPACITY_PLANNER, true);
        add("/RutasServlet", "Rutas", Rol.CAPACITY_PLANNER, true);
        add("/ServiciosServlet", "Servicios", Rol.CAPACITY_PLANNER, true);
        add("/HistorialServlet", "Historial de capacidad", Rol.CAPACITY_PLANNER, true);
        add("/ReservasServlet", "Reservas de capacidad", Rol.CAPACITY_PLANNER, true);
        add("/IncidenciasServlet", "Incidencias", Rol.NETWORK_OPERATOR, true);
        add("/MantenimientosServlet", "Mantenimientos", Rol.MAINTENANCE_COORDINATOR, true);
        // Dashboard y reportes (solo consulta): Supervisor y Network Admin
        addVarios("/DashboardServlet", "Dashboard de la red", Rol.SUPERVISOR, Rol.ADMIN);
        addVarios("/ReportesServlet", "Reportes", Rol.SUPERVISOR, Rol.ADMIN);
        // Prueba del correo SMTP: solo Network Admin
        add("/CorreoServlet", "Prueba de correo", Rol.ADMIN, false);
    }

    private Permisos() {
    }

    private static void add(String ruta, String nombre, Rol duenio, boolean supervisorLee) {
        MODULOS.put(ruta, new Modulo(ruta, nombre, EnumSet.of(duenio), supervisorLee));
    }

    private static void addVarios(String ruta, String nombre, Rol primero, Rol... otros) {
        MODULOS.put(ruta, new Modulo(ruta, nombre, EnumSet.of(primero, otros), false));
    }

    public static Modulo modulo(String ruta) {
        return MODULOS.get(ruta);
    }

    public static Iterable<Modulo> todos() {
        return MODULOS.values();
    }

    /** ¿Puede este rol hacer esta petición (método + action) sobre este Servlet? */
    public static boolean permite(Rol rol, String ruta, String metodo, String action) {
        Modulo m = MODULOS.get(ruta);
        if (m == null || rol == null) return false;
        if (m.duenios.contains(rol)) return true;
        if (rol == Rol.SUPERVISOR && m.supervisorLee) {
            boolean soloListar = action == null || action.isEmpty() || "lista".equals(action);
            return "GET".equalsIgnoreCase(metodo) && soloListar;
        }
        return false;
    }

    /** ¿Este rol ve el módulo en su pantalla de inicio / menú (aunque sea solo lectura)? */
    public static boolean veModulo(Rol rol, Modulo m) {
        return m.duenios.contains(rol) || (rol == Rol.SUPERVISOR && m.supervisorLee);
    }
}
