package hu08;

import claseslogicas.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import service.ClienteService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU8 - Listado de clientes. */
class HU08ListadoClientesTest {

    private final ClienteService clienteService = new ClienteService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU8 - Positivo: el listado de clientes activos incluye todos los campos relevantes")
    void listadoCompletoIncluyeClientesActivos() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        List<Cliente> listado = clienteService.obtenerPorEstado(true);

        assertTrue(listado.stream().anyMatch(c -> c.getIdCliente() == c1.getIdCliente()));
        assertTrue(listado.stream().anyMatch(c -> c.getIdCliente() == c2.getIdCliente()));
        Cliente encontrado = listado.stream().filter(c -> c.getIdCliente() == c1.getIdCliente()).findFirst().orElseThrow();
        assertNotNull(encontrado.getPersona(), "El listado debe traer los datos de persona embebidos");
        assertNotNull(encontrado.getPersona().getNombre());
        assertNotNull(encontrado.getPersona().getNumeroDocumento());
    }

    @Test
    @DisplayName("HU8 - Negativo: el listado de 'Activos' (obtenerPorEstado(true), filtro por defecto de la pantalla) NO incluye inactivos")
    void listadoActivosNoIncluyeClientesInactivos() throws SQLException {
        List<Cliente> listado = clienteService.obtenerPorEstado(true);
        // id 11 del seed está inactivo
        assertFalse(listado.stream().anyMatch(c -> c.getIdCliente() == 11),
                "obtenerPorEstado(true) debe excluir a los clientes con activo=false");
    }

    @Test
    @DisplayName("HU8 - Nota de diseño: obtenerTodos() trae activos E inactivos a propósito (alimenta el filtro 'Todos' de la pantalla)")
    void obtenerTodosEsUsadoPorElFiltroTodosIncluyendoInactivos() throws SQLException {
        List<Cliente> listado = clienteService.obtenerTodos();
        assertTrue(listado.stream().anyMatch(c -> c.getIdCliente() == 11),
                "obtenerTodos() no filtra por activo -- es el método correcto para el combo 'Todos' de ListarClientesController, "
                        + "no para 'Activos' (que usa obtenerPorEstado(true))");
    }

    @Test
    @DisplayName("HU8 - Exportación: existen los datos extendidos necesarios para exportar el listado")
    void listadoSePuedeExportar() throws SQLException {

        List<claseslogicas.ClienteReporteExtendido> datos =
                clienteService.obtenerDatosClientesExtendido();

        assertNotNull(datos, "Los datos para exportación no deben ser null");
    }
}
