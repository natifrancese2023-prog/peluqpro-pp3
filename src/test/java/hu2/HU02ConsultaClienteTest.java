package hu2;

import claseslogicas.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClienteService;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class HU02ConsultaClienteTest {

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService();
    }

    /**
     * HU2 - Criterio de aceptación:
     * Se puede buscar por documento.
     *
     * Datos de prueba:
     * Tipo documento: DNI
     * Documento: 40000001
     *
     * El documento 40000001 corresponde al cliente activo
     * Juan Perez de la base de datos de prueba.
     */
    @Test
    void consultarClientePorDocumento() throws SQLException {

        String tipoDocumento = "DNI";
        String numeroDocumento = "40000001";

        Cliente cliente = clienteService.buscarPorDocumento(
                tipoDocumento,
                numeroDocumento
        );

        assertNotNull(
                cliente,
                "Debe encontrarse un cliente al buscar por su documento"
        );

        assertEquals(
                numeroDocumento,
                cliente.getNumeroDocumento(),
                "El documento del cliente encontrado debe coincidir con el buscado"
        );
    }

    /**
     * HU2 - Criterio de aceptación:
     * Se muestra nombre, apellido y fecha de alta.
     *
     * Datos de prueba:
     * Tipo documento: DNI
     * Documento: 40000001
     * Cliente esperado: Juan Perez
     */
    @Test
    void consultarClienteDevuelveNombreApellidoYFechaAlta() throws SQLException {

        String tipoDocumento = "DNI";
        String numeroDocumento = "40000001";

        Cliente cliente = clienteService.buscarPorDocumento(
                tipoDocumento,
                numeroDocumento
        );

        assertNotNull(
                cliente,
                "Debe encontrarse el cliente"
        );

        assertAll(
                "Datos mostrados del cliente",
                () -> assertEquals(
                        "Juan",
                        cliente.getNombre(),
                        "El nombre debe coincidir con el cliente consultado"
                ),
                () -> assertEquals(
                        "Perez",
                        cliente.getApellido(),
                        "El apellido debe coincidir con el cliente consultado"
                ),
                () -> assertNotNull(
                        cliente.getFechaAlta(),
                        "La fecha de alta debe estar informada"
                )
        );
    }

    /**
     * Prueba negativa complementaria.
     *
     * Verifica que la consulta no devuelva un cliente
     * cuando el documento no existe.
     */
    @Test
    void consultarClienteConDocumentoInexistente() throws SQLException {

        String tipoDocumento = "DNI";
        String numeroDocumento = "999999999";

        Cliente cliente = clienteService.buscarPorDocumento(
                tipoDocumento,
                numeroDocumento
        );

        assertNull(
                cliente,
                "La consulta debe devolver null cuando no existe un cliente con ese documento"
        );
    }

    /**
     * Prueba de validaciones implementadas en ClienteService.
     *
     * Estas validaciones son utilizadas por AltaClienteController
     * antes de registrar un cliente.
     *
     * Se comprueban:
     * - Nombre con números.
     * - Apellido con números.
     * - Email con formato inválido.
     * - Teléfono con letras.
     * - Documento con letras.
     */
    @Test
    void validarDatosCliente() {

        assertAll(
                "Validaciones de datos de cliente",

                () -> assertFalse(
                        clienteService.validarNombre("Juan123"),
                        "El nombre no debe permitir números"
                ),

                () -> assertFalse(
                        clienteService.validarNombre("Perez123"),
                        "El apellido no debe permitir números"
                ),

                () -> assertNotNull(
                        clienteService.validarEmail("correo-invalido"),
                        "El email con formato inválido debe ser rechazado"
                ),

                () -> assertNotNull(
                        clienteService.validarTelefono("35725ABC"),
                        "El teléfono con letras debe ser rechazado"
                ),

                () -> assertNotNull(
                        clienteService.validarDocumento("30A12345"),
                        "El documento con letras debe ser rechazado"
                )
        );
    }
}