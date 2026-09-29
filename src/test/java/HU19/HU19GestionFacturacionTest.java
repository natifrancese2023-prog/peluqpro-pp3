package HU19;

import claseslogicas.EstadoFactura;
import claseslogicas.Factura;
import dao.ConexionBD;
import dao.FacturaDAO;
import org.junit.jupiter.api.Test;
import service.FacturaService;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU19GestionFacturacionTest {

    private final FacturaService facturaService =
            new FacturaService();

    private final FacturaDAO facturaDAO =
            new FacturaDAO();


    /**
     * Obtiene las facturas que ya existen actualmente
     * en la base de datos de testing.
     */
    private List<Factura> obtenerFacturasExistentes() {

        List<Factura> facturas =
                facturaService.obtenerTodas();

        assertNotNull(
                facturas,
                "La consulta de facturas no debe devolver null."
        );

        assertFalse(
                facturas.isEmpty(),
                "La BD de testing debe contener al menos una factura existente."
        );

        return facturas;
    }


    /**
     * Obtiene una factura existente en estado FACTURADA.
     * No se crea ninguna factura nueva.
     */
    private Factura obtenerFacturaFacturada() {

        return obtenerFacturasExistentes()
                .stream()
                .filter(f ->
                        f.getEstadoFactura()
                                == EstadoFactura.FACTURADA
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "La BD de testing no contiene una factura en estado FACTURADA."
                        )
                );
    }


    /**
     * Guarda el estado, método de pago y monto originales
     * de una factura para poder restaurarlos después.
     */
    private EstadoOriginal obtenerEstadoOriginal(
            int idFactura)
            throws Exception {

        String sql =
                "SELECT id_estado_factura, id_metodo, total " +
                        "FROM factura " +
                        "WHERE id_factura = ?";

        try (
                Connection conn =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idFactura
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                assertTrue(
                        rs.next(),
                        "No existe la factura seleccionada."
                );

                return new EstadoOriginal(
                        rs.getInt(
                                "id_estado_factura"
                        ),
                        rs.getObject(
                                "id_metodo",
                                Integer.class
                        ),
                        rs.getBigDecimal(
                                "total"
                        )
                );
            }
        }
    }


    /**
     * Restaura exactamente los valores originales
     * de la factura utilizada por la prueba.
     */
    private void restaurarFactura(
            int idFactura,
            EstadoOriginal original)
            throws Exception {

        String sql =
                "UPDATE factura " +
                        "SET id_estado_factura = ?, " +
                        "id_metodo = ?, " +
                        "total = ? " +
                        "WHERE id_factura = ?";

        try (
                Connection conn =
                        ConexionBD.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    original.idEstado()
            );

            if (original.idMetodo() == null) {
                ps.setNull(
                        2,
                        java.sql.Types.INTEGER
                );
            } else {
                ps.setInt(
                        2,
                        original.idMetodo()
                );
            }

            ps.setBigDecimal(
                    3,
                    original.total()
            );

            ps.setInt(
                    4,
                    idFactura
            );

            ps.executeUpdate();
        }
    }


    // ---------------------------------------------------------
    // 1. CONSULTAR FACTURAS
    // ---------------------------------------------------------

    @Test
    void consultarFacturasGeneradas() {

        List<Factura> facturas =
                obtenerFacturasExistentes();

        assertFalse(
                facturas.isEmpty(),
                "Debe ser posible consultar las facturas generadas."
        );

        assertTrue(
                facturas.stream()
                        .allMatch(f ->
                                f.getIdFactura() > 0
                        ),
                "Cada factura consultada debe poseer un identificador válido."
        );
    }


    // ---------------------------------------------------------
    // 2. CONSULTAR POR PERÍODO
    // ---------------------------------------------------------

    @Test
    void consultarFacturasPorPeriodo() {

        LocalDate desde =
                LocalDate.of(2020, 1, 1);

        LocalDate hasta =
                LocalDate.now().plusDays(1);

        List<Factura> facturas =
                facturaService.obtenerPorRango(
                        desde,
                        hasta
                );

        assertNotNull(
                facturas
        );

        assertTrue(
                facturas.stream()
                        .allMatch(f ->
                                f.getFechaHora() != null
                        ),
                "Las facturas obtenidas por período deben contener fecha."
        );

        assertTrue(
                facturas.stream()
                        .allMatch(f ->
                                !f.getFechaHora()
                                        .toLocalDate()
                                        .isBefore(desde)
                        ),
                "No deben incluirse facturas anteriores al período."
        );

        assertTrue(
                facturas.stream()
                        .allMatch(f ->
                                !f.getFechaHora()
                                        .toLocalDate()
                                        .isAfter(hasta)
                        ),
                "No deben incluirse facturas posteriores al período."
        );
    }


    // ---------------------------------------------------------
    // 3. FILTRAR POR MÉTODO DE PAGO
    // ---------------------------------------------------------

    @Test
    void filtrarFacturasPorMetodoDePago() {

        List<Factura> facturas =
                obtenerFacturasExistentes();

        List<Factura> conMetodo =
                facturas.stream()
                        .filter(f ->
                                f.getMetodoPago() != null
                                        &&
                                        !f.getMetodoPago()
                                                .isBlank()
                        )
                        .toList();

        assertFalse(
                conMetodo.isEmpty(),
                "Debe existir al menos una factura con método de pago registrado."
        );

        String metodo =
                conMetodo.get(0)
                        .getMetodoPago();

        List<Factura> filtradas =
                facturas.stream()
                        .filter(f ->
                                metodo.equalsIgnoreCase(
                                        f.getMetodoPago()
                                )
                        )
                        .toList();

        assertFalse(
                filtradas.isEmpty()
        );

        assertTrue(
                filtradas.stream()
                        .allMatch(f ->
                                metodo.equalsIgnoreCase(
                                        f.getMetodoPago()
                                )
                        ),
                "El filtro debe devolver únicamente facturas del método seleccionado."
        );
    }


    // ---------------------------------------------------------
    // 4. DATOS Y ESTADO DE FACTURA
    // ---------------------------------------------------------

    @Test
    void facturaMuestraDatosYEstado() {

        Factura factura =
                obtenerFacturasExistentes()
                        .get(0);

        assertTrue(
                factura.getIdFactura() > 0
        );

        assertTrue(
                factura.getIdCliente() > 0
        );

        assertNotNull(
                factura.getFechaHora()
        );

        assertNotNull(
                factura.getMontoTotal()
        );

        assertTrue(
                factura.getMontoTotal()
                        .compareTo(BigDecimal.ZERO) >= 0
        );

        assertNotNull(
                factura.getEstadoFactura()
        );
    }


    // ---------------------------------------------------------
    // 5. REGISTRAR PAGO
    // ---------------------------------------------------------

    @Test
    void registrarPagoActualizaEstadoDeFacturadaAPagada()
            throws Exception {

        Factura factura =
                obtenerFacturaFacturada();

        EstadoOriginal original =
                obtenerEstadoOriginal(
                        factura.getIdFactura()
                );

        try {

            facturaService.marcarComoPagada(
                    factura.getIdFactura()
            );

            Factura actualizada =
                    facturaDAO.obtenerPorId(
                            factura.getIdFactura()
                    );

            assertNotNull(
                    actualizada
            );

            assertEquals(
                    EstadoFactura.PAGADA,
                    actualizada.getEstadoFactura()
            );

        } finally {

            restaurarFactura(
                    factura.getIdFactura(),
                    original
            );
        }
    }


    // ---------------------------------------------------------
    // 6. SELECCIONAR MÉTODO DE PAGO AL COBRAR
    // ---------------------------------------------------------

    @Test
    void seleccionarMetodoPagoAlRegistrarPago()
            throws Exception {

        Factura factura =
                obtenerFacturaFacturada();

        EstadoOriginal original =
                obtenerEstadoOriginal(
                        factura.getIdFactura()
                );

        try {

            String metodoPago =
                    "Efectivo";

            BigDecimal montoOriginal =
                    factura.getMontoTotal();

            facturaService.cobrarFactura(
                    factura.getIdFactura(),
                    metodoPago,
                    montoOriginal
            );

            Factura actualizada =
                    facturaDAO.obtenerPorId(
                            factura.getIdFactura()
                    );

            assertNotNull(
                    actualizada
            );

            assertEquals(
                    EstadoFactura.PAGADA,
                    actualizada.getEstadoFactura()
            );

            assertEquals(
                    metodoPago,
                    actualizada.getMetodoPago()
            );

            assertEquals(
                    montoOriginal,
                    actualizada.getMontoTotal()
            );

        } finally {

            restaurarFactura(
                    factura.getIdFactura(),
                    original
            );
        }
    }


    // ---------------------------------------------------------
    // 7. ANULAR FACTURA
    // ---------------------------------------------------------

    @Test
    void anularFactura()
            throws Exception {

        Factura factura =
                obtenerFacturaFacturada();

        EstadoOriginal original =
                obtenerEstadoOriginal(
                        factura.getIdFactura()
                );

        try {

            facturaService.cancelarFactura(
                    factura.getIdFactura()
            );

            Factura anulada =
                    facturaDAO.obtenerPorId(
                            factura.getIdFactura()
                    );

            assertNotNull(
                    anulada
            );

            assertEquals(
                    EstadoFactura.ANULADA,
                    anulada.getEstadoFactura()
            );

        } finally {

            restaurarFactura(
                    factura.getIdFactura(),
                    original
            );
        }
    }


    // ---------------------------------------------------------
    // 8. NO PERMITIR COBRAR UNA FACTURA ANULADA
    // ---------------------------------------------------------

    @Test
    void noPermitirCobrarFacturaAnulada()
            throws Exception {

        Factura factura =
                obtenerFacturaFacturada();

        EstadoOriginal original =
                obtenerEstadoOriginal(
                        factura.getIdFactura()
                );

        try {

            facturaService.cancelarFactura(
                    factura.getIdFactura()
            );

            assertThrows(
                    Exception.class,
                    () ->
                            facturaService.marcarComoPagada(
                                    factura.getIdFactura()
                            )
            );

        } finally {

            restaurarFactura(
                    factura.getIdFactura(),
                    original
            );
        }
    }


    // ---------------------------------------------------------
    // 9. IMPRIMIR / GENERAR FACTURA
    // ---------------------------------------------------------

    @Test
    void imprimirFacturaExistente()
            throws Exception {

        Factura factura =
                obtenerFacturasExistentes()
                        .get(0);

        Path archivo =
                Files.createTempFile(
                        "HU19_factura_",
                        ".pdf"
                );

        /*
         * La impresión individual de factura se realiza
         * desde el objeto Factura existente.
         *
         * Se utiliza reflexión únicamente para invocar
         * el método real del exportador, evitando inventar
         * una factura de prueba.
         */
        Class<?> claseExportador =
                Class.forName(
                        "claseslogicas.ExportadorPDF"
                );

        Object exportador =
                claseExportador
                        .getDeclaredConstructor()
                        .newInstance();

        var metodo =
                claseExportador.getMethod(
                        "exportarFacturaIndividualPDF",
                        Factura.class,
                        java.io.File.class
                );

        metodo.invoke(
                exportador,
                factura,
                archivo.toFile()
        );

        assertTrue(
                Files.exists(archivo)
        );

        assertTrue(
                Files.size(archivo) > 0
        );

        Files.deleteIfExists(
                archivo
        );
    }


    private record EstadoOriginal(
            int idEstado,
            Integer idMetodo,
            BigDecimal total
    ) {
    }
}