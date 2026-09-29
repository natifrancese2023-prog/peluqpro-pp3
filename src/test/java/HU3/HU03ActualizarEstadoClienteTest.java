package hu3;

import claseslogicas.Cliente;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClienteService;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU03ActualizarEstadoClienteTest {

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService();
    }

    @AfterEach
    void restaurarEstadoInicial() throws SQLException {
        /*
         * Se restaura el estado original del seed:
         * Cliente 10 -> Activo
         * Cliente 11 -> Inactivo
         */

        Cliente cliente10 =
                clienteService.buscarPorDocumentoGeneral("DNI", "40000001");

        if (cliente10 != null && !cliente10.isActivo()) {
            clienteService.reactivarCliente(10);
        }

        Cliente cliente11 =
                clienteService.buscarPorDocumentoGeneral("DNI", "40000002");

        if (cliente11 != null && cliente11.isActivo()) {
            clienteService.eliminarCliente(cliente11);
        }
    }

    @Test
    void consultarEstadoActualCliente() throws SQLException {

        // Se asegura que el cliente esté activo antes de consultar.
        Cliente clienteActual =
                clienteService.buscarPorDocumentoGeneral("DNI", "40000001");

        assertNotNull(
                clienteActual,
                "Debe existir el cliente utilizado para la prueba."
        );

        if (!clienteActual.isActivo()) {
            clienteService.reactivarCliente(10);
        }

        Cliente cliente =
                clienteService.buscarPorDocumentoGeneral("DNI", "40000001");

        assertNotNull(
                cliente,
                "Debe poder consultarse un cliente registrado."
        );

        assertTrue(
                cliente.isActivo(),
                "El estado actual del cliente debe ser activo."
        );
    }

    @Test
    void inactivarCliente() throws SQLException {

        // Precondición: el cliente debe estar activo.
        Cliente cliente =
                clienteService.buscarPorDocumentoGeneral("DNI", "40000001");

        assertNotNull(
                cliente,
                "Debe encontrarse el cliente registrado."
        );

        if (!cliente.isActivo()) {
            clienteService.reactivarCliente(10);
            cliente = clienteService.buscarPorDocumentoGeneral(
                    "DNI",
                    "40000001"
            );
        }

        assertTrue(
                cliente.isActivo(),
                "El cliente debe estar activo antes de modificar su estado."
        );

        // Actuar: cambiar a inactivo.
        boolean resultado =
                clienteService.eliminarCliente(cliente);

        assertTrue(
                resultado,
                "La modificación del estado a inactivo debe realizarse correctamente."
        );

        // Verificar que el cambio quedó guardado.
        Cliente clienteActualizado =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000001"
                );

        assertNotNull(
                clienteActualizado,
                "El cliente debe continuar registrado."
        );

        assertFalse(
                clienteActualizado.isActivo(),
                "El nuevo estado del cliente debe ser inactivo."
        );
    }

    @Test
    void reactivarClienteInactivo() throws SQLException {

        // Precondición: el cliente debe estar inactivo.
        Cliente cliente =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000002"
                );

        assertNotNull(
                cliente,
                "Debe encontrarse el cliente registrado."
        );

        if (cliente.isActivo()) {
            clienteService.eliminarCliente(cliente);

            cliente = clienteService.buscarPorDocumentoGeneral(
                    "DNI",
                    "40000002"
            );
        }

        assertFalse(
                cliente.isActivo(),
                "El cliente debe estar inactivo antes de reactivarlo."
        );

        // Actuar.
        boolean resultado =
                clienteService.reactivarCliente(11);

        assertTrue(
                resultado,
                "La reactivación del cliente debe realizarse correctamente."
        );

        // Verificar.
        Cliente clienteActualizado =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000002"
                );

        assertNotNull(
                clienteActualizado,
                "El cliente debe continuar registrado después de reactivarlo."
        );

        assertTrue(
                clienteActualizado.isActivo(),
                "El nuevo estado del cliente debe ser activo."
        );
    }

    @Test
    void clientePermaneceRegistradoLuegoDeCambiarEstado() throws SQLException {

        Cliente cliente =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000001"
                );

        assertNotNull(
                cliente,
                "El cliente debe existir antes de modificar su estado."
        );

        if (!cliente.isActivo()) {
            clienteService.reactivarCliente(10);

            cliente = clienteService.buscarPorDocumentoGeneral(
                    "DNI",
                    "40000001"
            );
        }

        // Cambiar estado.
        boolean resultado =
                clienteService.eliminarCliente(cliente);

        assertTrue(
                resultado,
                "El cambio de estado debe realizarse correctamente."
        );

        // El registro debe seguir existiendo.
        Cliente clienteActualizado =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000001"
                );

        assertNotNull(
                clienteActualizado,
                "El cliente debe permanecer registrado después del cambio de estado."
        );

        assertEquals(
                10,
                clienteActualizado.getIdCliente(),
                "El ID del cliente debe conservarse."
        );

        assertEquals(
                "40000001",
                clienteActualizado.getNumeroDocumento(),
                "El documento del cliente debe conservarse."
        );
    }

    @Test
    void listarClientesSegunEstado() throws SQLException {

        // Preparar estado conocido:
        // Cliente 10 -> activo
        // Cliente 11 -> inactivo

        Cliente cliente10 =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000001"
                );

        if (cliente10 != null && !cliente10.isActivo()) {
            clienteService.reactivarCliente(10);
        }

        Cliente cliente11 =
                clienteService.buscarPorDocumentoGeneral(
                        "DNI",
                        "40000002"
                );

        if (cliente11 != null && cliente11.isActivo()) {
            clienteService.eliminarCliente(cliente11);
        }

        // Consultar listados.
        List<Cliente> activos =
                clienteService.obtenerPorEstado(true);

        List<Cliente> inactivos =
                clienteService.obtenerPorEstado(false);

        assertNotNull(
                activos,
                "El listado de clientes activos no debe ser nulo."
        );

        assertNotNull(
                inactivos,
                "El listado de clientes inactivos no debe ser nulo."
        );

        assertTrue(
                activos.stream()
                        .anyMatch(cliente -> cliente.getIdCliente() == 10),
                "Juan Perez debe aparecer en el listado de clientes activos."
        );

        assertTrue(
                inactivos.stream()
                        .anyMatch(cliente -> cliente.getIdCliente() == 11),
                "Vieja Inactiva debe aparecer en el listado de clientes inactivos."
        );
    }
}