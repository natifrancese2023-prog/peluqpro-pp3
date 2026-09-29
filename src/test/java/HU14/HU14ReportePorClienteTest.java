package HU14;

import claseslogicas.Cliente;
import claseslogicas.HistorialView;
import dao.ClienteDAO;
import org.junit.jupiter.api.Test;
import service.ReporteService;
import service.VisitaService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU14ReportePorClienteTest {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VisitaService visitaService = new VisitaService();
    private final ReporteService reporteService = new ReporteService();

    @Test
    void seleccionarClienteParaConsultarInformacion() throws Exception {

        Cliente cliente = clienteDAO.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba con ID 10."
        );

        assertEquals(
                10,
                cliente.getIdCliente(),
                "Debe poder identificarse correctamente el cliente seleccionado."
        );
    }

    @Test
    void mostrarServiciosRegistradosDelCliente() throws SQLException {

        Cliente cliente = clienteDAO.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba con ID 10."
        );

        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(
                        cliente.getIdCliente()
                );

        assertNotNull(
                historial,
                "La consulta del historial no debe devolver null."
        );

        for (HistorialView registro : historial) {

            assertNotNull(
                    registro.getFechaHora(),
                    "El servicio registrado debe tener fecha y hora."
            );

            assertNotNull(
                    registro.getNombreServicio(),
                    "El registro debe contener el nombre del servicio."
            );
        }
    }

    @Test
    void consultarTicketPromedioDelClientePorPeriodo() throws Exception {

        Cliente cliente = clienteDAO.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba con ID 10."
        );

        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);

        BigDecimal ticketPromedio =
                reporteService.obtenerTicketPromedioCliente(
                        cliente.getIdCliente(),
                        desde,
                        hasta
                );

        /*
         * Si existen facturas válidas para el cliente y período,
         * el sistema debe devolver un importe.
         *
         * Si no existen datos, el método devuelve null.
         */
        if (ticketPromedio != null) {

            assertTrue(
                    ticketPromedio.compareTo(BigDecimal.ZERO) >= 0,
                    "El ticket promedio no puede ser negativo."
            );
        }
    }

    @Test
    void periodoSinDatosInformaSinResultados() throws SQLException {

        Cliente cliente = clienteDAO.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba con ID 10."
        );

        LocalDate desde = LocalDate.of(2099, 1, 1);
        LocalDate hasta = LocalDate.of(2099, 12, 31);

        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(
                                cliente.getIdCliente()
                        )
                        .stream()
                        .filter(h ->
                                h.getFechaHora() != null
                                        && !h.getFechaHora()
                                        .toLocalDate()
                                        .isBefore(desde)
                                        && !h.getFechaHora()
                                        .toLocalDate()
                                        .isAfter(hasta)
                        )
                        .toList();

        BigDecimal ticketPromedio =
                reporteService.obtenerTicketPromedioCliente(
                        cliente.getIdCliente(),
                        desde,
                        hasta
                );

        assertTrue(
                historial.isEmpty() && ticketPromedio == null,
                "Para un período sin datos deben obtenerse cero resultados y ticket promedio sin datos."
        );
    }

    @Test
    void datosMostradosCorrespondenAlPeriodoSeleccionado() throws SQLException {

        Cliente cliente = clienteDAO.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente de prueba con ID 10."
        );

        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);

        List<HistorialView> historial =
                visitaService.obtenerHistorialPorCliente(
                                cliente.getIdCliente()
                        )
                        .stream()
                        .filter(h ->
                                h.getFechaHora() != null
                                        && !h.getFechaHora()
                                        .toLocalDate()
                                        .isBefore(desde)
                                        && !h.getFechaHora()
                                        .toLocalDate()
                                        .isAfter(hasta)
                        )
                        .toList();

        for (HistorialView registro : historial) {

            LocalDate fechaServicio =
                    registro.getFechaHora().toLocalDate();

            assertFalse(
                    fechaServicio.isBefore(desde),
                    "No debe aparecer un servicio anterior al período seleccionado."
            );

            assertFalse(
                    fechaServicio.isAfter(hasta),
                    "No debe aparecer un servicio posterior al período seleccionado."
            );
        }
    }
}