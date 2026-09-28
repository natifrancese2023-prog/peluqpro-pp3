package claseslogicas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detalle de una factura utilizado por el reporte de facturación por período.
 */
public class FacturaReporteDetalle {
    private final String cliente;
    private final LocalDateTime fechaHora;
    private final BigDecimal monto;
    private final String formaPago;
    private final String estado;

    public FacturaReporteDetalle(String cliente, LocalDateTime fechaHora,
                                 BigDecimal monto, String formaPago, String estado) {
        this.cliente = cliente;
        this.fechaHora = fechaHora;
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.formaPago = formaPago != null && !formaPago.isBlank()
                ? formaPago : "Sin especificar";
        this.estado = estado != null ? estado : "";
    }

    public String getCliente() { return cliente; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public BigDecimal getMonto() { return monto; }
    public String getFormaPago() { return formaPago; }
    public String getEstado() { return estado; }

    public boolean esPagada() {
        return "Pagada".equalsIgnoreCase(estado);
    }
}
