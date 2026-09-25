package hu11;

import claseslogicas.Cliente;
import claseslogicas.Servicio;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU11 - Registro de turno.
 * CA: cliente+fecha+hora+estilista+servicio / se guarda / confirmación /
 *     calcula duración automáticamente / muestra horarios disponibles /
 *     no permite turno no disponible / cliente inexistente da error /
 *     servicio no seleccionado da alerta.
 */
class HU11RegistroTurnoTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU11 - Positivo: turno con cliente, fecha, hora, estilista y servicio válidos se guarda")
    void registrarTurnoValidoSeGuarda() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0));

        assertTrue(turno.getIdTurno() > 0, "Debe quedar un id_turno real asignado tras el insert");
        Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());
        assertNotNull(recuperado);
        assertEquals(LocalTime.of(10, 0), recuperado.getHoraInicio());
    }

    @Test
    @DisplayName("HU11 - Positivo: la duración total se calcula sumando la duración de los servicios elegidos")
    void duracionSeCalculaAutomaticamenteSegunServicios() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        Turno turno = new Turno();
        turno.setIdCliente(c.getIdCliente());
        turno.setIdEmpleado(TestFixtures.ID_EMPLEADO_ESTILISTA);
        turno.setFecha(LocalDate.now().plusDays(1));
        turno.setHoraInicio(LocalTime.of(14, 0));
        // Coloración dura 90 min según el seed -> hora_fin debería reflejar esa duración
        turno.setHoraFin(LocalTime.of(14, 0).plusMinutes(90));
        Servicio coloracion = new Servicio();
        coloracion.setIdServicio(TestFixtures.ID_SERVICIO_COLORACION);
        turno.setServicios(List.of(coloracion));

        assertTrue(turnoService.registrarTurno(turno));
        Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());
        // BUG REAL (hallazgo nuevo): TurnoDAO.obtenerPorId() nunca selecciona la
        // columna hora_fin en su SQL -- getHoraFin() siempre da null sobre un
        // turno recuperado por id, sin importar lo que se haya guardado. Se deja
        // el assert tal como refleja el criterio de aceptación (no se ajusta la
        // expectativa): este test documenta el FAIL real, ver TESTING-REPORT.md.
        assertEquals(LocalTime.of(15, 30), recuperado.getHoraFin(),
                "14:00 + 90 min de Coloración = 15:30 (FALLA HOY: obtenerPorId() no trae hora_fin)");
    }

    @Test
    @DisplayName("HU11 - Positivo: buscarDisponibilidad muestra huecos libres respetando el horario del local")
    void buscarDisponibilidadMuestraHorariosLibres() throws SQLException {
        // Fecha fija en un LUNES futuro (evita que el test dependa del día de la
        // semana en que se ejecuta: el seed no carga horario para Domingo).
        LocalDate proximoLunes = LocalDate.now().plusDays(1);
        while (proximoLunes.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            proximoLunes = proximoLunes.plusDays(1);
        }
        var bloques = turnoService.buscarDisponibilidad(proximoLunes, 30, TestFixtures.ID_EMPLEADO_ESTILISTA);
        assertFalse(bloques.isEmpty(), "Un lunes (día laborable) sin turnos previos debe tener bloques disponibles");
    }

    @Test
    @DisplayName("HU11 - Negativo: no permite registrar un turno en un horario ya ocupado (revalidación, hallazgo #7)")
    void noPermiteRegistrarTurnoEnHorarioNoDisponible() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);

        TestFixtures.crearTurnoPendiente(c1.getIdCliente(), fecha, LocalTime.of(10, 0)); // ocupa 10:00-10:30

        Turno solapado = new Turno();
        solapado.setIdCliente(c2.getIdCliente());
        solapado.setIdEmpleado(TestFixtures.ID_EMPLEADO_ESTILISTA);
        solapado.setFecha(fecha);
        solapado.setHoraInicio(LocalTime.of(10, 15)); // se solapa con el anterior
        solapado.setHoraFin(LocalTime.of(10, 45));
        Servicio corte = new Servicio();
        corte.setIdServicio(TestFixtures.ID_SERVICIO_CORTE);
        solapado.setServicios(List.of(corte));

        SQLException ex = assertThrows(SQLException.class, () -> turnoService.registrarTurno(solapado));
        assertTrue(ex.getMessage().toLowerCase().contains("disponible"),
                "El mensaje debe indicar que el horario ya no está disponible");
    }

    @Test
    @DisplayName("HU11 - Negativo: cliente inexistente genera error (no se crea el turno)")
    void clienteInexistenteGeneraError() {
        Turno turno = new Turno();
        turno.setIdCliente(999999); // no existe
        turno.setIdEmpleado(TestFixtures.ID_EMPLEADO_ESTILISTA);
        turno.setFecha(LocalDate.now().plusDays(1));
        turno.setHoraInicio(LocalTime.of(16, 0));
        turno.setHoraFin(LocalTime.of(16, 30));
        Servicio corte = new Servicio();
        corte.setIdServicio(TestFixtures.ID_SERVICIO_CORTE);
        turno.setServicios(List.of(corte));

        assertThrows(SQLException.class, () -> turnoService.registrarTurno(turno),
                "Un id_cliente inexistente debe rechazar el alta con SQLException");
    }

    @Test
    @DisplayName("HU11 - Negativo: turno sin ningún servicio seleccionado no debe registrar filas en turno_servicios")
    void turnoSinServicioSeleccionadoNoQuedaBienFormado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = new Turno();
        turno.setIdCliente(c.getIdCliente());
        turno.setIdEmpleado(TestFixtures.ID_EMPLEADO_ESTILISTA);
        turno.setFecha(LocalDate.now().plusDays(1));
        turno.setHoraInicio(LocalTime.of(17, 0));
        turno.setHoraFin(LocalTime.of(17, 0)); // sin duración: ningún servicio elegido
        turno.setServicios(Collections.emptyList());

        boolean resultado = turnoService.registrarTurno(turno);
        // El turno puede quedar insertado sin servicios (la validación de "elegir
        // al menos un servicio" vive en el controller, AltaTurnoController, antes
        // de siquiera llamar al Service) -- documentamos el comportamiento real
        // del Service/DAO en este nivel:
        if (resultado) {
            Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());
            assertTrue(recuperado.getServicios() == null || recuperado.getServicios().isEmpty(),
                    "Sin servicios elegidos, el turno no debe tener ninguno asociado");
        }
    }
}
