package dao;

import claseslogicas.CompraResumen;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompraStockDAO {
    public void registrarCompra(int idProveedor,int idProducto,int cantidad,BigDecimal precioUnitario,int idUsuario) throws SQLException {
        String compraSql="INSERT INTO compras_stock(id_proveedor,id_usuario,total) VALUES(?,?,?)";
        String detalleSql="INSERT INTO detalle_compra_stock(id_compra,id_producto,cantidad,precio_unitario) VALUES(?,?,?,?)";
        String stockSql="UPDATE productos_stock SET stock_actual=stock_actual+? WHERE id_producto=? AND activo=true";
        try(Connection c=ConexionBD.getConnection()) {
            c.setAutoCommit(false);
            try {
                int compraId;
                BigDecimal total=precioUnitario.multiply(BigDecimal.valueOf(cantidad));
                try(PreparedStatement ps=c.prepareStatement(compraSql,Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1,idProveedor); ps.setInt(2,idUsuario); ps.setBigDecimal(3,total); ps.executeUpdate();
                    try(ResultSet rs=ps.getGeneratedKeys()){ if(!rs.next())throw new SQLException("No se pudo generar la compra."); compraId=rs.getInt(1); }
                }
                try(PreparedStatement ps=c.prepareStatement(detalleSql)){ps.setInt(1,compraId);ps.setInt(2,idProducto);ps.setInt(3,cantidad);ps.setBigDecimal(4,precioUnitario);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement(stockSql)){ps.setInt(1,cantidad);ps.setInt(2,idProducto);if(ps.executeUpdate()!=1)throw new SQLException("El producto no existe o está inactivo.");}
                c.commit();
            } catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;throw new SQLException("No se pudo registrar la compra.",e);}
            finally{c.setAutoCommit(true);}
        }
    }

    public List<CompraResumen> listar() throws SQLException {
        List<CompraResumen> l=new ArrayList<>();
        String sql="SELECT c.id_compra,c.fecha,pv.nombre proveedor,p.nombre producto,d.cantidad,d.precio_unitario,c.total,u.usuario " +
                "FROM compras_stock c JOIN proveedores_stock pv ON pv.id_proveedor=c.id_proveedor " +
                "JOIN detalle_compra_stock d ON d.id_compra=c.id_compra JOIN productos_stock p ON p.id_producto=d.id_producto " +
                "JOIN usuarios u ON u.id=c.id_usuario ORDER BY c.fecha DESC,c.id_compra DESC";
        try(Connection c=ConexionBD.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next())l.add(new CompraResumen(rs.getInt("id_compra"),rs.getString("proveedor"),rs.getString("producto"),rs.getInt("cantidad"),rs.getBigDecimal("precio_unitario"),rs.getBigDecimal("total"),rs.getTimestamp("fecha").toLocalDateTime(),rs.getString("usuario")));
        }
        return l;
    }
}
