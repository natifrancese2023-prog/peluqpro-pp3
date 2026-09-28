package dao;

import claseslogicas.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorStockDAO {
    private Proveedor map(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id_proveedor"),rs.getString("nombre"),rs.getString("telefono"),rs.getString("email"),rs.getString("direccion"));
    }
    public void insertar(Proveedor p) throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement("INSERT INTO proveedores_stock(nombre,telefono,email,direccion) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,p.getNombre()); ps.setString(2,p.getTelefono()); ps.setString(3,p.getEmail()); ps.setString(4,p.getDireccion()); ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())p.setIdProveedor(rs.getInt(1));}
        }
    }
    public void actualizar(Proveedor p) throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE proveedores_stock SET nombre=?,telefono=?,email=?,direccion=? WHERE id_proveedor=?")) {
            ps.setString(1,p.getNombre()); ps.setString(2,p.getTelefono()); ps.setString(3,p.getEmail()); ps.setString(4,p.getDireccion()); ps.setInt(5,p.getIdProveedor()); ps.executeUpdate();
        }
    }
    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> l=new ArrayList<>();
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT id_proveedor,nombre,telefono,email,direccion FROM proveedores_stock ORDER BY nombre"); ResultSet rs=ps.executeQuery()) { while(rs.next())l.add(map(rs)); }
        return l;
    }
    public Proveedor obtenerPorId(int id) throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT id_proveedor,nombre,telefono,email,direccion FROM proveedores_stock WHERE id_proveedor=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){if(rs.next())return map(rs);}}
        return null;
    }
}
