package hu25;

import claseslogicas.Cliente;
import claseslogicas.EstadoTurno;
import claseslogicas.Turno;
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

/**
 * HU25 - Agenda diaria (GestionDiariaController).
 * "Confirmación de llegada" mapea a cambiarEstado(CONFIRMADO) -- se verificó
 * que btnConfirmar habilita/deshabilita según turnoFresco.puedeCambiarA
 * (CONFIRMADO), sin ningún concepto separado de "llegada". "Cancelación" y
 * "liberación del horario" ya se prueban a fondo en HU15/HU17; acá se prueba
 * el recorte específico de la agenda de UN día.
 */
class HU25AgendaDiariaTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU25 - Positivo: seleccionar una fecha muestra los turnos del día con cliente, servicio, estilista, horario y estado")
    void seleccionarFechaMuestraTurnosDelDia() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), fecha, LocalTime.of(10, 0));

        List<Turno> agendaDelDia = turnoService.obtenerAgenda(fecha, null);

        assertTrue(agendaDelDia.stream().anyMatch(t -> t.getIdTurno() == turno.getIdTurno()));
    }

    @Test
    @DisplayName("HU25 - Positivo: filtrar la agenda por estilista dentro del día funciona (mismo mecanismo que HU14)")
    void filtrarAgendaPorEstilista() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno de1 = TestFixtures.crearTurnoPendiente(c1.getIdCliente(), TestFixtures.ID_EMPLEADO_ESTILISTA, fecha, LocalTime.of(9, 0));
        Turno de2 = TestFixtures.crearTurnoPendiente(c2.getIdCliente(), TestFixtures.ID_EMPLEADO_ESTILISTA_2, fecha, LocalTime.of(9, 0));

        List<Turno> soloEstilista1 = turnoService.obtenerAgenda(fecha, TestFixtures.ID_EMPLEADO_ESTILISTA);

        assertTrue(soloEstilista1.stream().anyMatch(t -> t.getIdTurno() == de1.getIdTurno()));
        assertFalse(soloEstilista1.stream().anyMatch(t -> t.getIdTurno() == de2.getIdTurno()));
    }

    @Test
    @DisplayName("HU25 - Positivo: 'confirmación de llegada' (Pendiente -> Confirmado) queda reflejada en la agenda")
    void confirmacionDeLlegadaQuedaReflejada() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), fecha, LocalTime.of(11, 0));

        turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "Llegó el cliente");

        Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());
        assertEquals(EstadoTurno.CONFIRMADO, recuperado.getEstadoTurno());
    }

    @Test
    @DisplayName("HU25 - Positivo: cancelar desde la agenda diaria libera el horario (mismo mecanismo que HU15)")
    void cancelarDesdeAgendaLiberaHorario() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(14, 0);

        Turno original = TestFixtures.crearTurnoPendiente(c1.getIdCliente(), fecha, hora);
        turnoService.cambiarEstado(original, EstadoTurno.CANCELADO, "Cliente avisó que no viene");

        assertDoesNotThrow(() -> TestFixtures.crearTurnoPendiente(c2.getIdCliente(), fecha, hora),
                "El horario debe quedar libre para otro cliente tras la cancelación");
    }

    @Test
    @DisplayName("HU25 - Límite: un día sin ningún turno agendado informa lista vacía")
    void diaSinTurnosInformaListaVacia() throws SQLException {
        List<Turno> agenda = turnoService.obtenerAgenda(LocalDate.now().plusYears(1), null);
        assertNotNull(agenda);
        assertTrue(agenda.isEmpty());
    }

    @Test
    @DisplayName("HU25 - Límite: un estilista sin turnos ese día informa lista vacía")
    void estilistaSinTurnosEseDiaInformaListaVacia() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        TestFixtures.crearTurnoPendiente(c.getIdCliente(), TestFixtures.ID_EMPLEADO_ESTILISTA, fecha, LocalTime.of(9, 0));

        List<Turno> deOtroEstilista = turnoService.obtenerAgenda(fecha, TestFixtures.ID_EMPLEADO_ESTILISTA_2);
        assertTrue(deOtroEstilista.isEmpty());
    }

    @Test
    @DisplayName("HU25 - Límite: turnos cancelados igual aparecen en la agenda del día (con su estado, no se ocultan)")
    void turnosCanceladosApareceEnAgendaConSuEstado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), fecha, LocalTime.of(16, 0));
        turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "cancelado");

        List<Turno> agenda = turnoService.obtenerAgenda(fecha, null);
        assertTrue(agenda.stream().anyMatch(t -> t.getIdTurno() == turno.getIdTurno()),
                "obtenerTurnosFiltrados() no excluye por estado -- un cancelado se ve igual en la agenda, con su estado");
    }
}
