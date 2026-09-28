package claseslogicas;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class CompraResumen {
    private int idCompra;
    private String proveedor;
    private String producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;
    private LocalDateTime fecha;
    private String usuario;

    public CompraResumen(int idCompra, String proveedor, String producto, int cantidad,
                         BigDecimal precioUnitario, BigDecimal total, LocalDateTime fecha, String usuario) {
        this.idCompra=idCompra; this.proveedor=proveedor; this.producto=producto; this.cantidad=cantidad;
        this.precioUnitario=precioUnitario; this.total=total; this.fecha=fecha; this.usuario=usuario;
    }
    public int getIdCompra(){return idCompra;}
    public String getProveedor(){return proveedor;}
    public String getProducto(){return producto;}
    public int getCantidad(){return cantidad;}
    public BigDecimal getPrecioUnitario(){return precioUnitario;}
    public BigDecimal getTotal(){return total;}
    public LocalDateTime getFecha(){return fecha;}
    public String getUsuario(){return usuario;}
}
