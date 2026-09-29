package claseslogicas;

public class ProductividadProfesional {

    private final int idProfesional;
    private final String nombreProfesional;
    private final long cantidadServicios;
    private final Double promedioDuracionMinutos;

    public ProductividadProfesional(int idProfesional,
                                    String nombreProfesional,
                                    long cantidadServicios,
                                    Double promedioDuracionMinutos) {
        this.idProfesional = idProfesional;
        this.nombreProfesional = nombreProfesional;
        this.cantidadServicios = cantidadServicios;
        this.promedioDuracionMinutos = promedioDuracionMinutos;
    }

    public int getIdProfesional() {
        return idProfesional;
    }

    public String getNombreProfesional() {
        return nombreProfesional;
    }

    public long getCantidadServicios() {
        return cantidadServicios;
    }

    public Double getPromedioDuracionMinutos() {
        return promedioDuracionMinutos;
    }
}
