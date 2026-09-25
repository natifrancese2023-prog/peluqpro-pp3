package hu23;

import claseslogicas.Cliente;
import dao.ReporteFacturacionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import support.TestDbSupport;
import support.TestFixtures;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU23 - Reporte de facturación por período.
 *
 * NOTA: visitaDAO.guardarNuevaVisita() graba factura.fecha_hora como
 * LocalDateTime.now() -- el momento REAL en que se carga la visita, no la
 * fecha futura del turno. Por eso estos tests consultan el reporte con
 * LocalDate.now() (hoy), aunque el turno de fixture se haya agendado para
 * el día siguiente: es el comportamiento real y correcto de la aplicación
 * (la factura nace cuando se carga la visita, no en la fecha del turno).
 *
 * Los criterios de datos (tabla, monto, forma de pago) se prueban contra
 * ReporteFacturacionDAO (donde vive el hallazgo #17, ya corregido). La
 * validación de rango ("Hasta < Desde") vive en el controller y requiere UI
 * -- BLOCKED. La exportación real de esta pantalla NO es PDF/Excel como pide
 * el criterio -- ver los tests marcados NOT_IMPLEMENTED más abajo.
 */
class HU23ReporteFacturacionPeriodoTest {

    private final ReporteFacturacionDAO dao = new ReporteFacturacionDAO();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU23 - Positivo: período válido devuelve el monto facturado del día correcto")
    void periodoValidoDevuelveMontoCorrecto() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Corte de pelo", "Efectivo");
        LocalDate hoy = LocalDate.now();

        Map<LocalDate, BigDecimal> resultado = dao.obtenerFacturacionPorDia(hoy, hoy);

        assertEquals(0, BigDecimal.valueOf(8000).compareTo(resultado.get(hoy)),
                "La factura se genera HOY (al cargar la visita), aunque el turno haya sido agendado para mañana");
    }

    @Test
    @DisplayName("HU23 - Límite: período sin resultados no rompe")
    void periodoSinResultadosNoRompe() throws SQLException {
        LocalDate fecha = LocalDate.now().plusYears(3);
        Map<LocalDate, BigDecimal> resultado = dao.obtenerFacturacionPorDia(fecha, fecha);
        assertTrue(resultado.isEmpty() || resultado.get(fecha) == null || resultado.get(fecha).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    @DisplayName("HU23 - Límite: fecha Desde = Hasta (un solo día) funciona correctamente")
    void fechaDesdeIgualHastaFunciona() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Corte de pelo", "Efectivo");
        LocalDate hoy = LocalDate.now();

        Map<LocalDate, BigDecimal> resultado = dao.obtenerFacturacionPorDia(hoy, hoy);
        assertNotNull(resultado.get(hoy));
    }

    @Test
    @DisplayName("HU23 - Negativo: datos fuera del período no se incluyen")
    void datosFueraDelPeriodoNoSeIncluyen() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Corte de pelo", "Efectivo");
        LocalDate hoy = LocalDate.now(); // la factura real queda fechada hoy

        LocalDate desde = hoy.plusDays(5);
        LocalDate hasta = hoy.plusDays(10);
        Map<LocalDate, BigDecimal> resultado = dao.obtenerFacturacionPorDia(desde, hasta);

        assertFalse(resultado.containsKey(hoy), "Un período que no incluye 'hoy' no debe traer la factura generada hoy");
    }

    @Test
    @DisplayName("HU23 - Positivo: diferentes formas de pago se distinguen correctamente (hallazgo #17)")
    void diferentesFormasDePagoSeDistinguen() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c1.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Corte de pelo", "Efectivo");
        TestFixtures.crearFacturaPagada(c2.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Peinado", "Tarjeta Crédito");
        LocalDate hoy = LocalDate.now();

        Map<LocalDate, List<String>> metodos = dao.obtenerMetodosPorFacturaPorDia(hoy, hoy);

        assertTrue(metodos.get(hoy).contains("Efectivo"));
        assertTrue(metodos.get(hoy).contains("Tarjeta Crédito"));
    }

    @Test
    @Disabled("BLOCKED: la validacion 'Hasta < Desde' vive dentro de ReporteFacturacionController.generarReporte(), " +
            "que lee DatePicker @FXML inyectados por FXMLLoader. No se puede invocar sin UI real (TestFX no " +
            "disponible en este entorno). Ver TESTING-REPORT.md.")
    @DisplayName("HU23 - BLOCKED: impedir que 'Hasta' sea menor que 'Desde'")
    void impideHastaMenorQueDesde() {
        fail("BLOCKED: requiere TestFX para invocar generarReporte() con los DatePicker reales.");
    }

    @Test
    @Disabled("NOT_IMPLEMENTED tal como lo pide el criterio: ReporteFacturacionController.exportarReporte() " +
            "exporta una CAPTURA DE PANTALLA (.png) del contenedor de graficos, no un PDF ni un Excel.")
    @DisplayName("HU23 - NOT_IMPLEMENTED: exportar el reporte a PDF")
    void exportarAPdf() {
        fail("NOT_IMPLEMENTED: este reporte exporta PNG, no PDF.");
    }

    @Test
    @Disabled("NOT_IMPLEMENTED tal como lo pide el criterio: mismo motivo que el de PDF -- solo exporta PNG.")
    @DisplayName("HU23 - NOT_IMPLEMENTED: exportar el reporte a Excel")
    void exportarAExcel() {
        fail("NOT_IMPLEMENTED: este reporte exporta PNG, no Excel.");
    }
}
