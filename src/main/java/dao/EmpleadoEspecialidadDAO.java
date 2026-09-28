package dao;

import claseslogicas.Especialidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoEspecialidadDAO {

    private final ConexionBD conexionBD = new ConexionBD();

    // Asignar una especialidad a un profesional
    public void asignarEspecialidad(int idEmpleado, int idEspecialidad)
            throws SQLException {

        String sql = "INSERT INTO empleado_especialidad " +
                "(id_empleado, id_especialidad) " +
                "VALUES (?, ?)";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            ps.setInt(2, idEspecialidad);

            ps.executeUpdate();
        }
    }

    // Consultar las especialidades de un profesional
    public List<Especialidad> obtenerEspecialidadesPorEmpleado(
            int idEmpleado) throws SQLException {

        String sql = "SELECT e.id_especialidad, e.nombre, e.activo " +
                "FROM especialidad e " +
                "JOIN empleado_especialidad ee " +
                "ON e.id_especialidad = ee.id_especialidad " +
                "WHERE ee.id_empleado = ? " +
                "ORDER BY e.nombre";

        List<Especialidad> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Especialidad especialidad = new Especialidad();

                    especialidad.setIdEspecialidad(
                            rs.getInt("id_especialidad")
                    );

                    especialidad.setNombre(
                            rs.getString("nombre")
                    );

                    especialidad.setActivo(
                            rs.getBoolean("activo")
                    );

                    lista.add(especialidad);
                }
            }
        }

        return lista;
    }

    // Quitar una especialidad de un profesional
    public void quitarEspecialidad(int idEmpleado, int idEspecialidad)
            throws SQLException {

        String sql = "DELETE FROM empleado_especialidad " +
                "WHERE id_empleado = ? " +
                "AND id_especialidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            ps.setInt(2, idEspecialidad);

            ps.executeUpdate();
        }
    }

    // Consultar los profesionales que tienen una especialidad
    public List<Integer> obtenerEmpleadosPorEspecialidad(
            int idEspecialidad) throws SQLException {

        String sql = "SELECT id_empleado " +
                "FROM empleado_especialidad " +
                "WHERE id_especialidad = ?";

        List<Integer> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(rs.getInt("id_empleado"));
                }
            }
        }

        return lista;
    }
}