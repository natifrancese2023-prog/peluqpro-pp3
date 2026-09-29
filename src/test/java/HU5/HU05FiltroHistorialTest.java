package HU05;

import claseslogicas.Cliente;
import claseslogicas.EstadoTurno;
import claseslogicas.Servicio;
import claseslogicas.ServicioTemp;
import claseslogicas.Turno;
import claseslogicas.HistorialView;

import dao.ConexionBD;
import dao.TurnoDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import service.ClienteService;
import service.VisitaService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU05FiltroHistorialTest {

    private final ClienteService clienteService =
            new ClienteService();

    private final VisitaService visitaService =
            new VisitaService();

    private final TurnoDAO turnoDAO =
            new TurnoDAO();

    private Cliente cliente;

    private final List<Integer> turnosGenerados =
            new ArrayList<>();


    // ============================================================
    // PREPARACIÓN
    // ============================================================

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Cliente existente en la BD de pruebas:
         *
         * ID 10
         * DNI 40000001
         * Juan Perez
         */
        cliente =
                clienteService.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba ID 10"
        );

        assertEquals(
                10,
                cliente.getIdCliente(),
                "El cliente utilizado debe ser el ID 10"
        );
    }


    // ============================================================
    // CREAR TURNO CONFIRMADO
    // ============================================================

    private Turno crearTurnoConfirmado(
            int idServicio,
            String nombreServicio,
            double precio,
            int duracionMinutos,
            LocalTime horaInicio
    ) throws Exception {

        Turno turno = new Turno();

        turno.setIdCliente(
                cliente.getIdCliente()
        );

        /*
         * Empleado / profesional:
         *
         * ID 3 = Estela Estilista
         */
        turno.setIdEmpleado(3);

        turno.setFecha(
                LocalDate.of(2099, 1, 15)
        );

        turno.setHoraInicio(
                horaInicio
        );

        turno.setHoraFin(
                horaInicio.plusMinutes(
                        duracionMinutos
                )
        );

        turno.setObservaciones(
                "Turno generado para prueba HU5"
        );

        Servicio servicio =
                new Servicio();

        servicio.setIdServicio(
                idServicio
        );

        servicio.setNombreServicio(
                nombreServicio
        );

        servicio.setPrecio(
                precio
        );

        turno.addServicio(
                servicio
        );

        boolean insertado =
                turnoDAO.insertarTurno(turno);

        assertTrue(
                insertado,
                "El turno de prueba debe registrarse"
        );

        int idTurno =
                turno.getIdTurno();

        assertTrue(
                idTurno > 0,
                "El turno debe obtener un ID"
        );

        turnosGenerados.add(
                idTurno
        );

        /*
         * HU5 necesita una visita.
         *
         * Dejamos el turno directamente en CONFIRMADO.
         * Esto solo prepara el escenario de prueba.
         */
        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "UPDATE turno " +
                                     "SET id_estado = ? " +
                                     "WHERE id_turno = ?"
                     )) {

            ps.setInt(
                    1,
                    EstadoTurno.CONFIRMADO.getId()
            );

            ps.setInt(
                    2,
                    idTurno
            );

            int filas =
                    ps.executeUpdate();

            assertEquals(
                    1,
                    filas,
                    "El turno debe quedar CONFIRMADO"
            );
        }

        return turno;
    }


    // ============================================================
    // REGISTRAR VISITA DE PRUEBA
    // ============================================================

    private void registrarVisita(
            int idServicio,
            String nombreServicio,
            double precio,
            int duracionMinutos,
            LocalTime horaInicio,
            String observacion
    ) throws Exception {

        Turno turno =
                crearTurnoConfirmado(
                        idServicio,
                        nombreServicio,
                        precio,
                        duracionMinutos,
                        horaInicio
                );

        ServicioTemp servicio =
                new ServicioTemp(
                        LocalDate.of(2099, 1, 15),
                        nombreServicio,
                        "Estela Estilista",
                        observacion,
                        "Realizado"
                );

        ObservableList<ServicioTemp> servicios =
                FXCollections.observableArrayList(
                        servicio
                );

        boolean resultado =
                visitaService.registrarVisita(
                        cliente,
                        servicios,
                        3,
                        turno.getIdTurno()
                );

        assertTrue(
                resultado,
                "La visita de prueba debe registrarse correctamente"
        );

        assertNotNull(
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                ),
                "Debe existir la visita generada"
        );
    }


    // ============================================================
    // TEST 1
    // Se elige servicio y se muestra historial filtrado
    // ============================================================

    @Test
    void filtrarHistorialPorServicio()
            throws Exception {

        /*
         * Visita 1:
         * Corte de pelo
         */
        registrarVisita(
                1,
                "Corte de pelo",
                8000,
                30,
                LocalTime.of(10, 0),
                "Corte realizado"
        );

        /*
         * Visita 2:
         * Coloración
         */
        registrarVisita(
                2,
                "Coloración",
                25000,
                90,
                LocalTime.of(12, 0),
                "Coloración realizada"
        );

        /*
         * Aplicamos el filtro:
         *
         * Servicio = Corte de pelo
         */
        List<HistorialView> historial =
                visitaService
                        .obtenerHistorialPorClienteYServicio(
                                cliente.getIdCliente(),
                                "Corte de pelo"
                        );

        assertNotNull(
                historial,
                "El resultado del filtro no debe ser null"
        );

        assertFalse(
                historial.isEmpty(),
                "Debe existir historial para el servicio seleccionado"
        );
    }


    // ============================================================
    // TEST 2
    // Solo aparecen registros del servicio seleccionado
    // ============================================================

    @Test
    void historialFiltradoSoloIncluyeServicioSeleccionado()
            throws Exception {

        registrarVisita(
                1,
                "Corte de pelo",
                8000,
                30,
                LocalTime.of(10, 0),
                "Corte realizado"
        );

        registrarVisita(
                2,
                "Coloración",
                25000,
                90,
                LocalTime.of(12, 0),
                "Coloración realizada"
        );

        /*
         * Filtramos por:
         *
         * Corte de pelo
         */
        List<HistorialView> historial =
                visitaService
                        .obtenerHistorialPorClienteYServicio(
                                cliente.getIdCliente(),
                                "Corte de pelo"
                        );

        assertFalse(
                historial.isEmpty(),
                "Debe existir al menos un registro de Corte de pelo"
        );

        /*
         * Todos los registros devueltos deben
         * corresponder al servicio seleccionado.
         */
        assertTrue(
                historial.stream()
                        .allMatch(h ->
                                "Corte de pelo".equals(
                                        h.getNombreServicio()
                                )
                        ),
                "El historial filtrado no debe contener otros servicios"
        );

        /*
         * Confirmamos además que no apareció
         * Coloración.
         */
        assertFalse(
                historial.stream()
                        .anyMatch(h ->
                                "Coloración".equals(
                                        h.getNombreServicio()
                                )
                        ),
                "El filtro por Corte de pelo no debe devolver Coloración"
        );
    }


    // ============================================================
    // TEST 3 - NEGATIVO
    // Servicio sin registros
    // ============================================================

    @Test
    void filtrarHistorialServicioSinRegistros()
            throws Exception {

        /*
         * Registramos únicamente Corte de pelo.
         */
        registrarVisita(
                1,
                "Corte de pelo",
                8000,
                30,
                LocalTime.of(10, 0),
                "Corte realizado"
        );

        /*
         * Buscamos Coloración.
         *
         * No existe una visita de Coloración
         * para este cliente dentro de esta prueba.
         */
        List<HistorialView> historial =
                visitaService
                        .obtenerHistorialPorClienteYServicio(
                                cliente.getIdCliente(),
                                "Coloración"
                        );

        assertNotNull(
                historial,
                "El filtro debe devolver una lista, no null"
        );

        assertTrue(
                historial.isEmpty(),
                "Si el cliente no tiene visitas del servicio seleccionado, el resultado debe ser vacío"
        );
    }


    // ============================================================
    // LIMPIEZA
    // ============================================================

    @AfterEach
    void limpiarDatosGenerados() {

        if (turnosGenerados.isEmpty()) {
            return;
        }

        String sqlDetalleFactura =
                "DELETE FROM detalle_factura " +
                        "WHERE id_factura IN (" +
                        "SELECT id_factura " +
                        "FROM factura " +
                        "WHERE id_turno = ?" +
                        ")";

        String sqlFactura =
                "DELETE FROM factura " +
                        "WHERE id_turno = ?";

        String sqlDetalleServicio =
                "DELETE FROM detalle_servicio " +
                        "WHERE id_visita IN (" +
                        "SELECT id_visita " +
                        "FROM visita " +
                        "WHERE id_turno = ?" +
                        ")";

        String sqlVisita =
                "DELETE FROM visita " +
                        "WHERE id_turno = ?";

        String sqlTurnoServicios =
                "DELETE FROM turno_servicios " +
                        "WHERE id_turno = ?";

        String sqlTurno =
                "DELETE FROM turno " +
                        "WHERE id_turno = ?";

        try (Connection conn =
                     ConexionBD.getConnection()) {

            conn.setAutoCommit(false);

            for (Integer idTurno :
                    turnosGenerados) {

                ejecutarDelete(
                        conn,
                        sqlDetalleFactura,
                        idTurno
                );

                ejecutarDelete(
                        conn,
                        sqlFactura,
                        idTurno
                );

                ejecutarDelete(
                        conn,
                        sqlDetalleServicio,
                        idTurno
                );

                ejecutarDelete(
                        conn,
                        sqlVisita,
                        idTurno
                );

                ejecutarDelete(
                        conn,
                        sqlTurnoServicios,
                        idTurno
                );

                ejecutarDelete(
                        conn,
                        sqlTurno,
                        idTurno
                );
            }

            conn.commit();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            turnosGenerados.clear();
        }
    }


    private void ejecutarDelete(
            Connection conn,
            String sql,
            int idTurno
    ) throws Exception {

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    idTurno
            );

            ps.executeUpdate();
        }
    }
}