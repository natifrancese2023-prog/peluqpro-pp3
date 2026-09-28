package controllers;

import claseslogicas.Servicio;
import service.ServicioService;

import java.sql.SQLException;
import java.util.List;

public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController() {
        this.servicioService = new ServicioService();
    }


    // ==========================================================
    // LISTADO
    // ==========================================================

    public List<Servicio> listarServicios() {
        return servicioService.listarServicios();
    }


    // ==========================================================
    // CONSULTA
    // ==========================================================

    public Servicio obtenerServicio(int idServicio) throws SQLException {
        return servicioService.obtenerServicio(idServicio);
    }


    // ==========================================================
    // ALTA
    // ==========================================================

    public void registrarServicio(
            String nombre,
            String descripcion,
            int duracionMinutos,
            double precio,
            double costo) throws SQLException {

        Servicio servicio = new Servicio();

        servicio.setNombreServicio(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setDuracionMinutos(duracionMinutos);
        servicio.setPrecio(precio);
        servicio.setCosto(costo);

        // Todo servicio nuevo comienza activo.
        servicio.setActivo(true);

        servicioService.registrarServicio(servicio);
    }


    // ==========================================================
    // MODIFICACIÓN
    // ==========================================================

    public void modificarServicio(
            int idServicio,
            String nombre,
            String descripcion,
            int duracionMinutos,
            double precio,
            double costo) throws SQLException {

        Servicio servicio = servicioService.obtenerServicio(idServicio);

        if (servicio == null) {
            throw new IllegalArgumentException(
                    "No se encontró el servicio seleccionado."
            );
        }

        servicio.setNombreServicio(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setDuracionMinutos(duracionMinutos);
        servicio.setPrecio(precio);
        servicio.setCosto(costo);

        servicioService.actualizarServicio(servicio);
    }


    // ==========================================================
    // ACTIVAR / INACTIVAR
    // ==========================================================

    public void activarServicio(int idServicio) throws SQLException {
        servicioService.actualizarEstado(idServicio, true);
    }


    public void inactivarServicio(int idServicio) throws SQLException {
        servicioService.actualizarEstado(idServicio, false);
    }


    public void cambiarEstado(int idServicio, boolean nuevoEstado)
            throws SQLException {

        servicioService.actualizarEstado(idServicio, nuevoEstado);
    }


    // ==========================================================
    // CONSULTAS ESPECÍFICAS
    // ==========================================================

    public List<Servicio> listarServiciosPorTurno(int idTurno) {
        return servicioService.listarServiciosPorTurno(idTurno);
    }
}