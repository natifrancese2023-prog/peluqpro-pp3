package hu02;

import claseslogicas.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ClienteService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/** HU2 - Consulta de cliente: buscar por documento, mostrar nombre/apellido/fecha de alta. */
class HU02ConsultaClienteTest {

    private final ClienteService clienteService = new ClienteService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU2 - Positivo: buscar por documento existente devuelve nombre, apellido y fecha de alta")
    void buscarPorDocumentoExistenteDevuelveDatosCompletos() throws SQLException {
        String doc = TestDbSupport.documentoUnico();
        TestFixtures.crearClienteActivo(doc);

        Cliente encontrado = clienteService.buscarPorDocumento("DNI", doc);

        assertNotNull(encontrado, "Debe encontrar el cliente por tipo+número de documento");
        assertEquals("Fixture", encontrado.getNombre());
        assertNotNull(encontrado.getFechaAlta(), "La ficha debe traer la fecha de alta");
    }

    @Test
    @DisplayName("HU2 - Negativo: buscar por documento inexistente no devuelve cliente")
    void buscarPorDocumentoInexistenteNoDevuelveNada() throws SQLException {
        Cliente encontrado = clienteService.buscarPorDocumento("DNI", "99999999999");
        assertNull(encontrado, "Un documento que no existe no debe traer ningún cliente");
    }

    @Test
    @DisplayName("HU2 - Límite: buscarPorDocumento (uso normal de consulta) NO debe devolver clientes inactivos")
    void buscarPorDocumentoNoDevuelveClienteInactivo() throws SQLException {
        // Cliente id 11 del seed: documento 40000002, inactivo.
        Cliente encontrado = clienteService.buscarPorDocumento("DNI", "40000002");
        assertNull(encontrado,
                "La consulta normal de cliente (distinta de la usada para reactivación en Alta) no debe listar inactivos");
    }
}
