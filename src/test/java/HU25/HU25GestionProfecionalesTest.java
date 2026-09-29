package HU25;

import claseslogicas.Disponibilidad;
import claseslogicas.Empleado;
import claseslogicas.Especialidad;
import dao.ConexionBD;
import dao.DisponibilidadDAO;
import org.junit.jupiter.api.*;

import service.EmpleadoService;
import service.EspecialidadService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HU25GestionProfecionalesTest {

    private static EmpleadoService empleadoService;
    private static EspecialidadService especialidadService;
    private static DisponibilidadDAO disponibilidadDAO;

    @BeforeAll
    static void iniciar() {
        empleadoService = new EmpleadoService();
        especialidadService = new EspecialidadService();
        disponibilidadDAO = new DisponibilidadDAO();
    }

    // =========================================================
    // 1. REGISTRAR PROFESIONAL
    // RD2.1 / UC13
    // =========================================================

    @Test
    @Order(1)
    void registrarProfesional() throws Exception {

        Empleado empleado = new Empleado();

        empleado.setNombre("Test");
        empleado.setApellido("Profesional");
        empleado.setTelefono("3572999001");
        empleado.setEmail("test.profesional.hu25@gmail.com");
        empleado.setCalle("Calle Test");
        empleado.setNumero("100");
        empleado.setFechaIngreso(LocalDate.now());
        empleado.setPorcentajeComision(15.0);
        empleado.setEsEstilista(true);

        boolean resultado =
                empleadoService.registrarProfesional(
                        empleado,
                        "DNI",
                        "99991001",
                        "Centro Laguna Larga"
                );

        assertTrue(
                resultado,
                "El profesional debería registrarse correctamente."
        );

        assertTrue(
                empleado.getIdEmpleado() > 0,
                "El profesional registrado debe recibir un ID."
        );

        assertTrue(
                empleado.getIdPersona() > 0,
                "La persona asociada debe recibir un ID."
        );

        eliminarProfesional(
                empleado.getIdEmpleado(),
                empleado.getIdPersona()
        );
    }


    // =========================================================
    // 2. CONSULTAR PROFESIONAL
    // RD2.2 / UC14
    // =========================================================

    @Test
    @Order(2)
    void consultarProfesional() throws Exception {

        Empleado profesional =
                empleadoService.obtenerPorId(3);

        assertNotNull(
                profesional,
                "Debe existir el profesional de prueba con ID 3."
        );

        assertEquals(
                3,
                profesional.getIdEmpleado()
        );

        assertNotNull(
                profesional.getPersona(),
                "El profesional debe tener sus datos personales."
        );

        assertEquals(
                "Estela",
                profesional.getPersona().getNombre()
        );

        assertEquals(
                "Estilista",
                profesional.getPersona().getApellido()
        );
    }


    // =========================================================
    // 3. ACTUALIZAR PROFESIONAL
    // RD2.3 / UC15
    // =========================================================

    @Test
    @Order(3)
    void actualizarProfesional() throws Exception {

        Empleado original =
                empleadoService.obtenerPorId(3);

        assertNotNull(original);

        String nombreOriginal =
                original.getPersona().getNombre();

        String telefonoOriginal =
                original.getPersona().getTelefono();

        original.getPersona().setNombre(
                "EstelaActualizadaHU25"
        );

        original.getPersona().setTelefono(
                "3572400999"
        );

        boolean resultado =
                empleadoService.actualizarProfesional(
                        original,
                        original.getPersona().getNombreBarrio()
                );

        assertTrue(
                resultado,
                "La actualización del profesional debería ser exitosa."
        );

        Empleado actualizado =
                empleadoService.obtenerPorId(3);

        assertEquals(
                "EstelaActualizadaHU25",
                actualizado.getPersona().getNombre()
        );

        assertEquals(
                "3572400999",
                actualizado.getPersona().getTelefono()
        );

        // Restaurar datos originales
        actualizado.getPersona().setNombre(nombreOriginal);
        actualizado.getPersona().setTelefono(telefonoOriginal);

        empleadoService.actualizarProfesional(
                actualizado,
                actualizado.getPersona().getNombreBarrio()
        );
    }


    // =========================================================
    // 4. ACTUALIZAR ESTADO DEL PROFESIONAL
    // RD2.4 / UC16
    // =========================================================

    @Test
    @Order(4)
    void actualizarEstadoProfesional() throws Exception {

        Empleado original =
                empleadoService.obtenerPorId(3);

        assertNotNull(original);
        assertTrue(original.isActivo());

        boolean desactivado =
                empleadoService.actualizarEstado(
                        3,
                        false
                );

        assertTrue(
                desactivado,
                "El estado del profesional debería actualizarse."
        );

        Empleado inactivo =
                empleadoService.obtenerPorId(3);

        assertNotNull(inactivo);
        assertFalse(
                inactivo.isActivo(),
                "El profesional debería quedar inactivo."
        );

        boolean reactivado =
                empleadoService.actualizarEstado(
                        3,
                        true
                );

        assertTrue(reactivado);

        Empleado activo =
                empleadoService.obtenerPorId(3);

        assertTrue(
                activo.isActivo(),
                "El profesional debería quedar nuevamente activo."
        );
    }


    // =========================================================
    // 5. LISTADO DE PROFESIONALES
    // RD2.5 / UC17
    // =========================================================

    @Test
    @Order(5)
    void listarProfesionales() throws Exception {

        List<Empleado> profesionales =
                empleadoService.obtenerProfesionales();

        assertNotNull(
                profesionales,
                "El listado de profesionales no debe devolver null."
        );

        assertFalse(
                profesionales.isEmpty(),
                "Debe existir al menos un profesional."
        );

        assertTrue(
                profesionales.stream()
                        .allMatch(empleado ->
                                empleado != null
                                        && empleado.getIdEmpleado() > 0
                        ),
                "Cada profesional del listado debe tener un ID válido."
        );
    }
    // =========================================================
    // 6. REGISTRAR ESPECIALIDAD
    // RD2.6 / UC18
    // =========================================================

    @Test
    @Order(6)
    void registrarEspecialidad() throws Exception {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setNombre(
                "Especialidad HU25 Test"
        );

        especialidadService.registrarEspecialidad(
                especialidad
        );

        assertTrue(
                especialidad.getIdEspecialidad() > 0,
                "La especialidad debe recibir un ID."
        );

        Especialidad registrada =
                especialidadService.obtenerEspecialidad(
                        especialidad.getIdEspecialidad()
                );

        assertNotNull(registrada);

        assertEquals(
                "Especialidad HU25 Test",
                registrada.getNombre()
        );

        eliminarEspecialidad(
                especialidad.getIdEspecialidad()
        );
    }


    // =========================================================
    // 7. CONSULTAR ESPECIALIDAD
    // RD2.7 / UC19
    // =========================================================

    @Test
    @Order(7)
    void consultarEspecialidad() throws Exception {

        int idEspecialidad =
                crearEspecialidadTemporal();

        Especialidad especialidad =
                especialidadService.obtenerEspecialidad(
                        idEspecialidad
                );

        assertNotNull(
                especialidad,
                "Debe poder consultarse la especialidad registrada."
        );

        assertEquals(
                idEspecialidad,
                especialidad.getIdEspecialidad()
        );

        assertEquals(
                "Especialidad HU25 Consulta",
                especialidad.getNombre()
        );

        eliminarEspecialidad(idEspecialidad);
    }


    // =========================================================
    // 8. ACTUALIZAR ESPECIALIDAD
    // RD2.8 / UC20
    // =========================================================

    @Test
    @Order(8)
    void actualizarEspecialidad() throws Exception {

        int idEspecialidad =
                crearEspecialidadTemporal();

        Especialidad especialidad =
                especialidadService.obtenerEspecialidad(
                        idEspecialidad
                );

        especialidad.setNombre(
                "Especialidad HU25 Actualizada"
        );

        especialidadService.actualizarEspecialidad(
                especialidad
        );

        Especialidad actualizada =
                especialidadService.obtenerEspecialidad(
                        idEspecialidad
                );

        assertNotNull(actualizada);

        assertEquals(
                "Especialidad HU25 Actualizada",
                actualizada.getNombre()
        );

        eliminarEspecialidad(idEspecialidad);
    }


    // =========================================================
    // 9. ACTUALIZAR ESTADO DE ESPECIALIDAD
    // RD2.9 / UC21
    // =========================================================

    @Test
    @Order(9)
    void actualizarEstadoEspecialidad() throws Exception {

        int idEspecialidad =
                crearEspecialidadTemporal();

        especialidadService.actualizarEstado(
                idEspecialidad,
                false
        );

        Especialidad inactiva =
                especialidadService.obtenerEspecialidad(
                        idEspecialidad
                );

        assertNotNull(inactiva);

        assertFalse(
                inactiva.isActivo(),
                "La especialidad debería quedar inactiva."
        );

        especialidadService.actualizarEstado(
                idEspecialidad,
                true
        );

        Especialidad activa =
                especialidadService.obtenerEspecialidad(
                        idEspecialidad
                );

        assertTrue(
                activa.isActivo(),
                "La especialidad debería quedar activa."
        );

        eliminarEspecialidad(idEspecialidad);
    }


    // =========================================================
    // 10. LISTAR ESPECIALIDADES
    // RD2.10 / UC22
    // =========================================================

    @Test
    @Order(10)
    void listarEspecialidades() throws Exception {

        int idEspecialidad =
                crearEspecialidadTemporal();

        List<Especialidad> especialidades =
                especialidadService.listarEspecialidades();

        assertNotNull(especialidades);

        assertFalse(
                especialidades.isEmpty(),
                "Debe existir al menos una especialidad."
        );

        assertTrue(
                especialidades.stream()
                        .anyMatch(e ->
                                e.getIdEspecialidad()
                                        == idEspecialidad
                        ),
                "La especialidad registrada debe aparecer en el listado."
        );

        eliminarEspecialidad(idEspecialidad);
    }


    // =========================================================
    // 11. REGISTRAR DISPONIBILIDAD
    // RD2.11 / UC23
    // =========================================================

    @Test
    @Order(11)
    void registrarDisponibilidad() throws Exception {

        Disponibilidad disponibilidad =
                new Disponibilidad();

        disponibilidad.setIdEmpleado(3);
        disponibilidad.setDiaSemana("Lunes");
        disponibilidad.setHoraDesde(
                LocalTime.of(10, 0)
        );
        disponibilidad.setHoraHasta(
                LocalTime.of(14, 0)
        );

        disponibilidadDAO.insertar(
                disponibilidad
        );

        assertTrue(
                disponibilidad.getIdDisponibilidad() > 0,
                "La disponibilidad debe recibir un ID."
        );

        Disponibilidad registrada =
                disponibilidadDAO.obtenerPorId(
                        disponibilidad.getIdDisponibilidad()
                );

        assertNotNull(registrada);

        assertEquals(
                3,
                registrada.getIdEmpleado()
        );

        assertEquals(
                "Lunes",
                registrada.getDiaSemana()
        );

        eliminarDisponibilidad(
                disponibilidad.getIdDisponibilidad()
        );
    }


    // =========================================================
    // 12. CONSULTAR DISPONIBILIDAD
    // RD2.12 / UC24
    // =========================================================

    @Test
    @Order(12)
    void consultarDisponibilidad() throws Exception {

        int idDisponibilidad =
                crearDisponibilidadTemporal();

        Disponibilidad disponibilidad =
                disponibilidadDAO.obtenerPorId(
                        idDisponibilidad
                );

        assertNotNull(
                disponibilidad,
                "Debe poder consultarse la disponibilidad."
        );

        assertEquals(
                3,
                disponibilidad.getIdEmpleado()
        );

        assertEquals(
                "Martes",
                disponibilidad.getDiaSemana()
        );

        eliminarDisponibilidad(
                idDisponibilidad
        );
    }


    // =========================================================
    // 13. ACTUALIZAR DISPONIBILIDAD
    // RD2.13 / UC25
    // =========================================================

    @Test
    @Order(13)
    void actualizarDisponibilidad() throws Exception {

        int idDisponibilidad =
                crearDisponibilidadTemporal();

        Disponibilidad disponibilidad =
                disponibilidadDAO.obtenerPorId(
                        idDisponibilidad
                );

        disponibilidad.setDiaSemana(
                "Miércoles"
        );

        disponibilidad.setHoraDesde(
                LocalTime.of(11, 0)
        );

        disponibilidad.setHoraHasta(
                LocalTime.of(15, 0)
        );

        disponibilidadDAO.actualizar(
                disponibilidad
        );

        Disponibilidad actualizada =
                disponibilidadDAO.obtenerPorId(
                        idDisponibilidad
                );

        assertNotNull(actualizada);

        assertEquals(
                "Miércoles",
                actualizada.getDiaSemana()
        );

        assertEquals(
                LocalTime.of(11, 0),
                actualizada.getHoraDesde()
        );

        assertEquals(
                LocalTime.of(15, 0),
                actualizada.getHoraHasta()
        );

        eliminarDisponibilidad(
                idDisponibilidad
        );
    }


    // =========================================================
    // 14. LISTAR DISPONIBILIDADES
    // RD2.14 / UC26
    // =========================================================

    @Test
    @Order(14)
    void listarDisponibilidades() throws Exception {

        int idDisponibilidad =
                crearDisponibilidadTemporal();

        List<Disponibilidad> disponibilidades =
                disponibilidadDAO.obtenerTodas();

        assertNotNull(disponibilidades);

        assertTrue(
                disponibilidades.stream()
                        .anyMatch(d ->
                                d.getIdDisponibilidad()
                                        == idDisponibilidad
                        ),
                "La disponibilidad registrada debe aparecer en el listado."
        );

        eliminarDisponibilidad(
                idDisponibilidad
        );
    }


    // =========================================================
    // 15. DISPONIBILIDAD DISPONIBLE PARA GESTIÓN DE TURNOS
    // =========================================================
    @Test
    @Order(15)
    void informacionProfesionalYDisponibilidadDisponibleParaTurnos()
            throws Exception {

        Empleado profesional =
                empleadoService.obtenerPorId(3);

        assertNotNull(
                profesional,
                "El profesional debe estar disponible para la gestión de turnos."
        );

        assertTrue(
                profesional.getIdEmpleado() > 0,
                "El profesional debe tener un ID válido."
        );

        int idDisponibilidad =
                crearDisponibilidadTemporal();

        List<Disponibilidad> disponibilidades =
                disponibilidadDAO.obtenerPorEmpleado(3);

        assertNotNull(
                disponibilidades,
                "Debe poder consultarse la disponibilidad del profesional."
        );

        assertTrue(
                disponibilidades.stream()
                        .anyMatch(d ->
                                d.getIdDisponibilidad()
                                        == idDisponibilidad
                        ),
                "La disponibilidad del profesional debe estar disponible para su gestión."
        );

        eliminarDisponibilidad(idDisponibilidad);
    }


    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private static int crearEspecialidadTemporal()
            throws Exception {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setNombre(
                "Especialidad HU25 Consulta"
        );

        especialidadService.registrarEspecialidad(
                especialidad
        );

        return especialidad.getIdEspecialidad();
    }


    private static int crearDisponibilidadTemporal()
            throws Exception {

        Disponibilidad disponibilidad =
                new Disponibilidad();

        disponibilidad.setIdEmpleado(3);
        disponibilidad.setDiaSemana("Martes");
        disponibilidad.setHoraDesde(
                LocalTime.of(10, 0)
        );
        disponibilidad.setHoraHasta(
                LocalTime.of(14, 0)
        );

        disponibilidadDAO.insertar(
                disponibilidad
        );

        return disponibilidad.getIdDisponibilidad();
    }


    private static void eliminarEspecialidad(
            int idEspecialidad
    ) throws Exception {

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "DELETE FROM especialidad " +
                                     "WHERE id_especialidad = ?"
                     )) {

            ps.setInt(1, idEspecialidad);
            ps.executeUpdate();
        }
    }


    private static void eliminarDisponibilidad(
            int idDisponibilidad
    ) throws Exception {

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "DELETE FROM disponibilidad " +
                                     "WHERE id_disponibilidad = ?"
                     )) {

            ps.setInt(1, idDisponibilidad);
            ps.executeUpdate();
        }
    }


    private static void eliminarProfesional(
            int idEmpleado,
            int idPersona
    ) throws Exception {

        int idDocumento = 0;

        try (Connection conn =
                     ConexionBD.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String sqlDocumento =
                        "SELECT id_documento " +
                                "FROM persona " +
                                "WHERE id_persona = ?";

                try (PreparedStatement ps =
                             conn.prepareStatement(sqlDocumento)) {

                    ps.setInt(1, idPersona);

                    try (ResultSet rs =
                                 ps.executeQuery()) {

                        if (rs.next()) {
                            idDocumento =
                                    rs.getInt("id_documento");
                        }
                    }
                }

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     "DELETE FROM empleado " +
                                             "WHERE id_empleado = ?"
                             )) {

                    ps.setInt(1, idEmpleado);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     "DELETE FROM persona " +
                                             "WHERE id_persona = ?"
                             )) {

                    ps.setInt(1, idPersona);
                    ps.executeUpdate();
                }

                if (idDocumento > 0) {

                    try (PreparedStatement ps =
                                 conn.prepareStatement(
                                         "DELETE FROM documento " +
                                                 "WHERE id_documento = ?"
                                 )) {

                        ps.setInt(1, idDocumento);
                        ps.executeUpdate();
                    }
                }

                conn.commit();

            } catch (Exception e) {

                conn.rollback();
                throw e;
            }
        }
    }
}