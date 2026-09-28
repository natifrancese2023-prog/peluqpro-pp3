package dao;

import claseslogicas.Servicio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServicioDAO {

    private static final Logger log = LoggerFactory.getLogger(ServicioDAO.class);

    public List<Servicio> obtenerTodos() {
        List<Servicio> servicios = new ArrayList<>();

        String sql = "SELECT id_servicio, nombre_servicio, descripcion, " +
                "duracion_minutos, precio, costo, activo " +
                "FROM servicios";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                servicios.add(mapearServicio(rs));
            }

        } catch (SQLException e) {
            log.error("Error al obtener todos los servicios", e);
        }

        return servicios;
    }


    public List<Servicio> obtenerServiciosPorTurno(int idTurno) {

        List<Servicio> servicios = new ArrayList<>();

        String sql = "SELECT s.id_servicio, s.nombre_servicio, " +
                "s.descripcion, s.duracion_minutos, s.precio, " +
                "s.costo, s.activo " +
                "FROM servicios s " +
                "JOIN turno_servicios ts ON s.id_servicio = ts.id_servicio " +
                "WHERE ts.id_turno = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTurno);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    servicios.add(mapearServicio(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Error al obtener servicios por turno id={}", idTurno, e);
        }

        return servicios;
    }


    private Servicio mapearServicio(ResultSet rs) throws SQLException {

        return new Servicio(
                rs.getInt("id_servicio"),
                rs.getString("nombre_servicio"),
                rs.getString("descripcion"),
                rs.getInt("duracion_minutos"),
                rs.getDouble("precio"),
                rs.getDouble("costo"),
                rs.getBoolean("activo")
        );
    }


    public List<Servicio> obtenerServiciosPorEmpleado(int idEmpleado)
            throws SQLException {

        List<Servicio> servicios = new ArrayList<>();

        String sql =
                "SELECT DISTINCT " +
                        "s.id_servicio, " +
                        "s.nombre_servicio, " +
                        "s.descripcion, " +
                        "s.duracion_minutos, " +
                        "s.precio, " +
                        "s.costo, " +
                        "s.activo " +
                        "FROM servicios s " +
                        "JOIN especialidad_servicio es " +
                        "ON s.id_servicio = es.id_servicio " +
                        "JOIN empleado_especialidad ee " +
                        "ON es.id_especialidad = ee.id_especialidad " +
                        "JOIN especialidad e " +
                        "ON ee.id_especialidad = e.id_especialidad " +
                        "WHERE ee.id_empleado = ? " +
                        "AND e.activo = true " +
                        "ORDER BY s.nombre_servicio";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    servicios.add(mapearServicio(rs));
                }
            }

        } catch (SQLException e) {

            log.error(
                    "Error al obtener servicios del profesional id={}",
                    idEmpleado,
                    e
            );

            throw e;
        }

        return servicios;
    }


    // ==========================================================
    // ABM DE SERVICIOS
    // ==========================================================

    public void insertar(Servicio servicio) throws SQLException {

        String sql =
                "INSERT INTO servicios " +
                        "(nombre_servicio, descripcion, duracion_minutos, precio, costo, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, servicio.getNombreServicio());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getDuracionMinutos());
            ps.setDouble(4, servicio.getPrecio());
            ps.setDouble(5, servicio.getCosto());
            ps.setBoolean(6, servicio.isActivo());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    servicio.setIdServicio(rs.getInt(1));
                }
            }
        }
    }


    public void actualizar(Servicio servicio) throws SQLException {

        String sql =
                "UPDATE servicios SET " +
                        "nombre_servicio = ?, " +
                        "descripcion = ?, " +
                        "duracion_minutos = ?, " +
                        "precio = ?, " +
                        "costo = ? " +
                        "WHERE id_servicio = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, servicio.getNombreServicio());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getDuracionMinutos());
            ps.setDouble(4, servicio.getPrecio());
            ps.setDouble(5, servicio.getCosto());
            ps.setInt(6, servicio.getIdServicio());

            ps.executeUpdate();
        }
    }


    public void actualizarEstado(int idServicio, boolean activo)
            throws SQLException {

        String sql =
                "UPDATE servicios " +
                        "SET activo = ? " +
                        "WHERE id_servicio = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idServicio);

            ps.executeUpdate();
        }
    }


    public Servicio obtenerPorId(int idServicio) throws SQLException {

        String sql =
                "SELECT id_servicio, nombre_servicio, descripcion, " +
                        "duracion_minutos, precio, costo, activo " +
                        "FROM servicios " +
                        "WHERE id_servicio = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idServicio);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearServicio(rs);
                }
            }
        }

        return null;
    }
}