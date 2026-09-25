package support;

import claseslogicas.Cliente;
import claseslogicas.EstadoTurno;
import claseslogicas.Servicio;
import claseslogicas.Turno;
import dao.ClienteDAO;
import dao.TurnoDAO;
import service.ClienteService;
import service.TurnoService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Builders reutilizables de datos de prueba, sobre los Service/DAO reales. */
public final class TestFixtures {

    public static final int ID_EMPLEADO_ESTILISTA = 3; // "estilista1" del seed
    public static final int ID_EMPLEADO_ESTILISTA_2 = 4; // "Marcos Estilista2" del seed, para probar filtros
    public static final int ID_SERVICIO_CORTE = 1;      // "Corte de pelo", 30 min, $8000
    public static final int ID_SERVICIO_COLORACION = 2; // "Coloración", 90 min, $25000

    private TestFixtures() {}

    public static Cliente crearClienteActivo(String documento) throws SQLException {
        Cliente c = new Cliente();
        c.setNombre("Fixture");
        c.setApellido("Cliente" + documento);
        c.setTelefono("3572500900");
        c.setEmail("fixture." + documento + "@test.com");
        c.setCalle("Calle Fixture");
        c.setNumero("1");
        c.setNombreBarrio("Centro (San Martín BA)");
        c.setNombreTipoDocumento("DNI");
        c.setNumeroDocumento(documento);

        ClienteService.ResultadoAlta resultado = new ClienteService().registrarCliente(c);
        if (resultado != ClienteService.ResultadoAlta.OK) {
            throw new IllegalStateException("No se pudo crear cliente de fixture: " + resultado);
        }
        return c;
    }

    /** Turno PENDIENTE recién agendado, con el servicio de Corte, para el estilista dado. */
    public static Turno crearTurnoPendiente(int idCliente, int idEmpleado, LocalDate fecha, LocalTime horaInicio) throws SQLException {
        Turno turno = new Turno();
        turno.setIdCliente(idCliente);
        turno.setIdEmpleado(idEmpleado);
        turno.setFecha(fecha);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaInicio.plusMinutes(30));
        turno.setObservaciones("Turno de test");

        Servicio corte = new Servicio();
        corte.setIdServicio(ID_SERVICIO_CORTE);
        turno.setServicios(List.of(corte));

        boolean ok = new TurnoService().registrarTurno(turno);
        if (!ok) throw new IllegalStateException("No se pudo crear turno de fixture");
        return turno;
    }

    /** Turno PENDIENTE recién agendado, con el servicio de Corte, para el estilista por defecto. */
    public static Turno crearTurnoPendiente(int idCliente, LocalDate fecha, LocalTime horaInicio) throws SQLException {
        return crearTurnoPendiente(idCliente, ID_EMPLEADO_ESTILISTA, fecha, horaInicio);
    }

    /** Turno CONFIRMADO (pendiente -> confirmado a través del Service real). */
    public static Turno crearTurnoConfirmado(int idCliente, LocalDate fecha, LocalTime horaInicio) throws SQLException {
        Turno turno = crearTurnoPendiente(idCliente, fecha, horaInicio);
        new TurnoService().cambiarEstado(turno, EstadoTurno.CONFIRMADO, "Confirmado por fixture de test");
        return turno;
    }

    /**
     * Circuito completo turno -> visita (auto-genera factura FACTURADA) -> cobro
     * (pasa a PAGADA con el método de pago indicado). Devuelve la Factura ya
     * pagada, útil para poblar reportes/facturación en los tests.
     */
    public static claseslogicas.Factura crearFacturaPagada(int idCliente, LocalDate fecha, LocalTime horaInicio,
                                                            String nombreServicio, String nombreMetodoPago) throws SQLException {
        Turno turno = crearTurnoConfirmado(idCliente, fecha, horaInicio);
        var servicios = javafx.collections.FXCollections.observableArrayList(
                new claseslogicas.ServicioTemp(fecha, nombreServicio, "Estela Estilista", "", "Pendiente")
        );
        boolean ok = new service.VisitaService().registrarVisita(
                new ClienteDAO().obtenerPorId(idCliente), servicios, ID_EMPLEADO_ESTILISTA, turno.getIdTurno());
        if (!ok) throw new IllegalStateException("No se pudo registrar la visita del fixture de factura");

        service.FacturaService facturaService = new service.FacturaService();
        claseslogicas.Factura factura = facturaService.obtenerPorTurno(turno.getIdTurno());
        if (factura == null) throw new IllegalStateException("La visita no generó factura automáticamente");

        java.math.BigDecimal montoFinal = facturaService.calcularMontoFinal(factura.getMontoTotal(), nombreMetodoPago);
        facturaService.cobrarFactura(factura.getIdFactura(), nombreMetodoPago, montoFinal);

        return facturaService.obtenerPorTurno(turno.getIdTurno());
    }
}
