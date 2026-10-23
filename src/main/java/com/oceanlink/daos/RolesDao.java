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
}
