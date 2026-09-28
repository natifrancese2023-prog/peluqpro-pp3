package service;

import claseslogicas.Servicio;
import dao.EspecialidadServicioDAO;

import java.sql.SQLException;
import java.util.List;

public class EspecialidadServicioService {

    private final EspecialidadServicioDAO especialidadServicioDAO =
            new EspecialidadServicioDAO();


    // ==========================================================
    // SERVICIOS ASOCIADOS
    // ==========================================================

    public List<Servicio> listarServiciosPorEspecialidad(
            int idEspecialidad) throws SQLException {

        validarIdEspecialidad(idEspecialidad);

        return especialidadServicioDAO.obtenerServiciosPorEspecialidad(
                idEspecialidad
        );
    }


    // ==========================================================
    // SERVICIOS DISPONIBLES PARA ASOCIAR
    // ==========================================================

    public List<Servicio> listarServiciosNoAsociados(
            int idEspecialidad) throws SQLException {

        validarIdEspecialidad(idEspecialidad);

        return especialidadServicioDAO.obtenerServiciosNoAsociados(
                idEspecialidad
        );
    }


    // ==========================================================
    // ASOCIAR
    // ==========================================================

    public void asociarServicio(
            int idEspecialidad,
            int idServicio) throws SQLException {

        validarIdEspecialidad(idEspecialidad);
        validarIdServicio(idServicio);

        if (especialidadServicioDAO.existeAsociacion(
                idEspecialidad,
                idServicio)) {

            throw new IllegalArgumentException(
                    "El servicio ya está asociado a esta especialidad."
            );
        }

        especialidadServicioDAO.asociarServicio(
                idEspecialidad,
                idServicio
        );
    }


    // ==========================================================
    // DESASOCIAR
    // ==========================================================

    public void desasociarServicio(
            int idEspecialidad,
            int idServicio) throws SQLException {

        validarIdEspecialidad(idEspecialidad);
        validarIdServicio(idServicio);

        if (!especialidadServicioDAO.existeAsociacion(
                idEspecialidad,
                idServicio)) {

            throw new IllegalArgumentException(
                    "El servicio no está asociado a esta especialidad."
            );
        }

        especialidadServicioDAO.desasociarServicio(
                idEspecialidad,
                idServicio
        );
    }


    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    private void validarIdEspecialidad(int idEspecialidad) {

        if (idEspecialidad <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la especialidad no es válido."
            );
        }
    }


    private void validarIdServicio(int idServicio) {

        if (idServicio <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del servicio no es válido."
            );
        }
    }
}