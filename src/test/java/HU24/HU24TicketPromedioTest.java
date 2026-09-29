package HU24;

import org.junit.jupiter.api.Test;
import service.ReporteService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HU24TicketPromedioTest {

    private final ReporteService reporteService =
            new ReporteService();


    // =========================================================
    // 1. SELECCIONAR PERÍODO DE CONSULTA
    // =========================================================

    @Test
    void seleccionarPeriodoDeConsulta() throws Exception {

        LocalDate desde =
                LocalDate.of(2100, 1, 1);

        LocalDate hasta =
                LocalDate.of(2100, 1, 31);

        BigDecimal resultado =
                reporteService.obtenerTicketPromedio(
                        desde,
                        hasta
                );

        assertNotNull(
                resultado,
                "El sistema debe permitir consultar un período y devolver un resultado."
        );
    }


    // =========================================================
    // 2. UTILIZAR FACTURACIÓN DEL PERÍODO SELECCIONADO
    // =========================================================

    @Test
    void utilizarFacturacionCorrespondienteAlPeriodoSeleccionado()
            throws Exception {

        LocalDate desde =
                LocalDate.of(2100, 1, 1);

        LocalDate hasta =
                LocalDate.of(2100, 1, 31);

        BigDecimal resultado =
                reporteService.obtenerTicketPromedio(
                        desde,
                        hasta
                );

        BigDecimal esperado =
                new BigDecimal("15000.00");

        assertEquals(
                0,
                resultado.compareTo(esperado),
                "El ticket promedio debe utilizar únicamente la facturación correspondiente al período seleccionado."
        );
    }


    // =========================================================
    // 3. CALCULAR PROMEDIO DEL IMPORTE GASTADO POR VISITA
    // =========================================================

    @Test
    void calcularPromedioDelImporteGastadoPorVisita()
            throws Exception {

        LocalDate desde =
                LocalDate.of(2100, 1, 1);

        LocalDate hasta =
                LocalDate.of(2100, 1, 31);

        BigDecimal resultado =
                reporteService.obtenerTicketPromedio(
                        desde,
                        hasta
                );

        BigDecimal total =
                new BigDecimal("10000.00")
                        .add(new BigDecimal("20000.00"));

        BigDecimal cantidadVisitas =
                new BigDecimal("2");

        BigDecimal esperado =
                total.divide(cantidadVisitas);

        assertEquals(
                0,
                resultado.compareTo(esperado),
                "El ticket promedio debe calcularse como el promedio de los importes facturados durante el período."
        );
    }


    // =========================================================
    // 4. MOSTRAR VALOR DEL TICKET PROMEDIO
    // =========================================================

    @Test
    void devolverValorDelTicketPromedio()
            throws Exception {

        LocalDate desde =
                LocalDate.of(2100, 1, 1);

        LocalDate hasta =
                LocalDate.of(2100, 1, 31);

        BigDecimal resultado =
                reporteService.obtenerTicketPromedio(
                        desde,
                        hasta
                );

        assertNotNull(
                resultado,
                "El sistema debe devolver el valor del ticket promedio."
        );

        assertTrue(
                resultado.compareTo(BigDecimal.ZERO) > 0,
                "El ticket promedio debe ser mayor que cero cuando existen facturas en el período."
        );
    }


    // =========================================================
    // 5. EL RESULTADO CORRESPONDE AL PERÍODO SELECCIONADO
    // =========================================================

    @Test
    void resultadoCorrespondeAlPeriodoSeleccionado()
            throws Exception {

        BigDecimal promedioEnero =
                reporteService.obtenerTicketPromedio(
                        LocalDate.of(2100, 1, 1),
                        LocalDate.of(2100, 1, 31)
                );

        BigDecimal promedioFebrero =
                reporteService.obtenerTicketPromedio(
                        LocalDate.of(2100, 2, 1),
                        LocalDate.of(2100, 2, 28)
                );

        // Enero:
        // $10.000 FACTURADA
        // $20.000 PAGADA
        // $99.999 ANULADA -> no participa
        //
        // (10.000 + 20.000) / 2 = 15.000

        assertEquals(
                0,
                promedioEnero.compareTo(
                        new BigDecimal("15000.00")
                ),
                "Enero debe devolver un ticket promedio de $15.000."
        );

        // Febrero:
        // $30.000 FACTURADA
        //
        // Ticket promedio = $30.000

        assertEquals(
                0,
                promedioFebrero.compareTo(
                        new BigDecimal("30000.00")
                ),
                "Febrero debe devolver un ticket promedio de $30.000."
        );

        assertNotEquals(
                0,
                promedioEnero.compareTo(promedioFebrero),
                "El resultado debe corresponder al período seleccionado."
        );
    }
}