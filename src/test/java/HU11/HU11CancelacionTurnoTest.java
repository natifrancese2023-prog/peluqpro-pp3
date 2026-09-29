package HU11;

import claseslogicas.EstadoTurno;
import claseslogicas.Servicio;
import claseslogicas.Turno;
import org.junit.jupiter.api.Test;
import service.TurnoService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU11CancelacionTurnoTest {

    private static final int ID_CLIENTE = 10;
    private static final int ID_ESTILISTA = 3;
    private static final int ID_SERVICIO = 1;

    /*
     * Lunes. La BD de prueba tiene disponibilidad
     * del empleado 3 de 08:00 a 20:00.
     */
    private static final LocalDate FECHA_TEST =
            LocalDate.of(2026, 10, 5);

    private final TurnoService turnoService =
            new TurnoService();

    @Test
    void seleccionarTurnoRegistrado() throws SQLException {

        Turno turno = crearTurno(
                LocalTime.of(13, 0),
                LocalTime.of(13, 30)
        );

        assertTrue(
                turnoService.registrarTurno(turno),
                "El turno debe registrarse correctamente"
        );

        List<Turno> agenda =
                turnoService.obtenerAgenda(
                        FECHA_TEST,
                        ID_ESTILISTA
                );

        Turno encontrado = agenda.stream()
                .filter(t ->
                        t.getIdTurno() == turno.getIdTurno()
                )
                .findFirst()
                .orElse(null);

        assertNotNull(
                encontrado,
                "El turno registrado debe poder seleccionarse desde la agenda"
        );
    }

    @Test
    void cancelarTurno() throws SQLException {

        Turno turno = crearTurno(
                LocalTime.of(14, 0),
                LocalTime.of(14, 30)
        );

        assertTrue(
                turnoService.registrarTurno(turno),
                "El turno debe registrarse correctamente"
        );

        Turno turnoRegistrado =
                turnoService.obtenerPorId(
                        turno.getIdTurno()
                );

        assertNotNull(turnoRegistrado);

        turnoService.cambiarEstado(
                turnoRegistrado,
                EstadoTurno.CANCELADO,
                "Cancelación manual"
        );

        assertEquals(
                EstadoTurno.CANCELADO,
                turnoRegistrado.getEstadoLogico(),
                "El turno debe quedar en estado Cancelado"
        );
    }

    @Test
    void turnoCanceladoPermaneceRegistrado() throws SQLException {

        Turno turno = crearTurno(
                LocalTime.of(15, 0),
                LocalTime.of(15, 30)
        );

        assertTrue(
                turnoService.registrarTurno(turno),
                "El turno debe registrarse correctamente"
        );

        int idTurno = turno.getIdTurno();

        Turno turnoRegistrado =
                turnoService.obtenerPorId(idTurno);

        assertNotNull(turnoRegistrado);

        turnoService.cambiarEstado(
                turnoRegistrado,
                EstadoTurno.CANCELADO,
                "Cancelación manual"
        );

        Turno consultado =
                turnoService.obtenerPorId(idTurno);

        assertNotNull(
                consultado,
                "El turno cancelado debe permanecer registrado"
        );

        assertEquals(
                idTurno,
                consultado.getIdTurno()
        );

        assertEquals(
                EstadoTurno.CANCELADO,
                consultado.getEstadoLogico(),
                "El turno debe permanecer registrado con estado Cancelado"
        );
    }

    @Test
    void cancelarTurnoLiberaHorario() throws SQLException {

        LocalTime horaInicio =
                LocalTime.of(16, 0);

        LocalTime horaFin =
                LocalTime.of(16, 30);

        Turno turno = crearTurno(
                horaInicio,
                horaFin
        );

        assertTrue(
                turnoService.registrarTurno(turno),
                "El turno debe registrarse correctamente"
        );

        // Antes de cancelar, el horario está ocupado.
        List<claseslogicas.BloqueDisponible> antes =
                turnoService.buscarDisponibilidad(
                        FECHA_TEST,
                        30,
                        ID_ESTILISTA
                );

        assertFalse(
                contieneHorario(antes, horaInicio, horaFin),
                "El horario ocupado no debe estar disponible"
        );

        Turno turnoRegistrado =
                turnoService.obtenerPorId(
                        turno.getIdTurno()
                );

        assertNotNull(turnoRegistrado);

        turnoService.cambiarEstado(
                turnoRegistrado,
                EstadoTurno.CANCELADO,
                "Cancelación manual"
        );

        // Después de cancelar, el horario vuelve a estar disponible.
        List<claseslogicas.BloqueDisponible> despues =
                turnoService.buscarDisponibilidad(
                        FECHA_TEST,
                        30,
                        ID_ESTILISTA
                );

        assertTrue(
                contieneHorario(despues, horaInicio, horaFin),
                "El horario del turno cancelado debe quedar nuevamente disponible"
        );
    }

    @Test
    void agendaVisualizaTurnoCancelado() throws SQLException {

        Turno turno = crearTurno(
                LocalTime.of(17, 0),
                LocalTime.of(17, 30)
        );

        assertTrue(
                turnoService.registrarTurno(turno),
                "El turno debe registrarse correctamente"
        );

        Turno turnoRegistrado =
                turnoService.obtenerPorId(
                        turno.getIdTurno()
                );

        assertNotNull(turnoRegistrado);

        turnoService.cambiarEstado(
                turnoRegistrado,
                EstadoTurno.CANCELADO,
                "Cancelación manual"
        );

        List<Turno> agenda =
                turnoService.obtenerAgenda(
                        FECHA_TEST,
                        ID_ESTILISTA
                );

        Turno cancelado = agenda.stream()
                .filter(t ->
                        t.getIdTurno() == turno.getIdTurno()
                )
                .findFirst()
                .orElse(null);

        assertNotNull(
                cancelado,
                "La agenda debe seguir mostrando el turno cancelado"
        );

        assertEquals(
                EstadoTurno.CANCELADO,
                cancelado.getEstadoLogico(),
                "La agenda debe mostrar el turno con estado Cancelado"
        );
    }

    private Turno crearTurno(
            LocalTime horaInicio,
            LocalTime horaFin) {

        Turno turno = new Turno();

        turno.setIdCliente(ID_CLIENTE);
        turno.setIdEmpleado(ID_ESTILISTA);
        turno.setFecha(FECHA_TEST);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        turno.setEstadoLogico(EstadoTurno.PENDIENTE);

        Servicio servicio = new Servicio();
        servicio.setIdServicio(ID_SERVICIO);
        servicio.setNombreServicio("Corte de pelo");

        turno.addServicio(servicio);

        return turno;
    }

    private boolean contieneHorario(
            List<claseslogicas.BloqueDisponible> bloques,
            LocalTime horaInicio,
            LocalTime horaFin) {

        return bloques.stream()
                .anyMatch(bloque ->
                        bloque.getHoraInicio().equals(horaInicio)
                                && bloque.getHoraFin().equals(horaFin)
                );
    }
}