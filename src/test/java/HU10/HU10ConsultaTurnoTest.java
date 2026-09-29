package hu10;

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

class HU10ConsultaTurnoTest {

    private static final int ID_CLIENTE = 10;
    private static final int ID_ESTILISTA = 3;
    private static final int ID_SERVICIO = 1;

    private static final LocalDate FECHA_TEST =
            LocalDate.of(2026, 9, 28);

    private final TurnoService turnoService =
            new TurnoService();

    @Test
    void consultarTurnosPorFecha() throws SQLException {

        Turno turno = crearTurno();

        try {

            assertTrue(
                    turnoService.registrarTurno(turno),
                    "El turno debe registrarse correctamente"
            );

            List<Turno> turnos =
                    turnoService.obtenerAgenda(
                            FECHA_TEST,
                            ID_ESTILISTA
                    );

            assertNotNull(turnos);

            assertTrue(
                    turnos.stream()
                            .anyMatch(t ->
                                    t.getIdTurno() == turno.getIdTurno()
                            ),
                    "La consulta por fecha debe devolver el turno registrado"
            );

        } finally {

            cancelarTurno(turno);
        }
    }

    @Test
    void consultarTurnoMuestraInformacionCompleta()
            throws SQLException {

        Turno turno = crearTurno();

        try {

            assertTrue(
                    turnoService.registrarTurno(turno),
                    "El turno debe registrarse correctamente"
            );

            List<Turno> turnos =
                    turnoService.obtenerAgenda(
                            FECHA_TEST,
                            ID_ESTILISTA
                    );

            Turno consultado = turnos.stream()
                    .filter(t ->
                            t.getIdTurno() == turno.getIdTurno()
                    )
                    .findFirst()
                    .orElse(null);

            assertNotNull(
                    consultado,
                    "El turno debe encontrarse en la agenda"
            );

            // CLIENTE
            assertEquals(
                    ID_CLIENTE,
                    consultado.getIdCliente(),
                    "Debe mostrar el cliente del turno"
            );

            assertNotNull(
                    consultado.getCliente(),
                    "Debe cargar la información del cliente"
            );

            // PROFESIONAL
            assertEquals(
                    ID_ESTILISTA,
                    consultado.getIdEmpleado(),
                    "Debe mostrar el profesional del turno"
            );

            assertNotNull(
                    consultado.getEmpleado(),
                    "Debe cargar la información del profesional"
            );

            // FECHA
            assertEquals(
                    FECHA_TEST,
                    consultado.getFecha(),
                    "Debe mostrar la fecha del turno"
            );

            // HORARIO
            assertEquals(
                    LocalTime.of(13, 0),
                    consultado.getHoraInicio(),
                    "Debe mostrar la hora de inicio"
            );

            assertEquals(
                    LocalTime.of(13, 30),
                    consultado.getHoraFin(),
                    "Debe mostrar la hora de finalización"
            );

            // ESTADO
            assertEquals(
                    EstadoTurno.PENDIENTE,
                    consultado.getEstadoLogico(),
                    "El turno debe conservar el estado Pendiente"
            );

            // SERVICIOS
            assertNotNull(
                    consultado.getServicios(),
                    "Debe cargar los servicios del turno"
            );

            assertFalse(
                    consultado.getServicios().isEmpty(),
                    "El turno debe mostrar los servicios seleccionados"
            );

            assertTrue(
                    consultado.getServicios()
                            .stream()
                            .anyMatch(s ->
                                    s.getIdServicio() == ID_SERVICIO
                            ),
                    "Debe mostrar el servicio seleccionado"
            );

        } finally {

            cancelarTurno(turno);
        }
    }

    @Test
    void consultarFechaSinTurnos()
            throws SQLException {

        LocalDate fechaSinTurnos =
                LocalDate.of(2099, 1, 1);

        List<Turno> turnos =
                turnoService.obtenerAgenda(
                        fechaSinTurnos,
                        ID_ESTILISTA
                );

        assertNotNull(turnos);

        assertTrue(
                turnos.isEmpty(),
                "Una fecha sin turnos debe devolver una lista vacía"
        );
    }

    private Turno crearTurno() {

        Turno turno = new Turno();

        turno.setIdCliente(ID_CLIENTE);
        turno.setIdEmpleado(ID_ESTILISTA);
        turno.setFecha(FECHA_TEST);
        turno.setHoraInicio(LocalTime.of(13, 0));
        turno.setHoraFin(LocalTime.of(13, 30));
        turno.setEstadoLogico(EstadoTurno.PENDIENTE);

        Servicio servicio = new Servicio();
        servicio.setIdServicio(ID_SERVICIO);
        servicio.setNombreServicio("Corte de pelo");

        turno.addServicio(servicio);

        return turno;
    }

    private void cancelarTurno(Turno turno)
            throws SQLException {

        if (turno != null && turno.getIdTurno() > 0) {

            turnoService.cambiarEstado(
                    turno,
                    EstadoTurno.CANCELADO,
                    "Limpieza de datos de prueba HU10"
            );
        }
    }
}