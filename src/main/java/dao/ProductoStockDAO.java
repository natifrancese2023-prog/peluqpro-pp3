package dao;

import claseslogicas.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoStockDAO {
    private Producto map(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id_producto"), rs.getString("nombre"), rs.getString("descripcion"),
                rs.getInt("stock_actual"), rs.getInt("stock_minimo"), rs.getBoolean("activo"));
    }

    public void insertar(Producto p) throws SQLException {
        String sql="INSERT INTO productos_stock(nombre,descripcion,stock_actual,stock_minimo,activo) VALUES(?,?,?,?,?)";
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,p.getNombre()); ps.setString(2,p.getDescripcion()); ps.setInt(3,p.getStockActual());
            ps.setInt(4,p.getStockMinimo()); ps.setBoolean(5,p.isActivo()); ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){ if(rs.next()) p.setIdProducto(rs.getInt(1)); }
        }
    }

    public void actualizar(Producto p) throws SQLException {
        String sql="UPDATE productos_stock SET nombre=?, descripcion=?, stock_minimo=? WHERE id_producto=?";
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,p.getNombre()); ps.setString(2,p.getDescripcion()); ps.setInt(3,p.getStockMinimo()); ps.setInt(4,p.getIdProducto()); ps.executeUpdate();
        }
    }

    public void actualizarEstado(int id, boolean activo) throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE productos_stock SET activo=? WHERE id_producto=?")) {
            ps.setBoolean(1,activo); ps.setInt(2,id); ps.executeUpdate();
        }
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> lista=new ArrayList<>();
        String sql="SELECT id_producto,nombre,descripcion,stock_actual,stock_minimo,activo FROM productos_stock ORDER BY nombre";
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public Producto obtenerPorId(int id) throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT id_producto,nombre,descripcion,stock_actual,stock_minimo,activo FROM productos_stock WHERE id_producto=?")) {
            ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()){ if(rs.next()) return map(rs); }
        }
        return null;
    }
}
