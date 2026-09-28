package service;

import claseslogicas.Empleado;
import dao.EmpleadoDAO;

import java.sql.SQLException;
import java.util.List;

public class EmpleadoService {

    private final EmpleadoDAO empleadoDAO;

    public EmpleadoService() {
        this.empleadoDAO = new EmpleadoDAO();
    }

    // Registrar profesional
    public boolean registrarProfesional(
            Empleado empleado,
            String tipoDocumento,
            String numeroDocumento,
            String nombreBarrio) throws SQLException {

        if (empleado == null) {
            return false;
        }

        return empleadoDAO.insertar(
                empleado,
                tipoDocumento,
                numeroDocumento,
                nombreBarrio
        );
    }

    // Consultar profesional
    public Empleado obtenerPorId(int idEmpleado)
            throws SQLException {

        return empleadoDAO.obtenerPorId(idEmpleado);
    }

    // Listar profesionales
    public List<Empleado> obtenerProfesionales()
            throws SQLException {

        return empleadoDAO.obtenerEstilistas();
    }

    // Actualizar profesional
    public boolean actualizarProfesional(
            Empleado empleado,
            String nombreBarrio) throws SQLException {

        if (empleado == null || empleado.getIdEmpleado() <= 0) {
            return false;
        }

        return empleadoDAO.actualizar(
                empleado,
                nombreBarrio
        );
    }

    // Actualizar estado del profesional
    public boolean actualizarEstado(
            int idEmpleado,
            boolean activo) throws SQLException {

        if (idEmpleado <= 0) {
            return false;
        }

        return empleadoDAO.actualizarEstado(
                idEmpleado,
                activo
        );
    }
}