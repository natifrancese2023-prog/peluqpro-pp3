package service;

import claseslogicas.Especialidad;
import dao.EspecialidadDAO;

import java.sql.SQLException;
import java.util.List;

public class EspecialidadService {

    private final EspecialidadDAO especialidadDAO = new EspecialidadDAO();


    // ==========================================================
    // LISTADO
    // ==========================================================

    public List<Especialidad> listarEspecialidades()
            throws SQLException {

        return especialidadDAO.obtenerTodas();
    }


    // ==========================================================
    // CONSULTA
    // ==========================================================

    public Especialidad obtenerEspecialidad(int idEspecialidad)
            throws SQLException {

        return especialidadDAO.obtenerPorId(idEspecialidad);
    }


    // ==========================================================
    // ALTA
    // ==========================================================

    public void registrarEspecialidad(Especialidad especialidad)
            throws SQLException {

        validarEspecialidad(especialidad);

        especialidadDAO.insertar(especialidad);
    }


    // ==========================================================
    // MODIFICACIÓN
    // ==========================================================

    public void actualizarEspecialidad(Especialidad especialidad)
            throws SQLException {

        validarEspecialidad(especialidad);

        if (especialidad.getIdEspecialidad() <= 0) {
            throw new IllegalArgumentException(
                    "La especialidad no tiene un identificador válido."
            );
        }

        especialidadDAO.actualizar(especialidad);
    }


    // ==========================================================
    // ACTIVAR / INACTIVAR
    // ==========================================================

    public void actualizarEstado(int idEspecialidad, boolean activo)
            throws SQLException {

        if (idEspecialidad <= 0) {
            throw new IllegalArgumentException(
                    "La especialidad no tiene un identificador válido."
            );
        }

        especialidadDAO.actualizarEstado(idEspecialidad, activo);
    }


    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    private void validarEspecialidad(Especialidad especialidad) {

        if (especialidad == null) {
            throw new IllegalArgumentException(
                    "La especialidad no puede ser nula."
            );
        }

        if (especialidad.getNombre() == null ||
                especialidad.getNombre().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre de la especialidad es obligatorio."
            );
        }
    }
}