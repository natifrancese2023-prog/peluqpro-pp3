package hu14;

import claseslogicas.Cliente;
import claseslogicas.Turno;
import dao.EmpleadoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.TurnoService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU14 - Filtro de turnos por estilista. */
class HU14FiltroPorEstilistaTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU14 - Positivo: filtrar por estilista muestra sólo los turnos asignados a ese estilista")
    void filtrarPorEstilistaMuestraSusTurnos() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno deEstilista1 = TestFixtures.crearTurnoPendiente(c1.getIdCliente(), TestFixtures.ID_EMPLEADO_ESTILISTA, fecha, LocalTime.of(10, 0));
        Turno deEstilista2 = TestFixtures.crearTurnoPendiente(c2.getIdCliente(), TestFixtures.ID_EMPLEADO_ESTILISTA_2, fecha, LocalTime.of(10, 0));

        List<Turno> filtradoPorEstilista1 = turnoService.obtenerAgenda(fecha, TestFixtures.ID_EMPLEADO_ESTILISTA);

        assertTrue(filtradoPorEstilista1.stream().anyMatch(t -> t.getIdTurno() == deEstilista1.getIdTurno()),
                "El turno del estilista filtrado sí debe aparecer");
        assertFalse(filtradoPorEstilista1.stream().anyMatch(t -> t.getIdTurno() == deEstilista2.getIdTurno()),
                "El turno de OTRO estilista, mismo día y hora, no debe aparecer al filtrar por el primero " +
                        "(el filtro se aplica en el WHERE del SQL, no requiere leer id_empleado del resultado)");
    }

    @Test
    @DisplayName("HU14 - Límite: estilista sin turnos ese día informa lista vacía, no error")
    void estilistaSinTurnosDevuelveListaVacia() throws SQLException {
        // id_empleado 3 = estilista del seed; ese día no se creó ningún turno.
        List<Turno> turnos = turnoService.obtenerAgenda(LocalDate.now().plusDays(30), TestFixtures.ID_EMPLEADO_ESTILISTA);
        assertNotNull(turnos);
        assertTrue(turnos.isEmpty());
    }

    @Test
    @DisplayName("HU14 - Negativo: filtrar por un id de estilista inexistente no rompe, devuelve vacío")
    void estilistaInexistenteNoRompe() {
        assertDoesNotThrow(() -> {
            List<Turno> turnos = turnoService.obtenerAgenda(LocalDate.now().plusDays(1), 999999);
            assertTrue(turnos.isEmpty());
        });
    }
}
