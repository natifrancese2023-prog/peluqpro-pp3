package dao;

import claseslogicas.DetalleRankingServicio;
import claseslogicas.RankingServicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RankingServicioDAO {

    private static final String SQL_BASE =
            "SELECT s.id_servicio, s.nombre_servicio, s.costo AS costo_estimado, " +
            "       df.precio_unitario, df.cantidad, " +
            "       e.id_empleado, e.porcentaje_comision, " +
            "       COALESCE(p.nombre, '') AS nombre_profesional, " +
            "       COALESCE(p.apellido, '') AS apellido_profesional, " +
            "       v.fecha_hora " +
            "FROM visita v " +
            "JOIN empleado e ON v.id_estilista = e.id_empleado " +
            "JOIN persona p ON e.id_persona = p.id_persona " +
            "JOIN factura f ON f.id_turno = v.id_turno " +
            "JOIN detalle_factura df ON df.id_factura = f.id_factura " +
            "JOIN servicios s ON s.id_servicio = df.id_servicio " +
            "WHERE v.fecha_hora::date BETWEEN ? AND ? ";

    public List<RankingServicio> obtenerRanking(LocalDate desde, LocalDate hasta)
            throws SQLException {

        Map<Integer, AcumuladoServicio> acumulados = new LinkedHashMap<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SQL_BASE +
                     "ORDER BY s.nombre_servicio, v.fecha_hora, f.id_factura, df.id_detalle")) {

            ps.setObject(1, desde);
            ps.setObject(2, hasta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idServicio = rs.getInt("id_servicio");
                    String nombreServicio = rs.getString("nombre_servicio");
                    BigDecimal costoUnitario = ceroSiNull(rs.getBigDecimal("costo_estimado"));
                    BigDecimal precioUnitario = ceroSiNull(rs.getBigDecimal("precio_unitario"));
                    int cantidad = rs.getInt("cantidad");
                    if (cantidad <= 0) {
                        cantidad = 1;
                    }

                    BigDecimal porcentaje = ceroSiNull(rs.getBigDecimal("porcentaje_comision"));
                    BigDecimal ingresos = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
                    BigDecimal costo = costoUnitario.multiply(BigDecimal.valueOf(cantidad));
                    BigDecimal comision = calcularComision(ingresos, porcentaje);
                    BigDecimal margen = ingresos.subtract(costo).subtract(comision);

                    AcumuladoServicio acumulado = acumulados.computeIfAbsent(
                            idServicio,
                            id -> new AcumuladoServicio(id, nombreServicio));

                    acumulado.agregar(cantidad, ingresos, costo, comision, margen);
                }
            }
        }

        List<AcumuladoServicio> ordenados = new ArrayList<>(acumulados.values());
        ordenados.sort((a, b) -> b.margenEstimado.compareTo(a.margenEstimado));

        List<RankingServicio> resultado = new ArrayList<>();
        int posicion = 1;

        for (AcumuladoServicio a : ordenados) {
            resultado.add(new RankingServicio(
                    a.idServicio,
                    posicion++,
                    a.nombreServicio,
                    a.cantidadRealizada,
                    a.ingresos,
                    a.costoEstimado,
                    a.comisiones,
                    a.margenEstimado
            ));
        }

        return resultado;
    }

    public List<DetalleRankingServicio> obtenerDetalleServicio(
            int idServicio, LocalDate desde, LocalDate hasta) throws SQLException {

        Map<Integer, AcumuladoProfesional> acumulados = new LinkedHashMap<>();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SQL_BASE +
                     "AND s.id_servicio = ? " +
                     "ORDER BY p.apellido, p.nombre, v.fecha_hora, f.id_factura, df.id_detalle")) {

            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            ps.setInt(3, idServicio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idEmpleado = rs.getInt("id_empleado");
                    String nombre = (rs.getString("nombre_profesional") + " " +
                            rs.getString("apellido_profesional")).trim();
                    if (nombre.isBlank()) {
                        nombre = "Profesional sin nombre";
                    }
                    final String nombreProfesional = nombre;

                    BigDecimal costoUnitario = ceroSiNull(rs.getBigDecimal("costo_estimado"));
                    BigDecimal precioUnitario = ceroSiNull(rs.getBigDecimal("precio_unitario"));
                    int cantidad = rs.getInt("cantidad");
                    if (cantidad <= 0) {
                        cantidad = 1;
                    }

                    BigDecimal porcentaje = ceroSiNull(rs.getBigDecimal("porcentaje_comision"));
                    BigDecimal ingresos = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
                    BigDecimal costo = costoUnitario.multiply(BigDecimal.valueOf(cantidad));
                    BigDecimal comision = calcularComision(ingresos, porcentaje);
                    BigDecimal margen = ingresos.subtract(costo).subtract(comision);

                    AcumuladoProfesional acumulado = acumulados.computeIfAbsent(
                            idEmpleado,
                            id -> new AcumuladoProfesional(nombreProfesional, porcentaje));

                    acumulado.agregar(cantidad, ingresos, costo, comision, margen);
                }
            }
        }

        List<DetalleRankingServicio> resultado = new ArrayList<>();
        for (AcumuladoProfesional a : acumulados.values()) {
            resultado.add(new DetalleRankingServicio(
                    a.profesional,
                    a.cantidad,
                    a.porcentajeComision,
                    a.ingresos,
                    a.costo,
                    a.comision,
                    a.margenEstimado
            ));
        }

        return resultado;
    }

    private BigDecimal calcularComision(BigDecimal ingresos, BigDecimal porcentaje) {
        return ingresos.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal ceroSiNull(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private static class AcumuladoServicio {
        private final int idServicio;
        private final String nombreServicio;
        private int cantidadRealizada;
        private BigDecimal ingresos = BigDecimal.ZERO;
        private BigDecimal costoEstimado = BigDecimal.ZERO;
        private BigDecimal comisiones = BigDecimal.ZERO;
        private BigDecimal margenEstimado = BigDecimal.ZERO;

        private AcumuladoServicio(int idServicio, String nombreServicio) {
            this.idServicio = idServicio;
            this.nombreServicio = nombreServicio;
        }

        private void agregar(int cantidad, BigDecimal ingresos,
                             BigDecimal costo, BigDecimal comision,
                             BigDecimal margen) {
            this.cantidadRealizada += cantidad;
            this.ingresos = this.ingresos.add(ingresos);
            this.costoEstimado = this.costoEstimado.add(costo);
            this.comisiones = this.comisiones.add(comision);
            this.margenEstimado = this.margenEstimado.add(margen);
        }
    }

    private static class AcumuladoProfesional {
        private final String profesional;
        private final BigDecimal porcentajeComision;
        private int cantidad;
        private BigDecimal ingresos = BigDecimal.ZERO;
        private BigDecimal costo = BigDecimal.ZERO;
        private BigDecimal comision = BigDecimal.ZERO;
        private BigDecimal margenEstimado = BigDecimal.ZERO;

        private AcumuladoProfesional(String profesional, BigDecimal porcentajeComision) {
            this.profesional = profesional;
            this.porcentajeComision = porcentajeComision;
        }

        private void agregar(int cantidad, BigDecimal ingresos,
                             BigDecimal costo, BigDecimal comision,
                             BigDecimal margen) {
            this.cantidad += cantidad;
            this.ingresos = this.ingresos.add(ingresos);
            this.costo = this.costo.add(costo);
            this.comision = this.comision.add(comision);
            this.margenEstimado = this.margenEstimado.add(margen);
        }
    }
}
