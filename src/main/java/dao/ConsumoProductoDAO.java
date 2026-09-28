package dao;

import java.sql.*;

public class ConsumoProductoDAO {
    public void registrarConsumo(int idProducto,int cantidad,int idUsuario) throws SQLException {
        try(Connection c=ConexionBD.getConnection()) {
            c.setAutoCommit(false);
            try {
                int stock;
                try(PreparedStatement ps=c.prepareStatement("SELECT stock_actual FROM productos_stock WHERE id_producto=? AND activo=true FOR UPDATE")){
                    ps.setInt(1,idProducto); try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new SQLException("El producto no existe o está inactivo."); stock=rs.getInt(1);}
                }
                if(stock<cantidad) throw new SQLException("Stock insuficiente. Stock actual: "+stock);
                try(PreparedStatement ps=c.prepareStatement("UPDATE productos_stock SET stock_actual=stock_actual-? WHERE id_producto=?")){ps.setInt(1,cantidad);ps.setInt(2,idProducto);ps.executeUpdate();}
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO consumo_producto_stock(id_producto,cantidad,id_usuario) VALUES(?,?,?)")){ps.setInt(1,idProducto);ps.setInt(2,cantidad);ps.setInt(3,idUsuario);ps.executeUpdate();}
                c.commit();
            }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;throw new SQLException("No se pudo registrar el consumo.",e);}
            finally{c.setAutoCommit(true);}
        }
    }
}
