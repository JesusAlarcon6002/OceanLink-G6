package com.oceanlink.util;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de permisos fija (en código): qué rol puede usar cada Servlet.
 *  - "duenios": roles con acceso completo (ver, crear, editar, borrar).
 *  - "lectores": otros roles con acceso de SOLO LECTURA a ese módulo (ej. el Maintenance Coordinator ve las incidencias).
 *  - El Supervisor tiene acceso de SOLO LECTURA (GET de la lista, action=ver y action=historial) a todos los módulos menos Usuarios.
 */
public final class Permisos {

    public static final class Modulo {
        public final String ruta;
        public final String nombre;
        public final Set<Rol> duenios;
        public final boolean supervisorLee;
        /** Roles que solo pueden consultar el módulo (además del Supervisor, si corresponde). */
        public final Set<Rol> lectores;
        /** false = servicio de apoyo (ej. verificación de correo): se controla por rol pero no sale como tarjeta en el inicio. */
        public final boolean visible;

        Modulo(String ruta, String nombre, Set<Rol> duenios, boolean supervisorLee) {
            this(ruta, nombre, duenios, supervisorLee, true);
        }

        Modulo(String ruta, String nombre, Set<Rol> duenios, boolean supervisorLee, boolean visible) {
            this(ruta, nombre, duenios, supervisorLee, visible, EnumSet.noneOf(Rol.class));
        }

        Modulo(String ruta, String nombre, Set<Rol> duenios, boolean supervisorLee, boolean visible, Set<Rol> lectores) {
            this.lectores = lectores;
            this.ruta = ruta;
            this.nombre = nombre;
            this.duenios = duenios;
            this.supervisorLee = supervisorLee;
            this.visible = visible;
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
        // El Network Operator también ve el dashboard y descarga reportes, pero de reportes solo los de incidencias (lo limita ReportesServlet)
        addVarios("/DashboardServlet", "Dashboard de la red", Rol.SUPERVISOR, Rol.ADMIN, Rol.NETWORK_OPERATOR);
        addVarios("/ReportesServlet", "Reportes", Rol.SUPERVISOR, Rol.ADMIN, Rol.NETWORK_OPERATOR);
        // Registro de los correos enviados por el sistema: solo Network Admin
        add("/CorreoServlet", "Registro de correos", Rol.ADMIN, false);
        // Comunicados escritos a mano para clientes y usuarios (por rol): solo Network Admin
        add("/ComunicadosServlet", "Enviar comunicado", Rol.ADMIN, false);
        // Solicitudes de registro de cuentas nuevas: solo Network Admin (única forma de crear usuarios)
        add("/SolicitudesRegistroServlet", "Solicitudes de registro", Rol.ADMIN, false);
        // El Maintenance Coordinator consulta las incidencias (solo lectura) para decidir dónde aplicar mantenimiento
        conLectores("/IncidenciasServlet", Rol.MAINTENANCE_COORDINATOR);
        // Roles y permisos (solo consulta): qué puede hacer cada rol y cuántos usuarios tiene
        add("/RolesServlet", "Roles y permisos", Rol.ADMIN, false);
        // Verificación de correo (código de 6 dígitos) al registrar usuarios y clientes: no es una tarjeta del inicio
        MODULOS.put("/VerificarCorreoServlet", new Modulo("/VerificarCorreoServlet", "Verificación de correo",
                EnumSet.of(Rol.ADMIN, Rol.CAPACITY_PLANNER), false, false));
    }

    private Permisos() {
    }

    private static void add(String ruta, String nombre, Rol duenio, boolean supervisorLee) {
        MODULOS.put(ruta, new Modulo(ruta, nombre, EnumSet.of(duenio), supervisorLee));
    }

    private static void addVarios(String ruta, String nombre, Rol primero, Rol... otros) {
        MODULOS.put(ruta, new Modulo(ruta, nombre, EnumSet.of(primero, otros), false));
    }

    /** Agrega roles de solo lectura a un módulo que ya existe. */
    private static void conLectores(String ruta, Rol primero, Rol... otros) {
        Modulo m = MODULOS.get(ruta);
        MODULOS.put(ruta, new Modulo(m.ruta, m.nombre, m.duenios, m.supervisorLee, m.visible, EnumSet.of(primero, otros)));
    }

    public static Modulo modulo(String ruta) {
        return MODULOS.get(ruta);
    }

    /** Módulos que se muestran como tarjetas/menú (sin los servicios de apoyo). */
    public static Iterable<Modulo> todos() {
        java.util.List<Modulo> visibles = new java.util.ArrayList<>();
        for (Modulo m : MODULOS.values()) {
            if (m.visible) visibles.add(m);
        }
        return visibles;
    }

    /** ¿Puede este rol hacer esta petición (método + action) sobre este Servlet? */
    public static boolean permite(Rol rol, String ruta, String metodo, String action) {
        Modulo m = MODULOS.get(ruta);
        if (m == null || rol == null) return false;
        if (m.duenios.contains(rol)) return true;
        if ((rol == Rol.SUPERVISOR && m.supervisorLee) || m.lectores.contains(rol)) {
            boolean soloConsultar = action == null || action.isEmpty() || "lista".equals(action)
                    || "ver".equals(action) || "historial".equals(action);
            return "GET".equalsIgnoreCase(metodo) && soloConsultar;
        }
        return false;
    }

    /** ¿Este rol ve el módulo en su pantalla de inicio / menú (aunque sea solo lectura)? */
    public static boolean veModulo(Rol rol, Modulo m) {
        return m.duenios.contains(rol) || m.lectores.contains(rol) || (rol == Rol.SUPERVISOR && m.supervisorLee);
    }
}
