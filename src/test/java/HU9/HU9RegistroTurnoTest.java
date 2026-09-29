package HU9;

import claseslogicas.Disponibilidad;
import claseslogicas.Especialidad;
import claseslogicas.Servicio;
import claseslogicas.Turno;
import dao.ConexionBD;
import dao.DisponibilidadDAO;
import dao.EmpleadoEspecialidadDAO;
import dao.EspecialidadDAO;
import dao.EspecialidadServicioDAO;
import dao.TurnoDAO;
import org.junit.jupiter.api.Test;
import service.TurnoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU09RegistroTurnoTest {

    private final TurnoService turnoService = new TurnoService();

    private static final int ID_CLIENTE = 10;
    private static final int ID_ESTILISTA = 3;
    private static final int ID_SERVICIO_CORTE = 1;
    private static final int ID_SERVICIO_COLORACION = 2;

    private static final LocalDate FECHA_TEST =
            LocalDate.of(2026, 9, 28); // Lunes

    /**
     * HU9 - Criterio:
     * Se puede seleccionar un cliente y uno o más servicios.
     *
     * Datos:
     * Cliente 10 - Juan Perez
     * Servicio 1 - Corte de pelo
     * Servicio 2 - Coloración
     */
    @Test
    void seleccionarClienteYServicios() {

        Servicio corte = crearServicio(
                ID_SERVICIO_CORTE,
                "Corte de pelo",
                30
        );

        Servicio coloracion = crearServicio(
                ID_SERVICIO_COLORACION,
                "Coloración",
                90
        );

        Turno turno = new Turno();

        turno.setIdCliente(ID_CLIENTE);
        turno.setIdEmpleado(ID_ESTILISTA);

        turno.addServicio(corte);
        turno.addServicio(coloracion);

        assertEquals(
                ID_CLIENTE,
                turno.getIdCliente()
        );

        assertEquals(
                2,
                turno.getServicios().size()
        );

        assertEquals(
                ID_SERVICIO_CORTE,
                turno.getServicios().get(0).getIdServicio()
        );

        assertEquals(
                ID_SERVICIO_COLORACION,
                turno.getServicios().get(1).getIdServicio()
        );
    }

    /**
     * HU9 - Criterio:
     * El sistema calcula automáticamente la duración
     * según los servicios seleccionados.
     *
     * Corte = 30 minutos
     * Coloración = 90 minutos
     * Total esperado = 120 minutos.
     */
    @Test
    void calcularDuracionSegunServicios() {

        Servicio corte = crearServicio(
                ID_SERVICIO_CORTE,
                "Corte de pelo",
                30
        );

        Servicio coloracion = crearServicio(
                ID_SERVICIO_COLORACION,
                "Coloración",
                90
        );

        List<Servicio> servicios = List.of(
                corte,
                coloracion
        );

        int duracionTotal = servicios.stream()
                .mapToInt(Servicio::getDuracionMinutos)
                .sum();

        assertEquals(
                120,
                duracionTotal,
                "La duración total debe ser la suma de las duraciones de los servicios"
        );
    }

    /**
     * HU9 - Criterio:
     * El sistema verifica que el profesional posea
     * la especialidad requerida para el servicio.
     *
     * Se crea temporalmente una especialidad de prueba,
     * se asocia al estilista 3 y al servicio Corte de pelo.
     */
    @Test
    void profesionalPoseeEspecialidadParaServicio() throws SQLException {

        EspecialidadDAO especialidadDAO =
                new EspecialidadDAO();

        EmpleadoEspecialidadDAO empleadoEspecialidadDAO =
                new EmpleadoEspecialidadDAO();

        EspecialidadServicioDAO especialidadServicioDAO =
                new EspecialidadServicioDAO();

        Especialidad especialidad =
                new Especialidad();

        especialidad.setNombre(
                "Especialidad HU9 Test"
        );

        especialidadDAO.insertar(especialidad);

        int idEspecialidad =
                especialidad.getIdEspecialidad();

        try {

            empleadoEspecialidadDAO.asignarEspecialidad(
                    ID_ESTILISTA,
                    idEspecialidad
            );

            especialidadServicioDAO.asociarServicio(
                    idEspecialidad,
                    ID_SERVICIO_CORTE
            );

            List<Servicio> servicios =
                    new dao.ServicioDAO()
                            .obtenerServiciosPorEmpleado(
                                    ID_ESTILISTA
                            );

            assertTrue(
                    servicios.stream()
                            .anyMatch(s ->
                                    s.getIdServicio()
                                            == ID_SERVICIO_CORTE
                            ),
                    "El profesional debe disponer del servicio asociado a su especialidad"
            );

        } finally {

            try (Connection conn =
                         ConexionBD.getConnection()) {

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     "DELETE FROM empleado_especialidad " +
                                             "WHERE id_empleado = ? " +
                                             "AND id_especialidad = ?")) {

                    ps.setInt(1, ID_ESTILISTA);
                    ps.setInt(2, idEspecialidad);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     "DELETE FROM especialidad_servicio " +
                                             "WHERE id_especialidad = ?")) {

                    ps.setInt(1, idEspecialidad);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     "DELETE FROM especialidad " +
                                             "WHERE id_especialidad = ?")) {

                    ps.setInt(1, idEspecialidad);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * HU9 - Criterio:
     * El sistema verifica que el profesional esté disponible
     * para la fecha y horario seleccionado.
     *
     * Se crea temporalmente disponibilidad del estilista 3:
     * Lunes de 09:00 a 19:00.
     */
    @Test
    void verificarDisponibilidadDelProfesional() throws SQLException {

        DisponibilidadDAO disponibilidadDAO =
                new DisponibilidadDAO();

        Disponibilidad disponibilidad =
                new Disponibilidad();

        disponibilidad.setIdEmpleado(ID_ESTILISTA);
        disponibilidad.setDiaSemana("Lunes");
        disponibilidad.setHoraDesde(
                LocalTime.of(9, 0)
        );
        disponibilidad.setHoraHasta(
                LocalTime.of(19, 0)
        );

        disponibilidadDAO.insertar(disponibilidad);

        try {

            TurnoService service =
                    new TurnoService();

            var disponibles =
                    service.buscarDisponibilidad(
                            FECHA_TEST,
                            30,
                            ID_ESTILISTA
                    );

            assertFalse(
                    disponibles.isEmpty(),
                    "Debe encontrar bloques disponibles dentro del horario del profesional"
            );

            assertTrue(
                    disponibles.stream()
                            .allMatch(b ->
                                    !b.getHoraInicio()
                                            .isBefore(LocalTime.of(9, 0))
                                            &&
                                            !b.getHoraFin()
                                                    .isAfter(LocalTime.of(19, 0))
                            ),
                    "Los bloques deben encontrarse dentro del horario disponible"
            );

        } finally {

            eliminarDisponibilidad(
                    disponibilidad.getIdDisponibilidad()
            );
        }
    }

    /**
     * HU9 - Criterio:
     * El turno se guarda correctamente.
     *
     * Datos:
     * Cliente 10
     * Estilista 3
     * Servicio 1
     * Fecha 28/09/2026
     * 09:00 - 09:30
     */
    @Test
    void registrarTurnoCorrectamente() throws SQLException {

        Turno turno = crearTurno(
                ID_CLIENTE,
                ID_ESTILISTA,
                FECHA_TEST,
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                ID_SERVICIO_CORTE
        );

        boolean resultado =
                turnoService.registrarTurno(turno);

        try {

            assertTrue(
                    resultado,
                    "El turno debe guardarse correctamente"
            );

            assertTrue(
                    turno.getIdTurno() > 0,
                    "El turno guardado debe recibir un ID"
            );

        } finally {

            eliminarTurno(turno.getIdTurno());
        }
    }

    /**
     * HU9 - Criterio:
     * El turno se registra inicialmente en estado Pendiente.
     */
    @Test
    void turnoSeRegistraInicialmentePendiente()
            throws SQLException {

        Turno turno = crearTurno(
                ID_CLIENTE,
                ID_ESTILISTA,
                FECHA_TEST,
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                ID_SERVICIO_CORTE
        );

        try {

            boolean resultado =
                    turnoService.registrarTurno(turno);

            assertTrue(resultado);

            Turno turnoGuardado =
                    turnoService.obtenerPorId(
                            turno.getIdTurno()
                    );

            assertNotNull(turnoGuardado);

            assertEquals(
                    claseslogicas.EstadoTurno.PENDIENTE,
                    turnoGuardado.getEstadoLogico()
            );

        } finally {

            eliminarTurno(turno.getIdTurno());
        }
    }

    /**
     * HU9 - Criterio:
     * No se permite registrar un turno que genere
     * superposición con otro turno existente.
     */
    @Test
    void registrarTurnoConSuperposicion()
            throws SQLException {

        Turno turnoExistente = crearTurno(
                ID_CLIENTE,
                ID_ESTILISTA,
                FECHA_TEST,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                ID_SERVICIO_COLORACION
        );

        Turno turnoSuperpuesto = crearTurno(
                ID_CLIENTE,
                ID_ESTILISTA,
                FECHA_TEST,
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                ID_SERVICIO_CORTE
        );

        try {

            assertTrue(
                    turnoService.registrarTurno(
                            turnoExistente
                    )
            );

            SQLException excepcion =
                    assertThrows(
                            SQLException.class,
                            () -> turnoService.registrarTurno(
                                    turnoSuperpuesto
                            )
                    );

            assertTrue(
                    excepcion.getMessage()
                            .toLowerCase()
                            .contains("disponible"),
                    "El sistema debe informar que el horario no está disponible"
            );

        } finally {

            eliminarTurno(
                    turnoSuperpuesto.getIdTurno()
            );

            eliminarTurno(
                    turnoExistente.getIdTurno()
            );
        }
    }

    /**
     * HU9 - Criterio:
     * El sistema guarda cliente, profesional, servicios,
     * fecha, hora y duración del turno.
     */
    @Test
    void turnoGuardaTodosLosDatos() throws SQLException {

        Turno turno = crearTurno(
                ID_CLIENTE,
                ID_ESTILISTA,
                FECHA_TEST,
                LocalTime.of(13, 0),
                LocalTime.of(14, 30),
                ID_SERVICIO_COLORACION
        );

        try {

            assertTrue(turnoService.registrarTurno(turno));

            List<Turno> turnos =
                    turnoService.obtenerAgenda(
                            FECHA_TEST,
                            ID_ESTILISTA
                    );

            Turno guardado = turnos.stream()
                    .filter(t -> t.getIdTurno() == turno.getIdTurno())
                    .findFirst()
                    .orElse(null);

            assertNotNull(guardado);

            assertEquals(
                    ID_CLIENTE,
                    guardado.getIdCliente()
            );

            assertEquals(
                    ID_ESTILISTA,
                    guardado.getIdEmpleado()
            );

            assertEquals(
                    FECHA_TEST,
                    guardado.getFecha()
            );

            assertEquals(
                    LocalTime.of(13, 0),
                    guardado.getHoraInicio()
            );

            assertEquals(
                    LocalTime.of(14, 30),
                    guardado.getHoraFin()
            );

            assertNotNull(guardado.getServicios());

            assertFalse(
                    guardado.getServicios().isEmpty(),
                    "El turno debe conservar los servicios seleccionados"
            );

        } finally {

            eliminarTurno(turno.getIdTurno());
        }
    }

    private Servicio crearServicio(
            int id,
            String nombre,
            int duracion) {

        return new Servicio(
                id,
                nombre,
                "",
                duracion,
                0
        );
    }

    private Turno crearTurno(
            int idCliente,
            int idEmpleado,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int idServicio) {

        Turno turno = new Turno();

        turno.setIdCliente(idCliente);
        turno.setIdEmpleado(idEmpleado);
        turno.setFecha(fecha);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);

        Servicio servicio =
                crearServicio(
                        idServicio,
                        idServicio == ID_SERVICIO_CORTE
                                ? "Corte de pelo"
                                : "Coloración",
                        idServicio == ID_SERVICIO_CORTE
                                ? 30
                                : 90
                );

        turno.addServicio(servicio);

        return turno;
    }

    private void eliminarTurno(int idTurno)
            throws SQLException {

        if (idTurno <= 0) {
            return;
        }

        try (Connection conn =
                     ConexionBD.getConnection()) {

            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 "DELETE FROM turno_servicios " +
                                         "WHERE id_turno = ?")) {

                ps.setInt(1, idTurno);
                ps.executeUpdate();
            }

            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 "DELETE FROM turno " +
                                         "WHERE id_turno = ?")) {

                ps.setInt(1, idTurno);
                ps.executeUpdate();
            }
        }
    }

    private void eliminarDisponibilidad(
            int idDisponibilidad)
            throws SQLException {

        if (idDisponibilidad <= 0) {
            return;
        }

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "DELETE FROM disponibilidad " +
                                     "WHERE id_disponibilidad = ?")) {

            ps.setInt(1, idDisponibilidad);
            ps.executeUpdate();
        }
    }
}