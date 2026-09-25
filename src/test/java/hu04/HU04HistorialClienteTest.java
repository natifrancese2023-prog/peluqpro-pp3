package hu04;

import claseslogicas.Cliente;
import claseslogicas.HistorialView;
import claseslogicas.ServicioTemp;
import claseslogicas.Turno;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.VisitaService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU4 - Historial de cliente: listado de visitas previas con fecha, servicio, estilista, observaciones. */
class HU04HistorialClienteTest {

    private final VisitaService visitaService = new VisitaService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU4 - Positivo: historial muestra fecha, servicio, estilista y observaciones de cada visita")
    void historialMuestraDatosCompletos() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 30));
        visitaService.registrarVisita(c,
                FXCollections.observableArrayList(new ServicioTemp(LocalDate.now(), "Coloración", "Estela Estilista", "Rubio ceniza", "Pendiente")),
                TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());

        List<HistorialView> historial = visitaService.obtenerHistorialPorCliente(c.getIdCliente());

        assertEquals(1, historial.size());
        HistorialView item = historial.get(0);
        assertNotNull(item.getFechaHora(), "Debe incluir fecha");
        assertEquals("Coloración", item.getNombreServicio(), "Debe incluir el servicio");
        assertEquals("Estela", item.getNombreEstilista(), "Debe incluir el nombre del estilista");
        assertEquals("Rubio ceniza", item.getObservaciones(), "Debe incluir las observaciones cargadas");
    }

    @Test
    @DisplayName("HU4 - Límite: historial de un cliente sin visitas es una lista vacía, no null ni error")
    void historialDeClienteSinVisitasEsListaVacia() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        List<HistorialView> historial = visitaService.obtenerHistorialPorCliente(c.getIdCliente());

        assertNotNull(historial);
        assertTrue(historial.isEmpty());
    }

    @Test
    @DisplayName("HU4 - Límite: visita con observaciones vacías (null) no rompe la consulta del historial (hallazgo #16)")
    void historialConObservacionesNulasNoRompe() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(15, 0));
        // observaciones = "" (el form real permite dejarlo vacío; en la BD queda NULL o "")
        visitaService.registrarVisita(c,
                FXCollections.observableArrayList(new ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "", "Pendiente")),
                TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());

        assertDoesNotThrow(() -> visitaService.obtenerHistorialPorCliente(c.getIdCliente()));
    }
}
