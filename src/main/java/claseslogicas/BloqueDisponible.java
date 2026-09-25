package claseslogicas;

import claseslogicas.Empleado;
import java.time.LocalTime;


public class BloqueDisponible {

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Empleado estilista;

    public BloqueDisponible(LocalTime horaInicio, LocalTime horaFin, Empleado estilista) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estilista = estilista;
    }

    public String getResumenBloque() {
        String nombreMostrable = "Estilista";

        if (estilista != null) {
            if (estilista.getNombreCompleto() != null
                    && !estilista.getNombreCompleto().equals("null null")) {

                nombreMostrable = estilista.getNombreCompleto();

            } else if (estilista.getPersona() != null) {

                String nombre = estilista.getPersona().getNombre();
                String apellido = estilista.getPersona().getApellido();

                if (nombre != null || apellido != null) {
                    nombreMostrable =
                            (nombre != null ? nombre : "") + " " +
                                    (apellido != null ? apellido : "");
                    nombreMostrable = nombreMostrable.trim();
                }
            }
        }

        return nombreMostrable +
                " | De " + horaInicio +
                " a " + horaFin;
    }

    public LocalTime getHoraInicio()
    {
        return horaInicio;
    }

    public LocalTime getHoraFin()
    { //
        return horaFin;
    }

    public Empleado getEstilista() {
        return estilista;
    }






    public void setEstilista(Empleado estilista)
    {
        this.estilista = estilista;
    }

    @Override

    public String toString() {
        String nombreMostrable = "Estilista";

        if (estilista != null) {

            if (estilista.getNombreCompleto() != null && !estilista.getNombreCompleto().equals("null null")) {
                nombreMostrable = estilista.getNombreCompleto();
            } else if (estilista.getPersona() != null) {
                nombreMostrable = estilista.getPersona().getNombre() + " " + estilista.getPersona().getApellido();
            }
        }

        return nombreMostrable + " | " + horaInicio.toString() + " - " + horaFin.toString();
    }
}