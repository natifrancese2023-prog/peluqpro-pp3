package claseslogicas;

import java.math.BigDecimal;

public class DetalleRankingServicio {

    private final String profesional;
    private final int cantidad;
    private final BigDecimal porcentajeComision;
    private final BigDecimal ingresos;
    private final BigDecimal costoEstimado;
    private final BigDecimal comision;
    private final BigDecimal margenEstimado;

    public DetalleRankingServicio(String profesional,
                                  int cantidad,
                                  BigDecimal porcentajeComision,
                                  BigDecimal ingresos,
                                  BigDecimal costoEstimado,
                                  BigDecimal comision,
                                  BigDecimal margenEstimado) {
        this.profesional = profesional;
        this.cantidad = cantidad;
        this.porcentajeComision = porcentajeComision;
        this.ingresos = ingresos;
        this.costoEstimado = costoEstimado;
        this.comision = comision;
        this.margenEstimado = margenEstimado;
    }

    public String getProfesional() {
        return profesional;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public BigDecimal getIngresos() {
        return ingresos;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public BigDecimal getMargenEstimado() {
        return margenEstimado;
    }
}
