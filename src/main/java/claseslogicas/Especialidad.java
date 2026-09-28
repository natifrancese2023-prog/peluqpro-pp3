package claseslogicas;

public class Especialidad {

    private int idEspecialidad;
    private String nombre;
    private boolean activo;

    public Especialidad() {
        this.activo = true;
    }

    public Especialidad(int idEspecialidad, String nombre, boolean activo) {
        this.idEspecialidad = idEspecialidad;
        this.nombre = nombre;
        this.activo = activo;
    }

    public int getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(int idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
