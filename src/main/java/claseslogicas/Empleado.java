package claseslogicas;

import java.time.LocalDate;

public class Empleado extends Persona {

    private boolean esEstilista;
    private LocalDate fechaIngreso;
    private String puesto;
    private int idEmpleado;
    private Rol rol;
    private double porcentajeComision;
    private boolean activo = true;

    public Persona persona;

    public Empleado() {
        super();
    }

    public Empleado(int idPersona, String nombre, String apellido, boolean esEstilista) {
        super();
        super.setIdPersona(idPersona);
        super.setNombre(nombre);
        super.setApellido(apellido);
        this.esEstilista = esEstilista;
    }

    public String getNombreCompleto() {
        return getNombre() + " " + getApellido();
    }

    @Override
    public String toString() {

        if (this.persona != null) {
            return this.persona.getNombre() + " " + this.persona.getApellido();
        }

        return getNombre() + " " + getApellido();
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Rol getRol() {
        return rol;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(double porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isEsEstilista() {
        return esEstilista;
    }

    public void setEsEstilista(boolean esEstilista) {
        this.esEstilista = esEstilista;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }
}