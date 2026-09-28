package controllers;

import claseslogicas.Especialidad;
import service.EspecialidadService;

import java.sql.SQLException;
import java.util.List;

public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController() {
        this.especialidadService = new EspecialidadService();
    }


    // ==========================================================
    // LISTADO
    // ==========================================================

    public List<Especialidad> listarEspecialidades()
            throws SQLException {

        return especialidadService.listarEspecialidades();
    }


    // ==========================================================
    // CONSULTA
    // ==========================================================

    public Especialidad obtenerEspecialidad(int idEspecialidad)
            throws SQLException {

        return especialidadService.obtenerEspecialidad(idEspecialidad);
    }


    // ==========================================================
    // ALTA
    // ==========================================================

    public void registrarEspecialidad(String nombre)
            throws SQLException {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la especialidad es obligatorio."
            );
        }

        Especialidad especialidad = new Especialidad();

        especialidad.setNombre(nombre.trim());
        especialidad.setActivo(true);

        especialidadService.registrarEspecialidad(especialidad);
    }


    // ==========================================================
    // MODIFICACIÓN
    // ==========================================================

    public void modificarEspecialidad(
            int idEspecialidad,
            String nombre) throws SQLException {

        if (idEspecialidad <= 0) {
            throw new IllegalArgumentException(
                    "La especialidad no tiene un identificador válido."
            );
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la especialidad es obligatorio."
            );
        }

        Especialidad especialidad =
                especialidadService.obtenerEspecialidad(idEspecialidad);

        if (especialidad == null) {
            throw new IllegalArgumentException(
                    "No se encontró la especialidad seleccionada."
            );
        }

        especialidad.setNombre(nombre.trim());

        especialidadService.actualizarEspecialidad(especialidad);
    }


    // ==========================================================
    // ACTIVAR / INACTIVAR
    // ==========================================================

    public void activarEspecialidad(int idEspecialidad)
            throws SQLException {

        especialidadService.actualizarEstado(
                idEspecialidad,
                true
        );
    }


    public void inactivarEspecialidad(int idEspecialidad)
            throws SQLException {

        especialidadService.actualizarEstado(
                idEspecialidad,
                false
        );
    }


    public void cambiarEstado(
            int idEspecialidad,
            boolean nuevoEstado) throws SQLException {

        especialidadService.actualizarEstado(
                idEspecialidad,
                nuevoEstado
        );
    }
}