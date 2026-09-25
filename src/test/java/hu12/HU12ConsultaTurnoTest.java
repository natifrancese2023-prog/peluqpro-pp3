package hu12;

import claseslogicas.Cliente;
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

/** HU12 - Consulta de turno: buscar por fecha, información completa, turno inexistente. */
class HU12ConsultaTurnoTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU12 - Positivo: buscar por fecha devuelve la agenda completa de ese día")
    void buscarPorFechaDevuelveAgendaDelDia() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), fecha, LocalTime.of(11, 0));

        List<Turno> agenda = turnoService.obtenerAgenda(fecha, null);

        assertTrue(agenda.stream().anyMatch(t -> t.getIdTurno() == turno.getIdTurno()));
    }

    @Test
    @DisplayName("HU12 - Positivo: la información del turno recuperado por id está completa (cliente, estilista, fecha, hora, estado)")
    void turnoRecuperadoTraeInformacionCompleta() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), LocalDate.now().plusDays(2), LocalTime.of(9, 0));

        Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());

        assertNotNull(recuperado);
        // BUG REAL (hallazgo nuevo): TurnoDAO.obtenerPorId() arma un objeto
        // Cliente/Empleado anidado (turno.getCliente(), turno.getEmpleado()) pero
        // NUNCA sincroniza los campos planos turno.idCliente / turno.idEmpleado
        // -- el mismo patrón de duplicación herencia+composición ya visto en
        // Cliente.java (hallazgo previo). turno.getIdCliente() da 0 aunque el
        // dato real está en turno.getCliente().getIdCliente(). No se ajusta la
        // expectativa: se documenta el FAIL real.
        assertEquals(c.getIdCliente(), recuperado.getIdCliente(),
                "FALLA HOY: obtenerPorId() no sincroniza idCliente, solo turno.getCliente().getIdCliente() trae el valor real");
        assertEquals(TestFixtures.ID_EMPLEADO_ESTILISTA, recuperado.getIdEmpleado(),
                "FALLA HOY: mismo problema con idEmpleado, ver turno.getEmpleado().getIdEmpleado()");
        assertNotNull(recuperado.getFecha());
        assertNotNull(recuperado.getHoraInicio());
        assertNotNull(recuperado.getEstadoTurno());
    }

    @Test
    @DisplayName("HU12 - Negativo: consultar un turno inexistente devuelve null (la UI lo traduce en el mensaje correspondiente)")
    void turnoInexistenteDevuelveNull() throws SQLException {
        Turno recuperado = turnoService.obtenerPorId(999999);
        assertNull(recuperado);
    }

    @Test
    @DisplayName("HU12 - Límite: buscar por una fecha sin ningún turno agendado devuelve lista vacía, no error")
    void fechaSinTurnosDevuelveListaVacia() throws SQLException {
        List<Turno> agenda = turnoService.obtenerAgenda(LocalDate.now().plusYears(1), null);
        assertNotNull(agenda);
        assertTrue(agenda.isEmpty());
    }
}
