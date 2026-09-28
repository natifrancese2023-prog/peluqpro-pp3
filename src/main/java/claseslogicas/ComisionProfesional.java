package claseslogicas;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ComisionProfesional {

    private LocalDate fecha;
    private String servicio;
    private BigDecimal precio;
    private double porcentajeComision;
    private BigDecimal comision;

    public ComisionProfesional() {
    }

    public ComisionProfesional(LocalDate fecha,
                               String servicio,
                               BigDecimal precio,
                               double porcentajeComision,
                               BigDecimal comision) {
        this.fecha = fecha;
        this.servicio = servicio;
        this.precio = precio;
        this.porcentajeComision = porcentajeComision;
        this.comision = comision;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(double porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public void setComision(BigDecimal comision) {
        this.comision = comision;
    }
}