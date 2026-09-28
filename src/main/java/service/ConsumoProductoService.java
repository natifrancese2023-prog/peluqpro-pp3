package service;

import dao.ConsumoProductoDAO;
import java.sql.SQLException;

public class ConsumoProductoService {
    private final ConsumoProductoDAO dao=new ConsumoProductoDAO();
    public void registrar(int producto,int cantidad,int usuario)throws SQLException{
        if(producto<=0||usuario<=0)throw new IllegalArgumentException("Datos de consumo inválidos.");
        if(cantidad<=0)throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        dao.registrarConsumo(producto,cantidad,usuario);
    }
}
