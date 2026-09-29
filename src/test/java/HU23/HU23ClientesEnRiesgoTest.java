package HU23;

import claseslogicas.ClienteRiesgo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import service.ReporteService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HU23ClientesEnRiesgoTest {

    private static ReporteService reporteService;

    @BeforeAll
    static void iniciar() {
        reporteService = new ReporteService();
    }

    // =========================================================
    // 1. CONSULTAR LA ÚLTIMA VISITA DE CADA CLIENTE
    // =========================================================

    @Test
    @Order(1)
    void consultarUltimaVisitaDeCadaCliente() throws Exception {

        List<ClienteRiesgo> clientes =
                reporteService.obtenerClientesEnRiesgo();

        assertNotNull(
                clientes,
                "La consulta de clientes en riesgo no debe devolver null."
        );

        assertFalse(
                clientes.isEmpty(),
                "Debe existir al menos un cliente en riesgo en la BD de prueba."
        );

        for (ClienteRiesgo cliente : clientes) {

            assertNotNull(
                    cliente.getUltimaVisita(),
                    "Cada cliente en riesgo debe tener registrada su última visita."
            );
        }
    }

    // =========================================================
    // 2. IDENTIFICAR CLIENTES CON MÁS DE TRES MESES
    // =========================================================

    @Test
    @Order(2)
    void identificarClientesConMasDeTresMesesSinVisitar() throws Exception {

        List<ClienteRiesgo> clientes =
                reporteService.obtenerClientesEnRiesgo();

        assertFalse(
                clientes.isEmpty(),
                "Debe existir al menos un cliente en riesgo."
        );

        LocalDateTime limite =
                LocalDateTime.now().minusMonths(3);

        for (ClienteRiesgo cliente : clientes) {

            assertTrue(
                    cliente.getUltimaVisita().isBefore(limite),
                    "La última visita del cliente "
                            + cliente.getIdCliente()
                            + " debe ser anterior a tres meses."
            );
        }
    }

    // =========================================================
    // 3. MOSTRAR INFORMACIÓN PARA IDENTIFICAR AL CLIENTE
    // =========================================================

    @Test
    @Order(3)
    void mostrarInformacionNecesariaParaIdentificarCliente()
            throws Exception {

        List<ClienteRiesgo> clientes =
                reporteService.obtenerClientesEnRiesgo();

        assertFalse(
                clientes.isEmpty(),
                "Debe existir información de clientes en riesgo."
        );

        for (ClienteRiesgo cliente : clientes) {

            assertTrue(
                    cliente.getIdCliente() > 0,
                    "El cliente debe tener un ID válido."
            );

            assertNotNull(
                    cliente.getNombreCompleto(),
                    "El cliente debe tener nombre."
            );

            assertFalse(
                    cliente.getNombreCompleto().trim().isEmpty(),
                    "El nombre del cliente no puede estar vacío."
            );
        }
    }

    // =========================================================
    // 4. EL LISTADO CORRESPONDE A CLIENTES EN RIESGO
    // =========================================================

    @Test
    @Order(4)
    void listadoContieneUnicamenteClientesEnRiesgo()
            throws Exception {

        List<ClienteRiesgo> clientes =
                reporteService.obtenerClientesEnRiesgo();

        assertFalse(
                clientes.isEmpty(),
                "Debe existir al menos un cliente en riesgo."
        );

        LocalDateTime limite =
                LocalDateTime.now().minusMonths(3);

        for (ClienteRiesgo cliente : clientes) {

            assertNotNull(
                    cliente.getUltimaVisita(),
                    "El cliente debe tener fecha de última visita."
            );

            assertTrue(
                    cliente.getUltimaVisita().isBefore(limite),
                    "El listado no debe incluir clientes cuya última visita "
                            + "sea de tres meses o menos."
            );
        }
    }
}
