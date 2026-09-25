package hu26;

import claseslogicas.Cliente;
import claseslogicas.Factura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import service.FacturaService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU26 - Gestión y consulta de facturación. */
class HU26FacturacionTest {

    private final FacturaService facturaService = new FacturaService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU26 - Positivo: consulta de facturas por período trae la factura generada")
    void consultaPorPeriodoTraeLaFactura() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Factura f = TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Corte de pelo", "Efectivo");
        LocalDate hoy = LocalDate.now();

        List<Factura> resultado = facturaService.obtenerPorRango(hoy, hoy);

        assertTrue(resultado.stream().anyMatch(x -> x.getIdFactura() == f.getIdFactura()));
    }

    @Test
    @DisplayName("HU26 - Positivo: filtro por forma de pago (dos facturas con métodos distintos)")
    void filtroPorFormaDePago() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c1.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Corte de pelo", "Efectivo");
        TestFixtures.crearFacturaPagada(c2.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Peinado", "Tarjeta Crédito");

        List<Factura> todas = facturaService.obtenerPorRango(LocalDate.now(), LocalDate.now());
        long enEfectivoCount = todas.stream().filter(f -> "Efectivo".equals(f.getMetodoPago())).count();
        long enCreditoCount = todas.stream().filter(f -> "Tarjeta Crédito".equals(f.getMetodoPago())).count();

        assertTrue(enEfectivoCount >= 1);
        assertTrue(enCreditoCount >= 1);
    }

    @Test
    @DisplayName("HU26 - Positivo: cobrar una factura pendiente (Facturada) la pasa a Pagada")
    void cobrarFacturaPendientePasaAPagada() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        var turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        var servicios = javafx.collections.FXCollections.observableArrayList(
                new claseslogicas.ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "", "Pendiente"));
        new service.VisitaService().registrarVisita(new dao.ClienteDAO().obtenerPorId(c.getIdCliente()), servicios,
                TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());

        Factura recienFacturada = facturaService.obtenerPorTurno(turno.getIdTurno());
        assertEquals("Facturada", recienFacturada.getEstadoFactura().getNombre());

        facturaService.cobrarFactura(recienFacturada.getIdFactura(), "Efectivo", recienFacturada.getMontoTotal());

        Factura cobrada = facturaService.obtenerPorTurno(turno.getIdTurno());
        assertEquals("Pagada", cobrada.getEstadoFactura().getNombre());
    }

    @Test
    @DisplayName("HU26 - Positivo: cancelar/anular una factura Facturada (aún no pagada) es válido")
    void cancelarFacturaFacturadaEsValido() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        var turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(11, 0));
        var servicios = javafx.collections.FXCollections.observableArrayList(
                new claseslogicas.ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "", "Pendiente"));
        new service.VisitaService().registrarVisita(new dao.ClienteDAO().obtenerPorId(c.getIdCliente()), servicios,
                TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());
        Factura factura = facturaService.obtenerPorTurno(turno.getIdTurno());

        facturaService.cancelarFactura(factura.getIdFactura());

        Factura anulada = facturaService.obtenerPorTurno(turno.getIdTurno());
        assertEquals("Anulada", anulada.getEstadoFactura().getNombre());
    }

    @Test
    @DisplayName("HU26 - Negativo: factura inexistente al intentar cobrarla genera error")
    void facturaInexistenteAlCobrarGeneraError() {
        assertThrows(service.EstadoFacturaInvalidoException.class, () -> facturaService.cobrarFactura(999999, "Efectivo", java.math.BigDecimal.TEN));
    }

    @Test
    @DisplayName("HU26 - Negativo: factura inexistente al intentar cancelarla genera error")
    void facturaInexistenteAlCancelarGeneraError() {
        assertThrows(service.EstadoFacturaInvalidoException.class, () -> facturaService.cancelarFactura(999999));
    }

    @Test
    @DisplayName("HU26 - Negativo: intentar cobrar una factura ya Pagada es rechazado (estado terminal)")
    void cobrarFacturaYaPagadaEsRechazado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Factura pagada = TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(12, 0), "Corte de pelo", "Efectivo");

        assertThrows(service.EstadoFacturaInvalidoException.class,
                () -> facturaService.cobrarFactura(pagada.getIdFactura(), "Efectivo", pagada.getMontoTotal()),
                "PAGADA es un estado terminal: no se puede volver a cobrar");
    }

    @Test
    @DisplayName("HU26 - Negativo: intentar anular una factura ya Pagada es rechazado (estado terminal)")
    void anularFacturaYaPagadaEsRechazado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Factura pagada = TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(13, 0), "Corte de pelo", "Efectivo");

        assertThrows(service.EstadoFacturaInvalidoException.class, () -> facturaService.cancelarFactura(pagada.getIdFactura()),
                "PAGADA es un estado terminal: no se puede anular una factura ya cobrada");
    }

    @Test
    @DisplayName("HU26 - Negativo: intentar cobrar una factura ya Anulada es rechazado (estado terminal)")
    void cobrarFacturaAnuladaEsRechazado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        var turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(14, 0));
        var servicios = javafx.collections.FXCollections.observableArrayList(
                new claseslogicas.ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "", "Pendiente"));
        new service.VisitaService().registrarVisita(new dao.ClienteDAO().obtenerPorId(c.getIdCliente()), servicios,
                TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());
        Factura factura = facturaService.obtenerPorTurno(turno.getIdTurno());
        facturaService.cancelarFactura(factura.getIdFactura());

        assertThrows(service.EstadoFacturaInvalidoException.class,
                () -> facturaService.cobrarFactura(factura.getIdFactura(), "Efectivo", factura.getMontoTotal()),
                "ANULADA es un estado terminal: no se puede cobrar una factura anulada");
    }

    @Test
    @DisplayName("HU26 - Límite: período sin resultados devuelve lista vacía, no error")
    void periodoSinResultadosDevuelveListaVacia() {
        List<Factura> resultado = facturaService.obtenerPorRango(LocalDate.now().plusYears(3), LocalDate.now().plusYears(3));
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @Disabled("BLOCKED: 'imprimir' en ListadoFacturasController exporta un PDF via FileChooser (ActionEvent), " +
            "requiere UI real (TestFX no disponible). La regla de negocio (solo se puede imprimir en estado " +
            "Pagada) se verifico por lectura de codigo, no ejecucion. Ver TESTING-REPORT.md.")
    @DisplayName("HU26 - BLOCKED: impresión de factura (sólo permitida en estado Pagada)")
    void impresionDeFactura() {
        fail("BLOCKED: requiere TestFX para invocar el flujo de impresion real.");
    }
}
