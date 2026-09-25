package claseslogicas;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FacturaResumen {

    private LocalDate fecha;
    private BigDecimal totalFacturado;
    private BigDecimal efectivo;
    private BigDecimal transferencia;
    private BigDecimal debito;
    private int cantidadFacturas;

    public FacturaResumen(
            LocalDate fecha,
            BigDecimal totalFacturado,
            BigDecimal efectivo,
            BigDecimal transferencia,
            BigDecimal debito,
            int cantidadFacturas) {

        this.fecha = fecha;
        this.totalFacturado = totalFacturado;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
        this.debito = debito;
        this.cantidadFacturas = cantidadFacturas;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getTotalFacturado() {
        return totalFacturado;
    }

    public BigDecimal getEfectivo() {
        return efectivo != null ? efectivo : BigDecimal.ZERO;
    }

    public BigDecimal getTransferencia() {
        return transferencia != null ? transferencia : BigDecimal.ZERO;
    }

    public BigDecimal getDebito() {
        return debito != null ? debito : BigDecimal.ZERO;
    }

    public int getCantidadFacturas() {
        return cantidadFacturas;
    }
}