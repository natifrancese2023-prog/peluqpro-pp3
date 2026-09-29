package hu04;

import claseslogicas.Cliente;
import claseslogicas.EstadoTurno;
import claseslogicas.HistorialView;
import claseslogicas.Servicio;
import claseslogicas.ServicioTemp;
import claseslogicas.Turno;
import dao.ConexionBD;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU04HistorialClienteTest {

    private final ClienteService clienteService = new ClienteService();
    private final VisitaService visitaService = new VisitaService();
    private final TurnoDAO turnoDAO = new TurnoDAO();

    private Cliente cliente;

    private final List<Integer> turnosGenerados = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Cliente fijo de la BD de pruebas:
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
                "El cliente utilizado debe ser el ID 10"
        );
    }

    /**
     * Crea un turno y lo deja directamente en CONFIRMADO.
     *
     * Esto solamente prepara el escenario necesario para registrar
     * una visita. No se está probando aquí la gestión de estados.
     */
    private Turno crearTurnoConfirmado(
            LocalTime horaInicio
    ) throws Exception {

        Turno turno = new Turno();

        turno.setIdCliente(cliente.getIdCliente());
        turno.setIdEmpleado(3);

        turno.setFecha(
                LocalDate.of(2099, 1, 15)
        );

        turno.setHoraInicio(horaInicio);

        turno.setHoraFin(
                horaInicio.plusMinutes(30)
        );

        turno.setObservaciones(
                "Turno generado para prueba HU4"
        );

        Servicio servicio = new Servicio();

        servicio.setIdServicio(1);
        servicio.setNombreServicio("Corte de pelo");
        servicio.setPrecio(8000);

        turno.addServicio(servicio);

        boolean insertado =
                turnoDAO.insertarTurno(turno);

        assertTrue(
                insertado,
                "El turno de prueba debe registrarse"
        );

        int idTurno = turno.getIdTurno();

        assertTrue(
                idTurno > 0,
                "El turno debe obtener un ID"
        );

        turnosGenerados.add(idTurno);

        /*
         * Preparación del escenario:
         *
         * PENDIENTE -> CONFIRMADO
         *
         * No es una prueba de HU13.
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
                    "El turno debe quedar confirmado"
            );
        }

        return turno;
    }

    /**
     * Registra una visita utilizando la funcionalidad real de HU6.
     *
     * De esta manera HU4 consulta información que realmente fue
     * generada por el flujo de registro de visita.
     */
    private int registrarVisitaDePrueba(
            LocalTime hora,
            String observacion
    ) throws Exception {

        Turno turno =
                crearTurnoConfirmado(hora);

        ServicioTemp servicio =
                new ServicioTemp(
                        LocalDate.of(2099, 1, 15),
                        "Corte de pelo",
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
                "La visita utilizada como dato de prueba debe registrarse correctamente"
        );

        var visita =
                visitaService.obtenerVisitaPorTurno(
                        turno.getIdTurno()
                );

        assertNotNull(
                visita,
                "Debe existir la visita generada"
        );

        return visita.getIdVisita();
    }

    // ============================================================
    // TEST 1
    // Se muestra listado de visitas previas
    // ============================================================

    @Test
    void historialMuestraVisitasPrevias() throws Exception {

        registrarVisitaDePrueba(
                LocalTime.of(10, 0),
                "Primera visita del historial"
        );

        registrarVisitaDePrueba(
                LocalTime.of(11, 0),
                "Segunda visita del historial"
        );

        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(
                        cliente.getIdCliente()
                );

        assertNotNull(
                historial,
                "El historial no debe ser null"
        );

        assertEquals(
                2,
                historial.size(),
                "El historial debe mostrar las dos visitas registradas"
        );
    }

    // ============================================================
    // TEST 2
    // Incluye fecha, servicio, estilista y observaciones
    // ============================================================

    @Test
    void historialIncluyeFechaServicioEstilistaYObservaciones()
            throws Exception {

        String observacion =
                "Cabello seco. Se recomienda tratamiento hidratante.";

        registrarVisitaDePrueba(
                LocalTime.of(10, 0),
                observacion
        );

        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(
                        cliente.getIdCliente()
                );

        assertFalse(
                historial.isEmpty(),
                "Debe existir al menos una visita en el historial"
        );

        HistorialView registro =
                historial.stream()
                        .filter(h ->
                                observacion.equals(
                                        h.getObservaciones()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(
                registro,
                "Debe encontrarse en el historial la visita registrada"
        );

        /*
         * Fecha de la visita
         */
        assertNotNull(
                registro.getFechaHora(),
                "El historial debe incluir la fecha y hora de la visita"
        );

        /*
         * Servicio realizado
         */
        assertEquals(
                "Corte de pelo",
                registro.getNombreServicio(),
                "El historial debe incluir el servicio realizado"
        );

        /*
         * Profesional / estilista.
         *
         * La consulta real del sistema obtiene p.nombre,
         * por eso el valor esperado es 'Estela'.
         */
        assertEquals(
                "Estela",
                registro.getNombreEstilista(),
                "El historial debe incluir el estilista que realizó la atención"
        );

        /*
         * Observaciones técnicas
         */
        assertEquals(
                observacion,
                registro.getObservaciones(),
                "El historial debe incluir las observaciones de la atención"
        );
    }

    // ============================================================
    // TEST 3
    // El historial corresponde al cliente consultado
    // ============================================================

    @Test
    void historialCorrespondeAlClienteConsultado()
            throws Exception {

        String observacion =
                "Visita correspondiente al cliente Juan Perez";

        registrarVisitaDePrueba(
                LocalTime.of(10, 0),
                observacion
        );

        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(
                        cliente.getIdCliente()
                );

        assertFalse(
                historial.isEmpty(),
                "El cliente debe tener la visita registrada en su historial"
        );

        boolean visitaEncontrada =
                historial.stream()
                        .anyMatch(h ->
                                observacion.equals(
                                        h.getObservaciones()
                                )
                        );

        assertTrue(
                visitaEncontrada,
                "El historial consultado debe contener la visita del cliente"
        );
    }

    // ============================================================
    // TEST 4 - NEGATIVO
    // Cliente sin visitas
    // ============================================================

    @Test
    void historialClienteSinVisitasDevuelveListaVacia() {

        /*
         * Cliente fijo de la BD:
         * ID 11 - DNI 40000002
         *
         * En seed_test_data.sql no se registran visitas
         * para este cliente.
         */
        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(11);

        assertNotNull(
                historial,
                "El servicio debe devolver una lista, no null"
        );

        assertTrue(
                historial.isEmpty(),
                "Un cliente sin visitas debe tener un historial vacío"
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