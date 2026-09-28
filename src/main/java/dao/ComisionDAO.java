package dao;

import claseslogicas.ComisionProfesional;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ComisionDAO {

    public List<ComisionProfesional> obtenerComisiones(
            int idEmpleado,
            LocalDate desde,
            LocalDate hasta) throws SQLException {

        List<ComisionProfesional> comisiones = new ArrayList<>();

        String sql =
                "SELECT " +
                        "    v.fecha_hora::date AS fecha, " +
                        "    s.nombre_servicio, " +
                        "    df.precio_unitario, " +
                        "    e.porcentaje_comision " +
                        "FROM visita v " +
                        "JOIN empleado e " +
                        "    ON v.id_estilista = e.id_empleado " +
                        "JOIN factura f " +
                        "    ON f.id_turno = v.id_turno " +
                        "JOIN detalle_factura df " +
                        "    ON df.id_factura = f.id_factura " +
                        "JOIN servicios s " +
                        "    ON s.id_servicio = df.id_servicio " +
                        "WHERE v.id_estilista = ? " +
                        "AND v.fecha_hora::date BETWEEN ? AND ? " +
                        "ORDER BY v.fecha_hora, f.id_factura, df.id_detalle";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            ps.setObject(2, desde);
            ps.setObject(3, hasta);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    LocalDate fecha =
                            rs.getDate("fecha").toLocalDate();

                    String nombreServicio =
                            rs.getString("nombre_servicio");

                    BigDecimal precio =
                            rs.getBigDecimal("precio_unitario");

                    double porcentaje =
                            rs.getDouble("porcentaje_comision");

                    BigDecimal comision =
                            precio
                                    .multiply(BigDecimal.valueOf(porcentaje))
                                    .divide(
                                            BigDecimal.valueOf(100),
                                            2,
                                            java.math.RoundingMode.HALF_UP
                                    );

                    comisiones.add(
                            new ComisionProfesional(
                                    fecha,
                                    nombreServicio,
                                    precio,
                                    porcentaje,
                                    comision
                            )
                    );
                }
            }
        }

        return comisiones;
    }
}