package claseslogicas;

import java.math.BigDecimal;

public class RankingServicio {

    private final int idServicio;
    private final int posicion;
    private final String servicio;
    private final int cantidadRealizada;
    private final BigDecimal ingresos;
    private final BigDecimal costoEstimado;
    private final BigDecimal comisiones;
    private final BigDecimal margenEstimado;

    public RankingServicio(int idServicio,
                           int posicion,
                           String servicio,
                           int cantidadRealizada,
                           BigDecimal ingresos,
                           BigDecimal costoEstimado,
                           BigDecimal comisiones,
                           BigDecimal margenEstimado) {
        this.idServicio = idServicio;
        this.posicion = posicion;
        this.servicio = servicio;
        this.cantidadRealizada = cantidadRealizada;
        this.ingresos = ingresos;
        this.costoEstimado = costoEstimado;
        this.comisiones = comisiones;
        this.margenEstimado = margenEstimado;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public int getPosicion() {
        return posicion;
    }

    public String getServicio() {
        return servicio;
    }

    public int getCantidadRealizada() {
        return cantidadRealizada;
    }

    public BigDecimal getIngresos() {
        return ingresos;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public BigDecimal getComisiones() {
        return comisiones;
    }

    public BigDecimal getMargenEstimado() {
        return margenEstimado;
    }
}
