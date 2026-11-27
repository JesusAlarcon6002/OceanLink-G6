package com.oceanlink.daos;

import com.oceanlink.beans.Roles;

import java.sql.*;
import java.util.ArrayList;

public class RolesDao extends DaoBase {
    public ArrayList<Roles> obtenerListaRoles(){
        ArrayList<Roles> listaRoles = new ArrayList<>();
        try {

            Connection conn = getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT id_roles, nombres_rol FROM roles");

            while(rs.next()){
                Roles rl = new Roles();
                rl.setIdRoles(rs.getInt("id_roles"));
                rl.setNombreRoles(rs.getString("nombres_rol"));
                listaRoles.add(rl);
            }
        } catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return listaRoles;
    }

    /** Cuántos usuarios ACTIVOS tiene cada rol (clave = nombre del rol en la BD). */
    public java.util.Map<String, Integer> contarUsuariosActivosPorRol() {
        java.util.Map<String, Integer> conteo = new java.util.HashMap<>();
        String sql = "SELECT r.nombres_rol, COUNT(u.idusuarios) FROM roles r "
                + "LEFT JOIN usuarios u ON u.roles_idroles = r.id_roles AND u.estado = 'Activo' GROUP BY r.id_roles, r.nombres_rol";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) conteo.put(rs.getString(1), rs.getInt(2));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conteo;
    }
}
