package hu13;

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

class HU13GestionEstadosTurnosTest {

    private static final int ID_CLIENTE = 10;
    private static final int ID_ESTILISTA = 3;
    private static final int ID_SERVICIO = 1;

    private final TurnoService turnoService =
            new TurnoService();

    @Test
    void visualizarEstadoActualDelTurno()
            throws SQLException {

        Turno turno = crearTurno(
                LocalDate.of(2026, 10, 20),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno),
                    "El turno debe registrarse correctamente"
            );

            Turno consultado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(consultado);

            assertEquals(
                    EstadoTurno.PENDIENTE,
                    consultado.getEstadoLogico(),
                    "Debe visualizarse el estado actual del turno"
            );

        } finally {

            cancelarTurno(turno);
        }
    }

    @Test
    void actualizarTurnoDePendienteAConfirmado()
            throws SQLException {

        Turno turno = crearTurno(
                LocalDate.of(2026, 10, 21),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno)
            );

            Turno registrado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(registrado);

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CONFIRMADO,
                    "Confirmación del turno"
            );

            assertEquals(
                    EstadoTurno.CONFIRMADO,
                    registrado.getEstadoLogico(),
                    "El turno debe pasar de Pendiente a Confirmado"
            );

        } finally {

            cancelarTurnoSiCorresponde(turno);
        }
    }

    @Test
    void actualizarTurnoDeConfirmadoAFinalizado()
            throws SQLException {

        Turno turno = crearTurno(
                LocalDate.of(2026, 10, 22),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno)
            );

            Turno registrado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(registrado);

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CONFIRMADO,
                    "Confirmación del turno"
            );

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.FINALIZADO,
                    "Finalización de la atención"
            );

            assertEquals(
                    EstadoTurno.FINALIZADO,
                    registrado.getEstadoLogico(),
                    "El turno debe pasar de Confirmado a Finalizado"
            );

        } finally {

            // FINALIZADO es un estado terminal.
            // No se modifica ni se elimina.
        }
    }

    @Test
    void actualizarTurnoDePendienteACancelado()
            throws SQLException {

        Turno turno = crearTurno(
                LocalDate.of(2026, 10, 23),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno)
            );

            Turno registrado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(registrado);

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CANCELADO,
                    "Cancelación del turno"
            );

            assertEquals(
                    EstadoTurno.CANCELADO,
                    registrado.getEstadoLogico(),
                    "El turno debe pasar de Pendiente a Cancelado"
            );

        } finally {

            // CANCELADO es un estado terminal.
        }
    }

    @Test
    void finalizarTurnoMantieneEstadoFinalizado()
            throws SQLException {

        Turno turno = crearTurno(
                LocalDate.of(2026, 10, 26),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno)
            );

            Turno registrado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(registrado);

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CONFIRMADO,
                    "Confirmación del turno"
            );

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.FINALIZADO,
                    "Finalización de la atención"
            );

            Turno consultado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(consultado);

            assertEquals(
                    EstadoTurno.FINALIZADO,
                    consultado.getEstadoLogico(),
                    "El turno debe quedar registrado con estado Finalizado"
            );

        } finally {

            // FINALIZADO no se modifica.
        }
    }

    @Test
    void turnoFinalizadoNoPuedeModificarEstado()
            throws SQLException {

        Turno turno = crearTurno(
                LocalDate.of(2026, 10, 27),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno)
            );

            Turno registrado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(registrado);

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CONFIRMADO,
                    "Confirmación del turno"
            );

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.FINALIZADO,
                    "Finalización de la atención"
            );

            assertThrows(
                    Exception.class,
                    () -> turnoService.cambiarEstado(
                            registrado,
                            EstadoTurno.CANCELADO,
                            "Intento de modificar turno finalizado"
                    ),
                    "Un turno Finalizado no debe poder modificarse"
            );

        } finally {

            // FINALIZADO es terminal.
        }
    }

    @Test
    void cancelarTurnoLiberaHorario()
            throws SQLException {

        LocalDate fecha =
                LocalDate.of(2026, 10, 5);

        LocalTime horaInicio =
                LocalTime.of(14, 0);

        LocalTime horaFin =
                LocalTime.of(14, 30);

        Turno turno = crearTurno(
                fecha,
                horaInicio,
                horaFin
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(turno)
            );

            List<claseslogicas.BloqueDisponible> antes =
                    turnoService.buscarDisponibilidad(
                            fecha,
                            30,
                            ID_ESTILISTA
                    );

            assertFalse(
                    contieneHorario(
                            antes,
                            horaInicio,
                            horaFin
                    ),
                    "El horario ocupado no debe estar disponible"
            );

            Turno registrado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(registrado);

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CANCELADO,
                    "Cancelación del turno"
            );

            List<claseslogicas.BloqueDisponible> despues =
                    turnoService.buscarDisponibilidad(
                            fecha,
                            30,
                            ID_ESTILISTA
                    );

            assertTrue(
                    contieneHorario(
                            despues,
                            horaInicio,
                            horaFin
                    ),
                    "Al cancelar el turno, el horario debe quedar disponible nuevamente"
            );

        } finally {

            // CANCELADO queda registrado.
        }
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

    private void cancelarTurnoSiCorresponde(
            Turno turno) throws SQLException {

        if (turno == null || turno.getIdTurno() <= 0) {
            return;
        }

        Turno registrado =
                turnoService.obtenerPorId(
                        turno.getIdTurno()
                );

        if (registrado == null) {
            return;
        }

        EstadoTurno estado =
                registrado.getEstadoLogico();

        if (estado == EstadoTurno.PENDIENTE
                || estado == EstadoTurno.CONFIRMADO) {

            turnoService.cambiarEstado(
                    registrado,
                    EstadoTurno.CANCELADO,
                    "Limpieza de datos de prueba HU13"
            );
        }
    }

    private void cancelarTurno(
            Turno turno) throws SQLException {

        cancelarTurnoSiCorresponde(turno);
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