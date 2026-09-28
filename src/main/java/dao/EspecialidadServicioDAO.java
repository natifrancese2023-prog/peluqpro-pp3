
package dao;

import claseslogicas.Servicio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadServicioDAO {

    private final ConexionBD conexionBD = new ConexionBD();


    // ==========================================================
    // OBTENER SERVICIOS ASOCIADOS A UNA ESPECIALIDAD
    // ==========================================================

    public List<Servicio> obtenerServiciosPorEspecialidad(
            int idEspecialidad) throws SQLException {

        List<Servicio> servicios = new ArrayList<>();

        String sql =
                "SELECT s.id_servicio, " +
                        "s.nombre_servicio, " +
                        "s.descripcion, " +
                        "s.duracion_minutos, " +
                        "s.precio, " +
                        "s.costo, " +
                        "s.activo " +
                        "FROM servicios s " +
                        "INNER JOIN especialidad_servicio es " +
                        "ON s.id_servicio = es.id_servicio " +
                        "WHERE es.id_especialidad = ? " +
                        "ORDER BY s.nombre_servicio";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Servicio servicio = new Servicio(
                            rs.getInt("id_servicio"),
                            rs.getString("nombre_servicio"),
                            rs.getString("descripcion"),
                            rs.getInt("duracion_minutos"),
                            rs.getDouble("precio"),
                            rs.getDouble("costo"),
                            rs.getBoolean("activo")
                    );

                    servicios.add(servicio);
                }
            }
        }

        return servicios;
    }


    // ==========================================================
    // OBTENER SERVICIOS QUE TODAVÍA NO ESTÁN ASOCIADOS
    // ==========================================================

    public List<Servicio> obtenerServiciosNoAsociados(
            int idEspecialidad) throws SQLException {

        List<Servicio> servicios = new ArrayList<>();

        String sql =
                "SELECT s.id_servicio, " +
                        "s.nombre_servicio, " +
                        "s.descripcion, " +
                        "s.duracion_minutos, " +
                        "s.precio, " +
                        "s.costo, " +
                        "s.activo " +
                        "FROM servicios s " +
                        "WHERE s.activo = true " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM especialidad_servicio es " +
                        "    WHERE es.id_especialidad = ? " +
                        "    AND es.id_servicio = s.id_servicio " +
                        ") " +
                        "ORDER BY s.nombre_servicio";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Servicio servicio = new Servicio(
                            rs.getInt("id_servicio"),
                            rs.getString("nombre_servicio"),
                            rs.getString("descripcion"),
                            rs.getInt("duracion_minutos"),
                            rs.getDouble("precio"),
                            rs.getDouble("costo"),
                            rs.getBoolean("activo")
                    );

                    servicios.add(servicio);
                }
            }
        }

        return servicios;
    }


    // ==========================================================
    // ASOCIAR SERVICIO
    // ==========================================================

    public void asociarServicio(
            int idEspecialidad,
            int idServicio) throws SQLException {

        String sql =
                "INSERT INTO especialidad_servicio " +
                        "(id_especialidad, id_servicio) " +
                        "VALUES (?, ?)";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);
            ps.setInt(2, idServicio);

            ps.executeUpdate();
        }
    }


    // ==========================================================
    // DESASOCIAR SERVICIO
    // ==========================================================

    public void desasociarServicio(
            int idEspecialidad,
            int idServicio) throws SQLException {

        String sql =
                "DELETE FROM especialidad_servicio " +
                        "WHERE id_especialidad = ? " +
                        "AND id_servicio = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);
            ps.setInt(2, idServicio);

            ps.executeUpdate();
        }
    }


    // ==========================================================
    // VERIFICAR SI YA EXISTE LA ASOCIACIÓN
    // ==========================================================

    public boolean existeAsociacion(
            int idEspecialidad,
            int idServicio) throws SQLException {

        String sql =
                "SELECT 1 " +
                        "FROM especialidad_servicio " +
                        "WHERE id_especialidad = ? " +
                        "AND id_servicio = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspecialidad);
            ps.setInt(2, idServicio);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}