package hu12;

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

class HU12ListadoTurnosTest {

    private static final int ID_CLIENTE = 10;
    private static final int ID_ESTILISTA = 3;
    private static final int ID_SERVICIO = 1;

    private final TurnoService turnoService =
            new TurnoService();

    @Test
    void generarListadoDeTurnos() throws SQLException {

        LocalDate fecha =
                LocalDate.of(2026, 10, 12);

        Turno turno = crearTurno(
                fecha,
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno),
                    "El turno debe registrarse correctamente"
            );

            List<Turno> listado =
                    turnoService.obtenerTurnosPorPeriodo(
                            fecha,
                            fecha
                    );

            assertNotNull(
                    listado,
                    "El listado de turnos no debe ser nulo"
            );

            assertTrue(
                    listado.stream()
                            .anyMatch(t ->
                                    t.getIdTurno()
                                            == turno.getIdTurno()
                            ),
                    "El listado debe incluir el turno registrado"
            );

        } finally {

            cancelarTurno(turno);
        }
    }

    @Test
    void consultarListadoPorPeriodo() throws SQLException {

        LocalDate fechaDentroPeriodo =
                LocalDate.of(2026, 10, 13);

        LocalDate desde =
                LocalDate.of(2026, 10, 13);

        LocalDate hasta =
                LocalDate.of(2026, 10, 14);

        Turno turnoDentro =
                crearTurno(
                        fechaDentroPeriodo,
                        LocalTime.of(11, 0),
                        LocalTime.of(11, 30)
                );

        Turno turnoFuera =
                crearTurno(
                        LocalDate.of(2026, 10, 15),
                        LocalTime.of(11, 0),
                        LocalTime.of(11, 30)
                );

        try {

            assertTrue(
                    turnoService.registrarTurno(turnoDentro)
            );

            assertTrue(
                    turnoService.registrarTurno(turnoFuera)
            );

            List<Turno> listado =
                    turnoService.obtenerTurnosPorPeriodo(
                            desde,
                            hasta
                    );

            assertNotNull(listado);

            assertTrue(
                    listado.stream()
                            .anyMatch(t ->
                                    t.getIdTurno()
                                            == turnoDentro.getIdTurno()
                            ),
                    "El listado debe incluir el turno dentro del período"
            );

            assertFalse(
                    listado.stream()
                            .anyMatch(t ->
                                    t.getIdTurno()
                                            == turnoFuera.getIdTurno()
                            ),
                    "El listado no debe incluir turnos fuera del período"
            );

        } finally {

            cancelarTurno(turnoDentro);
            cancelarTurno(turnoFuera);
        }
    }

    @Test
    void listadoMuestraDatosDelTurno()
            throws SQLException {

        LocalDate fecha =
                LocalDate.of(2026, 10, 16);

        Turno turno = crearTurno(
                fecha,
                LocalTime.of(12, 0),
                LocalTime.of(12, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno),
                    "El turno debe registrarse correctamente"
            );

            List<Turno> listado =
                    turnoService.obtenerTurnosPorPeriodo(
                            fecha,
                            fecha
                    );

            Turno encontrado = listado.stream()
                    .filter(t ->
                            t.getIdTurno()
                                    == turno.getIdTurno()
                    )
                    .findFirst()
                    .orElse(null);

            assertNotNull(
                    encontrado,
                    "El turno debe aparecer en el listado"
            );

            // Cliente
            assertEquals(
                    ID_CLIENTE,
                    encontrado.getIdCliente(),
                    "Debe mostrar el cliente del turno"
            );

            assertNotNull(
                    encontrado.getCliente(),
                    "Debe cargar la información del cliente"
            );

            // Profesional
            assertEquals(
                    ID_ESTILISTA,
                    encontrado.getIdEmpleado(),
                    "Debe mostrar el profesional del turno"
            );

            assertNotNull(
                    encontrado.getEmpleado(),
                    "Debe cargar la información del profesional"
            );

            // Fecha
            assertEquals(
                    fecha,
                    encontrado.getFecha(),
                    "Debe mostrar la fecha del turno"
            );

            // Hora
            assertEquals(
                    LocalTime.of(12, 0),
                    encontrado.getHoraInicio(),
                    "Debe mostrar la hora de inicio"
            );

            assertEquals(
                    LocalTime.of(12, 30),
                    encontrado.getHoraFin(),
                    "Debe mostrar la hora de finalización"
            );

            // Estado
            assertNotNull(
                    encontrado.getEstadoLogico(),
                    "Debe mostrar el estado del turno"
            );

            assertEquals(
                    EstadoTurno.PENDIENTE,
                    encontrado.getEstadoLogico(),
                    "El turno recién registrado debe estar Pendiente"
            );

        } finally {

            cancelarTurno(turno);
        }
    }

    @Test
    void listadoIncluyeServicioDelTurno()
            throws SQLException {

        LocalDate fecha =
                LocalDate.of(2026, 10, 19);

        Turno turno = crearTurno(
                fecha,
                LocalTime.of(13, 0),
                LocalTime.of(13, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno),
                    "El turno debe registrarse correctamente"
            );

            List<Turno> listado =
                    turnoService.obtenerTurnosPorPeriodo(
                            fecha,
                            fecha
                    );

            Turno encontrado = listado.stream()
                    .filter(t ->
                            t.getIdTurno()
                                    == turno.getIdTurno()
                    )
                    .findFirst()
                    .orElse(null);

            assertNotNull(encontrado);

            assertNotNull(
                    encontrado.getServicios(),
                    "El listado debe cargar los servicios del turno"
            );

            assertFalse(
                    encontrado.getServicios().isEmpty(),
                    "El turno debe tener servicios asociados"
            );

            assertTrue(
                    encontrado.getServicios()
                            .stream()
                            .anyMatch(s ->
                                    s.getIdServicio()
                                            == ID_SERVICIO
                            ),
                    "El listado debe incluir el servicio seleccionado"
            );

        } finally {

            cancelarTurno(turno);
        }
    }

    @Test
    void consultarPeriodoSinTurnos()
            throws SQLException {

        LocalDate desde =
                LocalDate.of(2099, 1, 1);

        LocalDate hasta =
                LocalDate.of(2099, 1, 31);

        List<Turno> listado =
                turnoService.obtenerTurnosPorPeriodo(
                        desde,
                        hasta
                );

        assertNotNull(
                listado,
                "El listado no debe ser nulo"
        );

        assertTrue(
                listado.isEmpty(),
                "Un período sin turnos debe devolver una lista vacía"
        );
    }

    private Turno crearTurno(
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        Turno turno = new Turno();

        turno.setIdCliente(ID_CLIENTE);
        turno.setIdEmpleado(ID_ESTILISTA);
        turno.setFecha(fecha);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        turno.setEstadoLogico(
                EstadoTurno.PENDIENTE
        );

        Servicio servicio = new Servicio();

        servicio.setIdServicio(ID_SERVICIO);
        servicio.setNombreServicio("Corte de pelo");

        turno.addServicio(servicio);

        return turno;
    }

    private void cancelarTurno(Turno turno)
            throws SQLException {

        if (turno == null || turno.getIdTurno() <= 0) {
            return;
        }

        Turno registrado =
                turnoService.obtenerPorId(
                        turno.getIdTurno()
                );

        if (registrado != null
                && registrado.getEstadoLogico()
                != EstadoTurno.CANCELADO) {

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CANCELADO,
                    "Limpieza de datos de prueba HU12"
            );
        }
    }
}