package dao;

import claseslogicas.Disponibilidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadDAO {

    private final ConexionBD conexionBD = new ConexionBD();

    private Disponibilidad mapearDisponibilidad(ResultSet rs)
            throws SQLException {

        Disponibilidad disponibilidad = new Disponibilidad();

        disponibilidad.setIdDisponibilidad(
                rs.getInt("id_disponibilidad")
        );

        disponibilidad.setIdEmpleado(
                rs.getInt("id_empleado")
        );

        disponibilidad.setDiaSemana(
                rs.getString("dia_semana")
        );

        Time horaDesde = rs.getTime("hora_desde");
        if (horaDesde != null) {
            disponibilidad.setHoraDesde(horaDesde.toLocalTime());
        }

        Time horaHasta = rs.getTime("hora_hasta");
        if (horaHasta != null) {
            disponibilidad.setHoraHasta(horaHasta.toLocalTime());
        }

        disponibilidad.setActivo(
                rs.getBoolean("activo")
        );

        return disponibilidad;
    }

    // Registrar disponibilidad
    public void insertar(Disponibilidad disponibilidad)
            throws SQLException {

        String sql = "INSERT INTO disponibilidad " +
                "(id_empleado, dia_semana, hora_desde, hora_hasta, activo) " +
                "VALUES (?, ?, ?, ?, true)";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, disponibilidad.getIdEmpleado());
            ps.setString(2, disponibilidad.getDiaSemana());
            ps.setTime(3, Time.valueOf(disponibilidad.getHoraDesde()));
            ps.setTime(4, Time.valueOf(disponibilidad.getHoraHasta()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    disponibilidad.setIdDisponibilidad(
                            rs.getInt(1)
                    );
                }
            }
        }
    }

    // Consultar disponibilidad por ID
    public Disponibilidad obtenerPorId(int idDisponibilidad)
            throws SQLException {

        String sql = "SELECT id_disponibilidad, id_empleado, " +
                "dia_semana, hora_desde, hora_hasta, activo " +
                "FROM disponibilidad " +
                "WHERE id_disponibilidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDisponibilidad);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearDisponibilidad(rs);
                }
            }
        }

        return null;
    }

    // Consultar disponibilidades de un profesional
    public List<Disponibilidad> obtenerPorEmpleado(int idEmpleado)
            throws SQLException {

        String sql = "SELECT id_disponibilidad, id_empleado, " +
                "dia_semana, hora_desde, hora_hasta, activo " +
                "FROM disponibilidad " +
                "WHERE id_empleado = ? " +
                "ORDER BY " +
                "CASE dia_semana " +
                "WHEN 'Lunes' THEN 1 " +
                "WHEN 'Martes' THEN 2 " +
                "WHEN 'Miércoles' THEN 3 " +
                "WHEN 'Jueves' THEN 4 " +
                "WHEN 'Viernes' THEN 5 " +
                "WHEN 'Sábado' THEN 6 " +
                "WHEN 'Domingo' THEN 7 " +
                "ELSE 8 END, " +
                "hora_desde";

        List<Disponibilidad> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearDisponibilidad(rs));
                }
            }
        }

        return lista;
    }

    // Listar todas las disponibilidades
    public List<Disponibilidad> obtenerTodas()
            throws SQLException {

        String sql = "SELECT id_disponibilidad, id_empleado, " +
                "dia_semana, hora_desde, hora_hasta, activo " +
                "FROM disponibilidad " +
                "ORDER BY id_empleado, hora_desde";

        List<Disponibilidad> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearDisponibilidad(rs));
            }
        }

        return lista;
    }

    // Actualizar disponibilidad
    public void actualizar(Disponibilidad disponibilidad)
            throws SQLException {

        String sql = "UPDATE disponibilidad " +
                "SET dia_semana = ?, " +
                "hora_desde = ?, " +
                "hora_hasta = ? " +
                "WHERE id_disponibilidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, disponibilidad.getDiaSemana());
            ps.setTime(2, Time.valueOf(disponibilidad.getHoraDesde()));
            ps.setTime(3, Time.valueOf(disponibilidad.getHoraHasta()));
            ps.setInt(4, disponibilidad.getIdDisponibilidad());

            ps.executeUpdate();
        }
    }

    // Actualizar estado
    public void actualizarEstado(int idDisponibilidad, boolean activo)
            throws SQLException {

        String sql = "UPDATE disponibilidad " +
                "SET activo = ? " +
                "WHERE id_disponibilidad = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idDisponibilidad);

            ps.executeUpdate();
        }
    }
}