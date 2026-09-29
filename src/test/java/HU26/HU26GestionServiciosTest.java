package HU26;

import claseslogicas.Servicio;
import claseslogicas.Turno;
import dao.ConexionBD;
import dao.TurnoDAO;
import org.junit.jupiter.api.Test;
import service.ServicioService;
import service.TurnoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU26GestionServiciosTest {

    private final ServicioService servicioService =
            new ServicioService();


    // =========================================================
    // TEST 1
    // Registrar y consultar un servicio
    // =========================================================

    @Test
    void registrarYConsultarServicio() throws Exception {

        String nombre =
                "Servicio HU26 " + System.currentTimeMillis();

        Servicio servicio = new Servicio(
                0,
                nombre,
                "Servicio creado para prueba HU26",
                60,
                15000.00,
                4000.00,
                true
        );

        try {

            servicioService.registrarServicio(servicio);

            assertTrue(
                    servicio.getIdServicio() > 0,
                    "El servicio debe registrarse y obtener un identificador."
            );

            Servicio obtenido =
                    servicioService.obtenerServicio(
                            servicio.getIdServicio()
                    );

            assertNotNull(
                    obtenido,
                    "El servicio registrado debe poder consultarse."
            );

            assertEquals(
                    nombre,
                    obtenido.getNombreServicio()
            );

            assertEquals(
                    60,
                    obtenido.getDuracionMinutos()
            );

            assertEquals(
                    15000.00,
                    obtenido.getPrecio(),
                    0.001
            );

            assertEquals(
                    4000.00,
                    obtenido.getCosto(),
                    0.001
            );

        } finally {

            eliminarServicio(servicio.getIdServicio());
        }
    }


    // =========================================================
    // TEST 2
    // Actualizar información y estado
    // =========================================================

    @Test
    void actualizarInformacionYEstadoDelServicio()
            throws Exception {

        Servicio servicio = new Servicio(
                0,
                "Servicio HU26 Actualizacion " +
                        System.currentTimeMillis(),
                "Descripcion inicial",
                45,
                10000.00,
                3000.00,
                true
        );

        try {

            servicioService.registrarServicio(servicio);

            servicio.setNombreServicio(
                    "Servicio HU26 Actualizado"
            );

            servicio.setDescripcion(
                    "Descripcion actualizada"
            );

            servicio.setDuracionMinutos(90);
            servicio.setPrecio(22000.00);
            servicio.setCosto(6000.00);

            servicioService.actualizarServicio(servicio);

            Servicio actualizado =
                    servicioService.obtenerServicio(
                            servicio.getIdServicio()
                    );

            assertNotNull(actualizado);

            assertEquals(
                    "Servicio HU26 Actualizado",
                    actualizado.getNombreServicio()
            );

            assertEquals(
                    "Descripcion actualizada",
                    actualizado.getDescripcion()
            );

            assertEquals(
                    90,
                    actualizado.getDuracionMinutos()
            );

            assertEquals(
                    22000.00,
                    actualizado.getPrecio(),
                    0.001
            );

            assertEquals(
                    6000.00,
                    actualizado.getCosto(),
                    0.001
            );

            servicioService.actualizarEstado(
                    servicio.getIdServicio(),
                    false
            );

            Servicio inactivo =
                    servicioService.obtenerServicio(
                            servicio.getIdServicio()
                    );

            assertFalse(
                    inactivo.isActivo(),
                    "El servicio debe quedar inactivo después de actualizar su estado."
            );

        } finally {

            eliminarServicio(servicio.getIdServicio());
        }
    }


    // =========================================================
    // TEST 3
    // Listar servicio y utilizarlo en un turno
    // =========================================================

    @Test
    void listarServicioYUtilizarloEnTurno()
            throws Exception {

        Servicio servicio = new Servicio(
                0,
                "Servicio HU26 Turno " +
                        System.currentTimeMillis(),
                "Servicio utilizado en turno de prueba",
                30,
                12000.00,
                2500.00,
                true
        );

        Turno turno = new Turno();

        try {

            servicioService.registrarServicio(servicio);

            List<Servicio> servicios =
                    servicioService.listarServicios();

            assertTrue(
                    servicios.stream()
                            .anyMatch(s ->
                                    s.getIdServicio()
                                            == servicio.getIdServicio()
                            ),
                    "El servicio registrado debe aparecer en el listado."
            );

            turno.setIdCliente(10);
            turno.setIdEmpleado(3);
            turno.setFecha(
                    LocalDate.of(2110, 12, 15)
            );
            turno.setHoraInicio(
                    LocalTime.of(17, 0)
            );
            turno.setHoraFin(
                    LocalTime.of(17, 30)
            );
            turno.setObservaciones(
                    "Turno de prueba HU26"
            );

            turno.addServicio(servicio);

            TurnoService turnoService =
                    new TurnoService();

            boolean registrado =
                    turnoService.registrarTurno(turno);

            assertTrue(
                    registrado,
                    "El servicio debe poder utilizarse dentro de la gestión de turnos."
            );

            ServicioService servicioService2 =
                    new ServicioService();

            List<Servicio> serviciosTurno =
                    servicioService2.listarServiciosPorTurno(
                            turno.getIdTurno()
                    );

            assertTrue(
                    serviciosTurno.stream()
                            .anyMatch(s ->
                                    s.getIdServicio()
                                            == servicio.getIdServicio()
                            ),
                    "El servicio debe quedar asociado al turno."
            );

        } finally {

            eliminarTurno(turno.getIdTurno());
            eliminarServicio(servicio.getIdServicio());
        }
    }


    // =========================================================
    // LIMPIEZA
    // =========================================================

    private void eliminarTurno(int idTurno) {

        if (idTurno <= 0) {
            return;
        }

        String sqlDetalle =
                "DELETE FROM turno_servicios WHERE id_turno = ?";

        String sqlTurno =
                "DELETE FROM turno WHERE id_turno = ?";

        try (Connection conn =
                     ConexionBD.getConnection()) {

            try (PreparedStatement ps =
                         conn.prepareStatement(sqlDetalle)) {

                ps.setInt(1, idTurno);
                ps.executeUpdate();
            }

            try (PreparedStatement ps =
                         conn.prepareStatement(sqlTurno)) {

                ps.setInt(1, idTurno);
                ps.executeUpdate();
            }

        } catch (Exception ignored) {
        }
    }


    private void eliminarServicio(int idServicio) {

        if (idServicio <= 0) {
            return;
        }

        String sql =
                "DELETE FROM servicios WHERE id_servicio = ?";

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, idServicio);
            ps.executeUpdate();

        } catch (Exception ignored) {
        }
    }
}