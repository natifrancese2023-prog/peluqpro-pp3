package service;

import claseslogicas.Producto;
import dao.ProductoStockDAO;
import java.sql.SQLException;
import java.util.List;

public class ProductoStockService {
    private final ProductoStockDAO dao=new ProductoStockDAO();
    public void registrar(Producto p)throws SQLException{validar(p);dao.insertar(p);}
    public void actualizar(Producto p)throws SQLException{if(p==null||p.getIdProducto()<=0)throw new IllegalArgumentException("Producto inválido.");validar(p);dao.actualizar(p);}
    public void actualizarEstado(int id,boolean activo)throws SQLException{if(id<=0)throw new IllegalArgumentException("Producto inválido.");dao.actualizarEstado(id,activo);}
    public List<Producto> listar()throws SQLException{return dao.listar();}
    public Producto obtener(int id)throws SQLException{return dao.obtenerPorId(id);}
    private void validar(Producto p){
        if(p==null||p.getNombre()==null||p.getNombre().trim().isEmpty())throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        if(p.getStockActual()<0)throw new IllegalArgumentException("El stock no puede ser negativo.");
        if(p.getStockMinimo()<0)throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
    }
}
