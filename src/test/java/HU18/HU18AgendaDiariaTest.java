package HU18;

import claseslogicas.EstadoTurno;
import claseslogicas.Turno;
import dao.ConexionBD;
import service.TurnoService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU18AgendaDiariaTest {

    private final TurnoService turnoService = new TurnoService();

    private int turnoCreado1 = -1;
    private int turnoCreado2 = -1;

    /*
     * Se utiliza una fecha futura para evitar interferencias
     * con datos reales de la BD de testing.
     */
    private final LocalDate fechaPrueba = LocalDate.now().plusYears(1);

    @BeforeEach
    void preparar() throws SQLException {
        limpiarTurnosDePrueba();
    }

    @AfterEach
    void limpiar() throws SQLException {
        limpiarTurnosDePrueba();
    }

    @Test
    void seleccionarFechaMuestraTurnosDelDia() throws SQLException {

        crearTurno(
                10,
                3,
                fechaPrueba,
                LocalTime.of(10, 0)
        );

        List<Turno> agenda =
                turnoService.obtenerAgenda(fechaPrueba, null);

        assertTrue(
                agenda.stream()
                        .anyMatch(t -> t.getIdTurno() == turnoCreado1),
                "La agenda debe mostrar los turnos correspondientes a la fecha seleccionada."
        );
    }

    @Test
    void filtrarAgendaPorEstilista() throws SQLException {

        crearTurno(
                10,
                3,
                fechaPrueba,
                LocalTime.of(10, 0)
        );

        crearTurno(
                10,
                4,
                fechaPrueba,
                LocalTime.of(11, 0)
        );

        List<Turno> agendaEstilista3 =
                turnoService.obtenerAgenda(fechaPrueba, 3);

        assertTrue(
                agendaEstilista3.stream()
                        .anyMatch(t -> t.getIdTurno() == turnoCreado1),
                "El filtro debe mostrar los turnos del estilista seleccionado."
        );

        assertFalse(
                agendaEstilista3.stream()
                        .anyMatch(t -> t.getIdTurno() == turnoCreado2),
                "El filtro no debe mostrar turnos de otro estilista."
        );
    }

    @Test
    void agendaMuestraClienteServicioHorarioYProfesional()
            throws SQLException {

        crearTurno(
                10,
                3,
                fechaPrueba,
                LocalTime.of(12, 0)
        );

        List<Turno> agenda =
                turnoService.obtenerAgenda(fechaPrueba, 3);

        Turno turno = agenda.stream()
                .filter(t -> t.getIdTurno() == turnoCreado1)
                .findFirst()
                .orElse(null);

        assertNotNull(
                turno,
                "El turno debe estar presente en la agenda."
        );

        // Cliente
        assertEquals(10, turno.getIdCliente());
        assertNotNull(turno.getCliente());

        // Profesional
        assertEquals(3, turno.getIdEmpleado());
        assertNotNull(turno.getEmpleado());

        // Horario
        assertEquals(
                LocalTime.of(12, 0),
                turno.getHoraInicio()
        );

        assertEquals(
                LocalTime.of(12, 30),
                turno.getHoraFin()
        );

        // Servicio
        assertNotNull(turno.getServicios());

        assertFalse(
                turno.getServicios().isEmpty(),
                "El turno debe mostrar el servicio asociado."
        );

        assertEquals(
                "Corte de pelo",
                turno.getServicios()
                        .get(0)
                        .getNombreServicio()
        );
    }

    @Test
    void agendaMuestraEstadoActualDelTurno()
            throws SQLException {

        crearTurno(
                10,
                3,
                fechaPrueba,
                LocalTime.of(13, 0)
        );

        List<Turno> agenda =
                turnoService.obtenerAgenda(fechaPrueba, 3);

        Turno turno = agenda.stream()
                .filter(t -> t.getIdTurno() == turnoCreado1)
                .findFirst()
                .orElseThrow();

        assertEquals(
                EstadoTurno.PENDIENTE,
                turno.getEstadoTurno()
        );
    }

    @Test
    void confirmarLlegadaActualizaEstadoDelTurno()
            throws SQLException {

        Turno turno = crearTurno(
                10,
                3,
                fechaPrueba,
                LocalTime.of(14, 0)
        );

        turnoService.cambiarEstado(
                turno,
                EstadoTurno.CONFIRMADO,
                "Turno confirmado por recepción"
        );

        Turno turnoActualizado =
                turnoService.obtenerPorId(turnoCreado1);

        assertNotNull(turnoActualizado);

        assertEquals(
                EstadoTurno.CONFIRMADO,
                turnoActualizado.getEstadoTurno()
        );

        /*
         * Verificamos nuevamente desde la agenda,
         * no solamente desde el objeto modificado.
         */
        List<Turno> agenda =
                turnoService.obtenerAgenda(fechaPrueba, 3);

        Turno turnoEnAgenda = agenda.stream()
                .filter(t -> t.getIdTurno() == turnoCreado1)
                .findFirst()
                .orElseThrow();

        assertEquals(
                EstadoTurno.CONFIRMADO,
                turnoEnAgenda.getEstadoTurno()
        );
    }

    @Test
    void cancelarDesdeAgendaMantieneTurnoRegistradoComoCancelado()
            throws SQLException {

        Turno turno = crearTurno(
                10,
                3,
                fechaPrueba,
                LocalTime.of(15, 0)
        );

        turnoService.cambiarEstado(
                turno,
                EstadoTurno.CANCELADO,
                "Cancelación manual"
        );

        Turno turnoActualizado =
                turnoService.obtenerPorId(turnoCreado1);

        assertNotNull(turnoActualizado);

        assertEquals(
                EstadoTurno.CANCELADO,
                turnoActualizado.getEstadoTurno()
        );

        /*
         * La agenda debe seguir mostrando el turno,
         * pero con estado Cancelado.
         */
        List<Turno> agenda =
                turnoService.obtenerAgenda(fechaPrueba, 3);

        Turno turnoEnAgenda = agenda.stream()
                .filter(t -> t.getIdTurno() == turnoCreado1)
                .findFirst()
                .orElseThrow();

        assertEquals(
                EstadoTurno.CANCELADO,
                turnoEnAgenda.getEstadoTurno()
        );
    }

    @Test
    void diaSinTurnosDevuelveListaVacia()
            throws SQLException {

        LocalDate diaSinTurnos =
                LocalDate.of(2099, 1, 1);

        List<Turno> agenda =
                turnoService.obtenerAgenda(
                        diaSinTurnos,
                        null
                );

        assertNotNull(agenda);

        assertTrue(
                agenda.isEmpty(),
                "Si no existen turnos para la fecha seleccionada, la agenda debe quedar vacía."
        );
    }

    /**
     * Crea un turno de prueba utilizando los datos existentes
     * en la BD de testing:
     *
     * Cliente 10
     * Estilista 3 / 4
     * Servicio 1 = Corte de pelo
     */
    private Turno crearTurno(
            int idCliente,
            int idEmpleado,
            LocalDate fecha,
            LocalTime horaInicio
    ) throws SQLException {

        LocalTime horaFin =
                horaInicio.plusMinutes(30);

        String sqlTurno =
                "INSERT INTO turno " +
                        "(id_cliente, id_empleado, id_estado, fecha, " +
                        "hora_inicio, hora_fin, observaciones, fecha_creacion) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                        "RETURNING id_turno";

        try (
                Connection conn = ConexionBD.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sqlTurno)
        ) {

            ps.setInt(
                    1,
                    idCliente
            );

            ps.setInt(
                    2,
                    idEmpleado
            );

            ps.setInt(
                    3,
                    EstadoTurno.PENDIENTE.getId()
            );

            ps.setDate(
                    4,
                    java.sql.Date.valueOf(fecha)
            );

            ps.setTime(
                    5,
                    java.sql.Time.valueOf(horaInicio)
            );

            ps.setTime(
                    6,
                    java.sql.Time.valueOf(horaFin)
            );

            ps.setString(
                    7,
                    "Prueba HU18"
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next(),
                        "No se pudo obtener el ID del turno de prueba."
                );

                int idTurno =
                        rs.getInt(1);

                // Servicio 1 existente en la BD de testing:
                // Corte de pelo
                try (
                        PreparedStatement psServicio =
                                conn.prepareStatement(
                                        "INSERT INTO turno_servicios " +
                                                "(id_turno, id_servicio) " +
                                                "VALUES (?, ?)"
                                )
                ) {

                    psServicio.setInt(
                            1,
                            idTurno
                    );

                    psServicio.setInt(
                            2,
                            1
                    );

                    psServicio.executeUpdate();
                }

                if (turnoCreado1 == -1) {
                    turnoCreado1 = idTurno;
                } else {
                    turnoCreado2 = idTurno;
                }

                return turnoService.obtenerPorId(
                        idTurno
                );
            }
        }
    }

    /**
     * Elimina solamente los registros creados
     * específicamente por estos tests.
     */
    private void limpiarTurnosDePrueba()
            throws SQLException {

        try (Connection conn =
                     ConexionBD.getConnection()) {

            conn.setAutoCommit(false);

            try (
                    PreparedStatement psDetalle =
                            conn.prepareStatement(
                                    "DELETE FROM turno_servicios " +
                                            "WHERE id_turno IN " +
                                            "(SELECT id_turno FROM turno " +
                                            "WHERE observaciones = 'Prueba HU18')"
                            )
            ) {
                psDetalle.executeUpdate();
            }

            try (
                    PreparedStatement psTurno =
                            conn.prepareStatement(
                                    "DELETE FROM turno " +
                                            "WHERE observaciones = 'Prueba HU18'"
                            )
            ) {
                psTurno.executeUpdate();
            }

            conn.commit();

        } finally {

            turnoCreado1 = -1;
            turnoCreado2 = -1;
        }
    }
}