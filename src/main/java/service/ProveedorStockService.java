package service;

import claseslogicas.Proveedor;
import dao.ProveedorStockDAO;
import java.sql.SQLException;
import java.util.List;

public class ProveedorStockService {
    private final ProveedorStockDAO dao=new ProveedorStockDAO();
    public void registrar(Proveedor p)throws SQLException{validar(p);dao.insertar(p);}
    public void actualizar(Proveedor p)throws SQLException{if(p==null||p.getIdProveedor()<=0)throw new IllegalArgumentException("Proveedor inválido.");validar(p);dao.actualizar(p);}
    public List<Proveedor> listar()throws SQLException{return dao.listar();}
    public Proveedor obtener(int id)throws SQLException{return dao.obtenerPorId(id);}
    private void validar(Proveedor p){if(p==null||p.getNombre()==null||p.getNombre().trim().isEmpty())throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");}
}
