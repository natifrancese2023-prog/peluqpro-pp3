package hu09;

import claseslogicas.Cliente;
import claseslogicas.ServicioTemp;
import claseslogicas.Turno;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ClienteService;
import service.VisitaService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** HU9 - Conteo de visitas: se muestra el número total de visitas por cliente. */
class HU09ConteoVisitasTest {

    private final ClienteService clienteService = new ClienteService();
    private final VisitaService visitaService = new VisitaService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU9 - Límite: cliente sin visitas cuenta 0")
    void clienteSinVisitasCuentaCero() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        assertEquals(0, clienteService.contarVisitasPorIdCliente(c.getIdCliente()));
    }

    @Test
    @DisplayName("HU9 - Positivo: el conteo refleja exactamente la cantidad de visitas registradas")
    void conteoReflejaVisitasRegistradas() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        Turno t1 = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        visitaService.registrarVisita(c,
                FXCollections.observableArrayList(new ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "", "Pendiente")),
                TestFixtures.ID_EMPLEADO_ESTILISTA, t1.getIdTurno());
        assertEquals(1, clienteService.contarVisitasPorIdCliente(c.getIdCliente()));

        Turno t2 = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(2), LocalTime.of(9, 0));
        visitaService.registrarVisita(c,
                FXCollections.observableArrayList(new ServicioTemp(LocalDate.now(), "Peinado", "Estela Estilista", "", "Pendiente")),
                TestFixtures.ID_EMPLEADO_ESTILISTA, t2.getIdTurno());
        assertEquals(2, clienteService.contarVisitasPorIdCliente(c.getIdCliente()),
                "El conteo debe subir a 2 tras la segunda visita");
    }

    @Test
    @DisplayName("HU9 - Nota: el conteo se calcula en vivo (COUNT), no depende de cliente.numero_visitas (columna huérfana, hallazgo #24)")
    void conteoNoDependeDeColumnaHuerfana() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno t1 = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        visitaService.registrarVisita(c,
                FXCollections.observableArrayList(new ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "", "Pendiente")),
                TestFixtures.ID_EMPLEADO_ESTILISTA, t1.getIdTurno());

        // cliente.numero_visitas nunca se actualiza (queda en su default 0) y sin
        // embargo el conteo real (dinámico) tiene que dar 1 igual.
        assertEquals(1, clienteService.contarVisitasPorIdCliente(c.getIdCliente()));
    }
}
