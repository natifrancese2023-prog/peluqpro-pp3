
package HU6;

import claseslogicas.Cliente;
import claseslogicas.EstadoFactura;
import claseslogicas.EstadoTurno;
import claseslogicas.Servicio;
import claseslogicas.ServicioTemp;
import claseslogicas.Turno;
import claseslogicas.Visita;
import dao.ConexionBD;
import dao.FacturaDAO;
import dao.TurnoDAO;
import service.ClienteService;
import service.VisitaService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HU06RegistroVisitaTest {

    private final ClienteService clienteService = new ClienteService();
    private final VisitaService visitaService = new VisitaService();
    private final TurnoDAO turnoDAO = new TurnoDAO();
    private final FacturaDAO facturaDAO = new FacturaDAO();

    private Cliente cliente;
    private int idTurnoGenerado = -1;

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Cliente existente en la BD de pruebas:
         * ID 10 - DNI 40000001 - Juan Perez
         */
        cliente = clienteService.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba con ID 10"
        );

        assertEquals(
                10,
                cliente.getIdCliente(),
                "El cliente utilizado para HU6 debe ser el ID 10"
        );
    }

    /**
     * Crea el escenario inicial de HU6:
     *
     * Cliente registrado
     * Profesional registrado
     * Servicio registrado
     * Turno CONFIRMADO
     *
     * IMPORTANTE:
     * El turno se deja directamente en CONFIRMADO porque
     * HU6 parte de un turno confirmado.
     *
     * Este UPDATE es solamente preparación de datos de prueba.
     * No está probando la gestión de estados del turno.
     */
    private Turno crearTurnoConfirmadoDePrueba() throws Exception {

        Turno turno = new Turno();

        turno.setIdCliente(cliente.getIdCliente());

        // Empleado / profesional de prueba
        // ID 3 = Estela Estilista
        turno.setIdEmpleado(3);

        turno.setFecha(LocalDate.of(2099, 1, 15));
        turno.setHoraInicio(LocalTime.of(10, 0));
        turno.setHoraFin(LocalTime.of(10, 30));

        turno.setObservaciones(
                "Turno confirmado para prueba HU6"
        );

        Servicio servicio = new Servicio();
        servicio.setIdServicio(1);
        servicio.setNombreServicio("Corte de pelo");
        servicio.setPrecio(8000);

        turno.addServicio(servicio);

        /*
         * TurnoDAO lo crea inicialmente como PENDIENTE.
         */
        boolean insertado = turnoDAO.insertarTurno(turno);

        assertTrue(
                insertado,
                "El turno de prueba debe registrarse"
        );

        idTurnoGenerado = turno.getIdTurno();

        assertTrue(
                idTurnoGenerado > 0,
                "El turno debe obtener un ID"
        );

        /*
         * Preparación del escenario:
         *
         * PENDIENTE -> CONFIRMADO
         *
         * Esto NO prueba HU13.
         * Solo deja el turno en el estado requerido
         * para ejecutar HU6.
         */
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
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
                    idTurnoGenerado
            );

            int filas = ps.executeUpdate();

            assertEquals(
                    1,
                    filas,
                    "El turno debe quedar preparado en estado CONFIRMADO"
            );
        }

        /*
         * Verificación del escenario inicial.
         */
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id_estado " +
                             "FROM turno " +
                             "WHERE id_turno = ?"
             )) {

            ps.setInt(1, idTurnoGenerado);

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(rs.next());

                assertEquals(
                        EstadoTurno.CONFIRMADO.getId(),
                        rs.getInt("id_estado"),
                        "El turno utilizado por HU6 debe estar CONFIRMADO"
                );
            }
        }

        return turno;
    }

    /**
     * Servicios que se enviarán al registrar la visita.
     */
    private ObservableList<ServicioTemp> crearServiciosDePrueba(
            String observaciones
    ) {

        ServicioTemp servicio = new ServicioTemp(
                LocalDate.of(2099, 1, 15),
                "Corte de pelo",
                "Estela Estilista",
                observaciones,
                "Realizado"
        );

        return FXCollections.observableArrayList(servicio);
    }

    // ============================================================
    // TEST 1
    // Se puede registrar la visita correspondiente a un turno
    // ============================================================

    @Test
    void registrarVisitaCorrespondienteATurno() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Corte realizado correctamente"
                );

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "Debe poder registrarse una visita correspondiente a un turno CONFIRMADO"
        );

        Visita visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                visita,
                "Debe existir una visita asociada al turno"
        );

        assertEquals(
                turno.getIdTurno(),
                visita.getIdTurno(),
                "La visita debe quedar asociada al turno"
        );
    }

    // ============================================================
    // TEST 2
    // Fecha + cliente + profesional
    // ============================================================

    @Test
    void visitaRegistraFechaClienteYEmpleado() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Observación técnica"
                );

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "La visita debe registrarse correctamente"
        );

        Visita visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                visita,
                "Debe existir la visita registrada"
        );

        // Cliente atendido
        assertEquals(
                cliente.getIdCliente(),
                visita.getIdCliente(),
                "Debe registrarse el cliente atendido"
        );

        // Fecha de visita
        assertNotNull(
                visita.getFechaHoraCierre(),
                "La visita debe registrar fecha y hora"
        );

        /*
         * Verificación del profesional.
         * El modelo utiliza id_estilista en la tabla visita.
         * Funcionalmente corresponde al empleado/profesional.
         */
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id_estilista " +
                             "FROM visita " +
                             "WHERE id_visita = ?"
             )) {

            ps.setInt(
                    1,
                    visita.getIdVisita()
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next(),
                        "Debe existir la visita en la BD"
                );

                assertEquals(
                        3,
                        rs.getInt("id_estilista"),
                        "Debe registrarse el profesional que realizó la atención"
                );
            }
        }
    }

    // ============================================================
    // TEST 3
    // Servicios + observaciones
    // ============================================================

    @Test
    void visitaRegistraServiciosYObservaciones() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        String observacion =
                "Cabello seco. Se recomienda tratamiento hidratante.";

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(observacion);

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "La visita debe registrarse correctamente"
        );

        Visita visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                visita,
                "Debe existir la visita"
        );

        assertNotNull(
                visita.getServiciosRealizados(),
                "La visita debe contener los servicios realizados"
        );

        assertFalse(
                visita.getServiciosRealizados().isEmpty(),
                "Debe existir al menos un servicio realizado"
        );

        Servicio servicio =
                visita.getServiciosRealizados().get(0);

        assertEquals(
                1,
                servicio.getIdServicio(),
                "Debe registrarse el servicio Corte de pelo"
        );

        /*
         * La observación técnica se almacena
         * en detalle_servicio.
         */
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT observaciones_servicio " +
                             "FROM detalle_servicio " +
                             "WHERE id_visita = ? " +
                             "AND id_servicio = ?"
             )) {

            ps.setInt(
                    1,
                    visita.getIdVisita()
            );

            ps.setInt(
                    2,
                    1
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next(),
                        "Debe existir el detalle del servicio"
                );

                assertEquals(
                        observacion,
                        rs.getString("observaciones_servicio"),
                        "Debe registrarse la observación de la atención"
                );
            }
        }
    }

    // ============================================================
    // TEST 4
    // La visita queda en el historial
    // ============================================================

    @Test
    void visitaQuedaAsociadaAlHistorial() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Observación registrada para historial"
                );

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "La visita debe registrarse correctamente"
        );

        Visita visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                visita,
                "Debe existir la visita"
        );

        var historial =
                visitaService.obtenerHistorialPorCliente(
                        cliente.getIdCliente()
                );

        assertFalse(
                historial.isEmpty(),
                "El historial del cliente debe contener la visita registrada"
        );

        /*
         * HistorialView no posee getIdVisita().
         * Por eso comprobamos la asociación real
         * directamente en la tabla visita.
         */
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) " +
                             "FROM visita " +
                             "WHERE id_visita = ? " +
                             "AND id_cliente = ?"
             )) {

            ps.setInt(
                    1,
                    visita.getIdVisita()
            );

            ps.setInt(
                    2,
                    cliente.getIdCliente()
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(rs.next());

                assertEquals(
                        1,
                        rs.getInt(1),
                        "La visita debe quedar asociada al cliente"
                );
            }
        }
    }

    // ============================================================
    // TEST 5
    // Al registrar visita se genera factura
    // ============================================================

    @Test
    void registrarVisitaGeneraFactura() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Factura automática"
                );

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "La visita debe registrarse correctamente"
        );

        var factura =
                facturaDAO.obtenerPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                factura,
                "Al registrar la visita debe generarse automáticamente una factura"
        );

        assertEquals(
                turno.getIdTurno(),
                factura.getIdTurno(),
                "La factura debe quedar asociada al turno"
        );

        assertEquals(
                cliente.getIdCliente(),
                factura.getIdCliente(),
                "La factura debe quedar asociada al cliente"
        );
    }

    // ============================================================
    // TEST 6
    // La factura se genera en estado FACTURADA
    // ============================================================

    @Test
    void facturaInicialmenteFacturada() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Factura en estado facturada"
                );

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "La visita debe registrarse correctamente"
        );

        var factura =
                facturaDAO.obtenerPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                factura,
                "Debe generarse una factura"
        );

        assertEquals(
                EstadoFactura.FACTURADA,
                factura.getEstadoFactura(),
                "La factura debe generarse inicialmente en estado FACTURADA"
        );
    }

    // ============================================================
    // TEST 7
    // Detalle de servicios e importes
    // ============================================================

    @Test
    void facturaIncluyeDetalleDeServiciosEImportes() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Detalle de factura"
                );

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertTrue(
                resultado,
                "La visita debe registrarse correctamente"
        );

        var factura =
                facturaDAO.obtenerPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                factura,
                "Debe existir la factura"
        );

        /*
         * Servicio utilizado:
         * ID 1
         * Corte de pelo
         * $8000
         */
        assertEquals(
                8000.0,
                factura.getMontoTotal().doubleValue(),
                0.01,
                "El total debe coincidir con el importe del servicio"
        );

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT descripcion_servicio, " +
                             "precio_unitario, " +
                             "cantidad " +
                             "FROM detalle_factura " +
                             "WHERE id_factura = ? " +
                             "AND id_servicio = ?"
             )) {

            ps.setInt(
                    1,
                    factura.getIdFactura()
            );

            ps.setInt(
                    2,
                    1
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next(),
                        "La factura debe incluir el detalle del servicio"
                );

                assertNotNull(
                        rs.getString("descripcion_servicio"),
                        "El detalle de factura debe contener una descripción del servicio"
                );

                assertEquals(
                        8000.0,
                        rs.getBigDecimal("precio_unitario")
                                .doubleValue(),
                        0.01,
                        "Debe registrarse el importe del servicio"
                );

                assertEquals(
                        1,
                        rs.getInt("cantidad"),
                        "La cantidad del servicio debe ser 1"
                );
            }
        }
    }

    // ============================================================
    // TEST 8 - NEGATIVO
    // ============================================================

    @Test
    void registrarVisitaSinServicios() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                FXCollections.observableArrayList();

        boolean resultado = visitaService.registrarVisita(
                cliente,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertFalse(
                resultado,
                "No debe permitirse registrar una visita sin servicios"
        );

        Visita visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNull(
                visita,
                "No debe generarse una visita sin servicios"
        );
    }

    // ============================================================
    // TEST 9 - NEGATIVO
    // ============================================================

    @Test
    void registrarVisitaSinCliente() throws Exception {

        Turno turno = crearTurnoConfirmadoDePrueba();

        ObservableList<ServicioTemp> servicios =
                crearServiciosDePrueba(
                        "Prueba sin cliente"
                );

        boolean resultado = visitaService.registrarVisita(
                null,
                servicios,
                3,
                turno.getIdTurno()
        );

        assertFalse(
                resultado,
                "No debe permitirse registrar una visita sin cliente"
        );

        Visita visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNull(
                visita,
                "No debe generarse una visita sin cliente"
        );
    }

    // ============================================================
    // LIMPIEZA
    // ============================================================

    @AfterEach
    void limpiarDatosGenerados() {

        if (idTurnoGenerado <= 0) {
            return;
        }

        String sqlDetalleFactura =
                "DELETE FROM detalle_factura " +
                        "WHERE id_factura IN (" +
                        "SELECT id_factura FROM factura WHERE id_turno = ?" +
                        ")";

        String sqlFactura =
                "DELETE FROM factura WHERE id_turno = ?";

        String sqlDetalleServicio =
                "DELETE FROM detalle_servicio " +
                        "WHERE id_visita IN (" +
                        "SELECT id_visita FROM visita WHERE id_turno = ?" +
                        ")";

        String sqlVisita =
                "DELETE FROM visita WHERE id_turno = ?";

        String sqlTurnoServicios =
                "DELETE FROM turno_servicios WHERE id_turno = ?";

        String sqlTurno =
                "DELETE FROM turno WHERE id_turno = ?";

        try (Connection conn = ConexionBD.getConnection()) {

            conn.setAutoCommit(false);

            ejecutarDelete(
                    conn,
                    sqlDetalleFactura,
                    idTurnoGenerado
            );

            ejecutarDelete(
                    conn,
                    sqlFactura,
                    idTurnoGenerado
            );

            ejecutarDelete(
                    conn,
                    sqlDetalleServicio,
                    idTurnoGenerado
            );

            ejecutarDelete(
                    conn,
                    sqlVisita,
                    idTurnoGenerado
            );

            ejecutarDelete(
                    conn,
                    sqlTurnoServicios,
                    idTurnoGenerado
            );

            ejecutarDelete(
                    conn,
                    sqlTurno,
                    idTurnoGenerado
            );

            conn.commit();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            idTurnoGenerado = -1;
        }
    }

    private void ejecutarDelete(
            Connection conn,
            String sql,
            int idTurno
    ) throws Exception {

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, idTurno);
            ps.executeUpdate();
        }
    }
}