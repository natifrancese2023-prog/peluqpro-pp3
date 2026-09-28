package service;

import claseslogicas.Servicio;
import dao.ServicioDAO;

import java.sql.SQLException;
import java.util.List;

public class ServicioService {

    private final ServicioDAO servicioDAO = new ServicioDAO();

    public List<Servicio> listarServicios() {
        return servicioDAO.obtenerTodos();
    }

    public List<Servicio> listarServiciosPorTurno(int idTurno) {
        return servicioDAO.obtenerServiciosPorTurno(idTurno);
    }


    // ==========================================================
    // ABM DE SERVICIOS
    // ==========================================================

    public void registrarServicio(Servicio servicio) throws SQLException {
        validarServicio(servicio);
        servicioDAO.insertar(servicio);
    }


    public void actualizarServicio(Servicio servicio) throws SQLException {
        validarServicio(servicio);

        if (servicio.getIdServicio() <= 0) {
            throw new IllegalArgumentException(
                    "El servicio no tiene un identificador válido."
            );
        }

        servicioDAO.actualizar(servicio);
    }


    public void actualizarEstado(int idServicio, boolean activo)
            throws SQLException {

        if (idServicio <= 0) {
            throw new IllegalArgumentException(
                    "El servicio no tiene un identificador válido."
            );
        }

        servicioDAO.actualizarEstado(idServicio, activo);
    }


    public Servicio obtenerServicio(int idServicio) throws SQLException {
        return servicioDAO.obtenerPorId(idServicio);
    }


    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    private void validarServicio(Servicio servicio) {

        if (servicio == null) {
            throw new IllegalArgumentException(
                    "El servicio no puede ser nulo."
            );
        }

        if (servicio.getNombreServicio() == null ||
                servicio.getNombreServicio().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El nombre del servicio es obligatorio."
            );
        }

        if (servicio.getDuracionMinutos() <= 0) {
            throw new IllegalArgumentException(
                    "La duración debe ser mayor a cero."
            );
        }

        if (servicio.getPrecio() < 0) {
            throw new IllegalArgumentException(
                    "El precio no puede ser negativo."
            );
        }

        if (servicio.getCosto() < 0) {
            throw new IllegalArgumentException(
                    "El costo no puede ser negativo."
            );
        }
    }
}