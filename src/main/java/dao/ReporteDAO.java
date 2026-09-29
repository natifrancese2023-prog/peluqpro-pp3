package dao;

import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ClienteRiesgo;
import claseslogicas.EstadoFactura;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporteDAO {

    private static final Logger log = LoggerFactory.getLogger(ReporteDAO.class);

    public List<ClienteReporteExtendido> obtenerDatosClientesExtendido() throws SQLException {
        List<ClienteReporteExtendido> lista = new ArrayList<>();

        String sql = """
            SELECT
                p.id_persona,
                c.id_cliente,
                CONCAT(p.nombre, ' ', p.apellido) AS nombre_completo,
                p.telefono,
                p.email,
                CONCAT(p.calle, ' ', p.numero) AS direccion,
                c.fecha_alta,
                COALESCE(v.cantidad_visitas, 0) AS cantidad_visitas,
                COALESCE(f.gasto_total, 0) AS gasto_total,
                ut.nombre_estado AS estado_ultimo_turno,
                rs.red_social
            FROM persona p
            JOIN cliente c ON p.id_persona = c.id_persona
            LEFT JOIN (
                SELECT id_cliente, COUNT(*) AS cantidad_visitas
                FROM visita
                GROUP BY id_cliente
            ) v ON v.id_cliente = c.id_cliente
            LEFT JOIN (
                SELECT id_cliente, SUM(total) AS gasto_total
                FROM factura
                WHERE id_estado_factura <> ?
                GROUP BY id_cliente
            ) f ON f.id_cliente = c.id_cliente
            LEFT JOIN (
                SELECT id_cliente, STRING_AGG(nombre_usuario, ', ') AS red_social
                FROM red_social
                GROUP BY id_cliente
            ) rs ON rs.id_cliente = c.id_cliente
            LEFT JOIN LATERAL (
                SELECT et.nombre_estado
                FROM turno t
                JOIN estado et ON et.id_estado = t.id_estado
                WHERE t.id_cliente = c.id_cliente
                ORDER BY t.fecha DESC, t.hora_inicio DESC
                LIMIT 1
            ) ut ON true
            ORDER BY gasto_total DESC
        """;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, EstadoFactura.ANULADA.getIdEstadoFactura());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("fecha_alta");
                    LocalDate fechaAlta = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    ClienteReporteExtendido cliente = new ClienteReporteExtendido(
                            rs.getInt("id_persona"),
                            rs.getInt("id_cliente"),
                            rs.getString("nombre_completo"),
                            rs.getString("telefono"),
                            rs.getString("email"),
                            rs.getString("direccion"),
                            fechaAlta,
                            rs.getInt("cantidad_visitas"),
                            rs.getBigDecimal("gasto_total"),
                            rs.getString("estado_ultimo_turno"),
                            rs.getString("red_social")
                    );

                    lista.add(cliente);
                }
            }

        } catch (SQLException e) {
            log.error("Error al obtener datos extendidos de clientes", e);
            throw e;
        }

        return lista;
    }
    /**
     * Obtiene el ticket promedio de un cliente para el período indicado.
     * Las facturas anuladas no forman parte del cálculo.
     * Devuelve null cuando no existen facturas en el período.
     */
    public java.math.BigDecimal obtenerTicketPromedioCliente(int idCliente, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT SUM(total) AS total, COUNT(*) AS cantidad " +
                "FROM factura " +
                "WHERE id_cliente = ? " +
                "AND fecha_hora >= ? " +
                "AND fecha_hora < ? " +
                "AND id_estado_factura IN (?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setTimestamp(2, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            ps.setInt(4, EstadoFactura.FACTURADA.getIdEstadoFactura());
            ps.setInt(5, EstadoFactura.PAGADA.getIdEstadoFactura());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cantidad = rs.getInt("cantidad");
                    if (cantidad == 0) {
                        return null;
                    }
                    java.math.BigDecimal total = rs.getBigDecimal("total");
                    return total.divide(java.math.BigDecimal.valueOf(cantidad), 2, java.math.RoundingMode.HALF_UP);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene los clientes activos cuya última visita fue hace más de tres meses.
     * La regla de negocio RN1 se aplica sobre la fecha de la última visita registrada.
     */
    public List<ClienteRiesgo> obtenerClientesEnRiesgo() throws SQLException {
        List<ClienteRiesgo> lista = new ArrayList<>();

        String sql = """
            SELECT
                c.id_cliente,
                CONCAT(p.nombre, ' ', p.apellido) AS nombre_completo,
                p.telefono,
                p.email,
                MAX(v.fecha_hora) AS ultima_visita
            FROM cliente c
            JOIN persona p ON p.id_persona = c.id_persona
            JOIN visita v ON v.id_cliente = c.id_cliente
            WHERE c.activo = TRUE
            GROUP BY c.id_cliente, p.nombre, p.apellido, p.telefono, p.email
            HAVING MAX(v.fecha_hora) < CURRENT_TIMESTAMP - INTERVAL '3 months'
            ORDER BY ultima_visita ASC
        """;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("ultima_visita");
                lista.add(new ClienteRiesgo(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_completo"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        timestamp != null ? timestamp.toLocalDateTime() : null
                ));
            }
        }
        return lista;
    }

    /**
     * Calcula el ticket promedio general del período seleccionado.
     * Se consideran únicamente facturas facturadas o pagadas; las anuladas quedan fuera.
     */
    public java.math.BigDecimal obtenerTicketPromedio(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
            SELECT AVG(total) AS ticket_promedio
            FROM factura
            WHERE fecha_hora >= ?
              AND fecha_hora < ?
              AND id_estado_factura IN (?, ?)
        """;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            ps.setInt(3, EstadoFactura.FACTURADA.getIdEstadoFactura());
            ps.setInt(4, EstadoFactura.PAGADA.getIdEstadoFactura());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal promedio = rs.getBigDecimal("ticket_promedio");
                    return promedio == null ? null : promedio.setScale(2, java.math.RoundingMode.HALF_UP);
                }
            }
        }
        return null;
    }

}
