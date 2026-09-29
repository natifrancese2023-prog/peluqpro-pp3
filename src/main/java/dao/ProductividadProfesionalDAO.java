package dao;

import claseslogicas.ProductividadProfesional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductividadProfesionalDAO {

    private static final String SQL_PRODUCTIVIDAD =
            "SELECT e.id_empleado, " +
            "       p.nombre || ' ' || p.apellido AS profesional, " +
            "       COUNT(ds.id_servicio) AS cantidad_servicios, " +
            "       AVG(s.duracion_minutos) AS promedio_duracion " +
            "FROM empleado e " +
            "JOIN persona p ON p.id_persona = e.id_persona " +
            "JOIN roles r ON r.id_rol = e.id_rol " +
            "LEFT JOIN visita v ON v.id_estilista = e.id_empleado " +
            "    AND v.fecha_hora >= ? " +
            "    AND v.fecha_hora < ? " +
            "LEFT JOIN detalle_servicio ds ON ds.id_visita = v.id_visita " +
            "LEFT JOIN servicios s ON s.id_servicio = ds.id_servicio " +
            "WHERE r.es_estilista = true " +
            "GROUP BY e.id_empleado, p.nombre, p.apellido " +
            "ORDER BY p.apellido, p.nombre";

    public List<ProductividadProfesional> obtenerPorPeriodo(LocalDate desde, LocalDate hasta)
            throws SQLException {

        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("El período es obligatorio.");
        }

        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha Hasta no puede ser anterior a Desde.");
        }

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime finExclusivo = hasta.plusDays(1).atStartOfDay();

        List<ProductividadProfesional> resultados = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_PRODUCTIVIDAD)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(finExclusivo));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.math.BigDecimal promedioDecimal = rs.getBigDecimal("promedio_duracion");
                    Double promedio = promedioDecimal != null
                            ? promedioDecimal.doubleValue()
                            : null;

                    resultados.add(new ProductividadProfesional(
                            rs.getInt("id_empleado"),
                            rs.getString("profesional"),
                            rs.getLong("cantidad_servicios"),
                            promedio
                    ));
                }
            }
        }

        return resultados;
    }
}
