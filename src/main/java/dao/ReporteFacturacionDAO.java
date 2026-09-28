package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import claseslogicas.FacturaReporteDetalle;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporteFacturacionDAO {

    private static final Logger log = LoggerFactory.getLogger(ReporteFacturacionDAO.class);

    private static final int ID_ESTADO_FACTURADA = 2;
    private static final int ID_ESTADO_PAGADA = 4;


    public Map<LocalDate, BigDecimal> obtenerFacturacionPorDia(LocalDate inicio, LocalDate fin) {
        Map<LocalDate, BigDecimal> resultados = new LinkedHashMap<>();
        String sql = "SELECT DATE(fecha_hora) AS fecha, SUM(total) AS total_facturado " +
                "FROM factura " +
                "WHERE fecha_hora >= ? AND fecha_hora < ? " +
                "AND id_estado_factura IN (?, ?) " +
                "GROUP BY DATE(fecha_hora) " +
                "ORDER BY fecha";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, inicio);
            stmt.setObject(2, fin.plusDays(1)); // incluye el último día completo
            stmt.setInt(3, ID_ESTADO_FACTURADA);
            stmt.setInt(4, ID_ESTADO_PAGADA);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate fecha = rs.getDate("fecha").toLocalDate();
                    BigDecimal total = rs.getBigDecimal("total_facturado");
                    resultados.put(fecha, total != null ? total : BigDecimal.ZERO);
                }
            }

        } catch (SQLException e) {
            log.error("Error al obtener facturación entre {} y {}", inicio, fin, e);
        }

        return resultados;
    }

    public Map<String, Integer> obtenerUsoMetodosPago(LocalDate inicio, LocalDate fin) {
        Map<String, Integer> resultados = new LinkedHashMap<>();
        String sql = "SELECT mp.nombre_metodo, COUNT(f.id_factura) AS cantidad " +
                "FROM factura f " +
                "LEFT JOIN metodo_pago mp ON f.id_metodo = mp.id_metodo " +
                "WHERE f.fecha_hora >= ? AND f.fecha_hora < ? " +
                "AND f.id_estado_factura = ? " +
                "GROUP BY mp.nombre_metodo " +
                "ORDER BY cantidad DESC";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, inicio);
            stmt.setObject(2, fin.plusDays(1)); // incluye el día final
            stmt.setInt(3, ID_ESTADO_PAGADA);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String metodo = rs.getString("nombre_metodo");
                    int cantidad = rs.getInt("cantidad");
                    resultados.put(metodo != null ? metodo : "Sin especificar", cantidad);
                }
            }

        } catch (SQLException e) {
            log.error("Error al obtener uso de métodos de pago entre {} y {}", inicio, fin, e);
        }

        return resultados;
    }

    public Map<LocalDate, List<String>> obtenerMetodosPorFacturaPorDia(LocalDate inicio, LocalDate fin) {
        Map<LocalDate, List<String>> resultados = new LinkedHashMap<>();
        String sql = "SELECT DATE(f.fecha_hora) AS fecha, mp.nombre_metodo, f.id_estado_factura " +
                "FROM factura f " +
                "LEFT JOIN metodo_pago mp ON f.id_metodo = mp.id_metodo " +
                "WHERE f.fecha_hora >= ? AND f.fecha_hora < ? " +
                "AND f.id_estado_factura IN (?, ?) " +
                "ORDER BY f.fecha_hora";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, inicio);
            stmt.setObject(2, fin.plusDays(1));
            stmt.setInt(3, ID_ESTADO_FACTURADA);
            stmt.setInt(4, ID_ESTADO_PAGADA);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate fecha = rs.getDate("fecha").toLocalDate();
                    int idEstado = rs.getInt("id_estado_factura");

                    String etiqueta = (idEstado == ID_ESTADO_PAGADA) ? rs.getString("nombre_metodo") : "Facturada";
                    resultados.computeIfAbsent(fecha, k -> new ArrayList<>()).add(etiqueta);
                }
            }

        } catch (SQLException e) {
            log.error("Error al obtener métodos por factura entre {} y {}", inicio, fin, e);
        }

        return resultados;
    }
    public Map<LocalDate, Map<String, BigDecimal>> obtenerImportesPorMetodoPagoPorDia(
            LocalDate inicio, LocalDate fin) {

        Map<LocalDate, Map<String, BigDecimal>> resultados =
                new LinkedHashMap<>();

        String sql =
                "SELECT DATE(f.fecha_hora) AS fecha, " +
                        "       mp.nombre_metodo AS metodo, " +
                        "       COALESCE(SUM(f.total), 0) AS importe " +
                        "FROM factura f " +
                        "INNER JOIN metodo_pago mp ON f.id_metodo = mp.id_metodo " +
                        "WHERE f.fecha_hora >= ? " +
                        "AND f.fecha_hora < ? " +
                        "AND f.id_estado_factura = ? " +
                        "GROUP BY DATE(f.fecha_hora), mp.nombre_metodo " +
                        "ORDER BY DATE(f.fecha_hora), mp.nombre_metodo";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, inicio);
            stmt.setObject(2, fin.plusDays(1));
            stmt.setInt(3, ID_ESTADO_PAGADA);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    LocalDate fecha =
                            rs.getDate("fecha").toLocalDate();

                    String metodo =
                            rs.getString("metodo");

                    BigDecimal importe =
                            rs.getBigDecimal("importe");

                    if (metodo == null) {
                        metodo = "Sin especificar";
                    }

                    if (importe == null) {
                        importe = BigDecimal.ZERO;
                    }

                    resultados
                            .computeIfAbsent(
                                    fecha,
                                    k -> new LinkedHashMap<>()
                            )
                            .put(metodo, importe);
                }
            }

        } catch (SQLException e) {
            log.error(
                    "Error al obtener importes por método de pago entre {} y {}",
                    inicio,
                    fin,
                    e
            );
        }

        return resultados;
    }

    /**
     * Obtiene el detalle de facturación del período, incluyendo cliente, fecha,
     * monto y forma de pago. Se consideran las facturas Facturada y Pagada,
     * igual que los demás cálculos de este reporte.
     */
    public List<FacturaReporteDetalle> obtenerDetalleFacturacion(LocalDate inicio, LocalDate fin) {
        List<FacturaReporteDetalle> resultados = new ArrayList<>();
        String sql = "SELECT CONCAT(p.nombre, ' ', p.apellido) AS cliente, " +
                "f.fecha_hora, f.total, COALESCE(mp.nombre_metodo, 'Sin especificar') AS forma_pago, " +
                "ef.nombre AS estado_factura " +
                "FROM factura f " +
                "JOIN cliente c ON f.id_cliente = c.id_cliente " +
                "JOIN persona p ON c.id_persona = p.id_persona " +
                "LEFT JOIN metodo_pago mp ON f.id_metodo = mp.id_metodo " +
                "JOIN estado_factura ef ON f.id_estado_factura = ef.id_estado_factura " +
                "WHERE f.fecha_hora >= ? AND f.fecha_hora < ? " +
                "AND f.id_estado_factura IN (?, ?) " +
                "ORDER BY f.fecha_hora, cliente";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(fin.plusDays(1).atStartOfDay()));
            stmt.setInt(3, ID_ESTADO_FACTURADA);
            stmt.setInt(4, ID_ESTADO_PAGADA);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp timestamp = rs.getTimestamp("fecha_hora");
                    resultados.add(new FacturaReporteDetalle(
                            rs.getString("cliente"),
                            timestamp != null ? timestamp.toLocalDateTime() : null,
                            rs.getBigDecimal("total"),
                            rs.getString("forma_pago"),
                            rs.getString("estado_factura")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Error al obtener detalle de facturación entre {} y {}", inicio, fin, e);
        }
        return resultados;
    }

}