package hu18;

import claseslogicas.Cliente;
import claseslogicas.ClienteReporteExtendido;
import dao.ReporteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU18 - Reporte por cliente: tabla de facturación, datos consistentes y trazables. */
class HU18ReportePorClienteTest {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU18 - Positivo: la tabla incluye cliente con gasto total y cantidad de visitas trazables")
    void tablaIncluyeDatosConsistentes() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0),
                "Corte de pelo", "Efectivo");

        List<ClienteReporteExtendido> reporte = reporteDAO.obtenerDatosClientesExtendido();

        ClienteReporteExtendido fila = reporte.stream()
                .filter(r -> r.getNombreCompleto().contains("Fixture"))
                .findFirst().orElseThrow(() -> new AssertionError("El cliente de test debe aparecer en el reporte"));

        assertEquals(1, fila.getCantidadVisitas(), "1 visita registrada -> cantidadVisitas debe ser 1 (trazable a la tabla visita)");
        assertEquals(0, java.math.BigDecimal.valueOf(8000).compareTo(fila.getGastoTotal()),
                "El gasto total debe ser exactamente el precio del servicio de Corte de pelo ($8000), sin recargo (pagó en Efectivo)");
    }

    @Test
    @DisplayName("HU18 - Límite: un cliente sin ninguna visita/factura aparece con gasto 0, no null ni excepción")
    void clienteSinDatosApareceConGastoCero() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        List<ClienteReporteExtendido> reporte = reporteDAO.obtenerDatosClientesExtendido();
        ClienteReporteExtendido fila = reporte.stream()
                .filter(r -> r.getIdPersona() == c.getIdPersona())
                .findFirst().orElseThrow();

        assertEquals(0, fila.getCantidadVisitas());
        assertNotNull(fila.getGastoTotal(), "gastoTotal no debe ser null gracias al COALESCE en el SQL");
        assertEquals(0, java.math.BigDecimal.ZERO.compareTo(fila.getGastoTotal()));
    }
}
