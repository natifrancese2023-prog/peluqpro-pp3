package claseslogicas;

import java.time.LocalTime;

public class Disponibilidad {

    private int idDisponibilidad;
    private int idEmpleado;
    private String diaSemana;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private boolean activo;

    public Disponibilidad() {
        this.activo = true;
    }

    public Disponibilidad(int idDisponibilidad, int idEmpleado, String diaSemana,
                          LocalTime horaDesde, LocalTime horaHasta, boolean activo) {
        this.idDisponibilidad = idDisponibilidad;
        this.idEmpleado = idEmpleado;
        this.diaSemana = diaSemana;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.activo = activo;
    }

    public int getIdDisponibilidad() {
        return idDisponibilidad;
    }

    public void setIdDisponibilidad(int idDisponibilidad) {
        this.idDisponibilidad = idDisponibilidad;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(LocalTime horaDesde) {
        this.horaDesde = horaDesde;
    }

    public LocalTime getHoraHasta() {
        return horaHasta;
    }

    public void setHoraHasta(LocalTime horaHasta) {
        this.horaHasta = horaHasta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return diaSemana + " " + horaDesde + " - " + horaHasta;
    }
}