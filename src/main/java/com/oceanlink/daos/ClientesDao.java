package com.oceanlink.daos;

import com.oceanlink.beans.Clientes;

import java.sql.*;
import java.util.ArrayList;

/**
 * CRUD de la tabla clientes (columnas reales: id_clientes, nombres, correos,
 * telefonos, estado, etiqueta_cliente). Usa ConexionBD, igual que los demás DAO.
 */
public class ClientesDao {

    private static final String SELECT_BASE =
            "SELECT id_clientes, nombres, correos, telefonos, estado, etiqueta_cliente FROM clientes ";

    public ArrayList<Clientes> getListaClientes() {
        ArrayList<Clientes> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY id_clientes";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Clientes getClientes(int idCliente) {
        Clientes c = null;
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BASE + "WHERE id_clientes = ?")) {
            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    c = mapear(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    /** @return true si se guardó */
    public boolean crearClientes(String etiqueta, String nombre, String correo, String telefono, String estado) {
        String sql = "INSERT INTO clientes(etiqueta_cliente, nombres, correos, telefonos, estado) VALUES (?,?,?,?,?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, etiqueta);
            pstmt.setString(2, nombre);
            pstmt.setString(3, correo);
            pstmt.setString(4, telefono);
            pstmt.setString(5, estado);
            pstmt.executeUpdate();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** @return true si se actualizó */
    public boolean actualizarClientes(int idCliente, String etiqueta, String nombre, String correo, String telefono, String estado) {
        // OJO: las columnas se llaman etiqueta_cliente y telefonos (en la rama wip estaban mal escritas)
        String sql = "UPDATE clientes SET etiqueta_cliente = ?, nombres = ?, correos = ?, telefonos = ?, estado = ? " +
                     "WHERE id_clientes = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, etiqueta);
            pstmt.setString(2, nombre);
            pstmt.setString(3, correo);
            pstmt.setString(4, telefono);
            pstmt.setString(5, estado);
            pstmt.setInt(6, idCliente);
            return pstmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return true si se borró. Falla (false) si el cliente tiene servicios o
     * solicitudes de capacidad, porque esas FK son ON DELETE RESTRICT.
     */
    public boolean borrarClientes(int idCliente) {
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM clientes WHERE id_clientes = ?")) {
            pstmt.setInt(1, idCliente);
            return pstmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Clientes mapear(ResultSet rs) throws SQLException {
        Clientes c = new Clientes();
        c.setIdCliente(rs.getInt("id_clientes"));
        c.setNombre(rs.getString("nombres"));
        c.setCorreo(rs.getString("correos"));
        c.setTelefono(rs.getString("telefonos"));
        c.setEstado(rs.getString("estado"));
        c.setEtiqueta(rs.getString("etiqueta_cliente"));
        return c;
    }
}
