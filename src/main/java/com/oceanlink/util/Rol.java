package com.oceanlink.util;

/**
 * Los 5 roles del sistema. nombreBd es el texto exacto de roles.nombres_rol;
 * clave es la que usa el menú lateral (sidebar.js) para armar las opciones.
 */
public enum Rol {
    ADMIN("Network Admin", "admin"),
    CAPACITY_PLANNER("Capacity Planner", "capacity-planner"),
    SUPERVISOR("Supervisor", "supervisor"),
    NETWORK_OPERATOR("Network Operator", "network-operator"),
    MAINTENANCE_COORDINATOR("Maintenance Coordinator", "maintenance-coordinator");

    private final String nombreBd;
    private final String clave;

    Rol(String nombreBd, String clave) {
        this.nombreBd = nombreBd;
        this.clave = clave;
    }

    public String getNombreBd() { return nombreBd; }

    public String getClave() { return clave; }

    /** Devuelve el rol que corresponde al nombre guardado en la BD, o null si no lo conocemos. */
    public static Rol porNombreBd(String nombre) {
        if (nombre == null) return null;
        for (Rol r : values()) {
            if (r.nombreBd.equalsIgnoreCase(nombre.trim())) return r;
        }
        return null;
    }
}
