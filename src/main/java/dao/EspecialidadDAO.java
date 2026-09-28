package dao;

import claseslogicas.Especialidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadDAO {

    private final ConexionBD conexionBD = new ConexionBD();

    private Especialidad mapearEspecialidad(ResultSet rs) throws SQLException {
        Especialidad especialidad = new Especialidad();

        especialidad.setIdEspecialidad(rs.getInt("id_especialidad"));
        especialidad.setNombre(rs.getString("nombre"));
        especialidad.setActivo(rs.getBoolean("activo"));

        return especialidad;
    }

    // Registrar especialidad
    public void insertar(Especialidad especialidad) throws SQLException {

        String sql = "INSERT INTO especialidad (nombre, activo) " +
                "VALUES (?, true)";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, especialidad.getNombre());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    especialidad.setIdEspecialidad(
                            rs.getInt(1)
                    );
                }
            }
        }
    }

    // Consultar especialidad por ID
    public Especialidad obtenerPorId(int idEspecialidad) throws SQLException {

        String sql = "SELECT id_especialidad, nombre, activo " +
                "FROM especialidad " +
                "WHERE id_especialidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearEspecialidad(rs);
                }
            }
        }

        return null;
    }

    // Listar todas las especialidades
    public List<Especialidad> obtenerTodas() throws SQLException {

        String sql = "SELECT id_especialidad, nombre, activo " +
                "FROM especialidad " +
                "ORDER BY nombre";

        List<Especialidad> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEspecialidad(rs));
            }
        }

        return lista;
    }

    // Actualizar especialidad
    public void actualizar(Especialidad especialidad) throws SQLException {

        String sql = "UPDATE especialidad " +
                "SET nombre = ? " +
                "WHERE id_especialidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, especialidad.getNombre());
            ps.setInt(2, especialidad.getIdEspecialidad());

            ps.executeUpdate();
        }
    }

    // Actualizar estado de la especialidad
    public void actualizarEstado(int idEspecialidad, boolean activo)
            throws SQLException {

        String sql = "UPDATE especialidad " +
                "SET activo = ? " +
                "WHERE id_especialidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idEspecialidad);

            ps.executeUpdate();
        }
    }
}