package service;

import claseslogicas.CompraResumen;
import dao.CompraStockDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CompraStockService {
    private final CompraStockDAO dao=new CompraStockDAO();
    public void registrar(int proveedor,int producto,int cantidad,BigDecimal precio,int usuario)throws SQLException{
        if(proveedor<=0||producto<=0||usuario<=0)throw new IllegalArgumentException("Datos de compra inválidos.");
        if(cantidad<=0)throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        if(precio==null||precio.compareTo(BigDecimal.ZERO)<0)throw new IllegalArgumentException("El precio no puede ser negativo.");
        dao.registrarCompra(proveedor,producto,cantidad,precio,usuario);
    }
    public List<CompraResumen> listar()throws SQLException{return dao.listar();}
}
