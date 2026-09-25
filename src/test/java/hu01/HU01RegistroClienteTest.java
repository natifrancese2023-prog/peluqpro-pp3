package hu01;

import claseslogicas.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ClienteService;
import service.ClienteService.ResultadoAlta;
import support.TestDbSupport;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU1 - Registro de cliente
 * CA: se ingresan datos del cliente / el cliente se guarda correctamente /
 *     se muestra confirmación (a nivel Service: ResultadoAlta.OK).
 */
class HU01RegistroClienteTest {

    private final ClienteService clienteService = new ClienteService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    private Cliente clienteValido(String documento) {
        Cliente c = new Cliente();
        c.setNombre("Test");
        c.setApellido("Cliente" + documento);
        c.setTelefono("3572500999");
        c.setEmail("test." + documento + "@test.com");
        c.setCalle("Calle Falsa");
        c.setNumero("123");
        c.setNombreBarrio("Centro (San Martín BA)");
        c.setNombreTipoDocumento("DNI");
        c.setNumeroDocumento(documento);
        return c;
    }

    @Test
    @DisplayName("HU1 - Positivo: alta de cliente nuevo con datos válidos se guarda correctamente")
    void altaClienteNuevoSeGuardaCorrectamente() throws SQLException {
        String doc = TestDbSupport.documentoUnico();
        Cliente c = clienteValido(doc);

        ResultadoAlta resultado = clienteService.registrarCliente(c);

        assertEquals(ResultadoAlta.OK, resultado, "El alta de un cliente nuevo debe devolver OK");
        assertTrue(c.getIdCliente() > 0, "Debe quedar asignado un id_cliente real tras el insert");

        Cliente recuperado = new dao.ClienteDAO().obtenerPorId(c.getIdCliente());
        assertNotNull(recuperado, "El cliente insertado debe poder recuperarse de la base");
        assertEquals("Test", recuperado.getNombre());
        assertEquals(doc, recuperado.getNumeroDocumento());
    }

    @Test
    @DisplayName("HU1 - Negativo: alta con documento ya existente y activo es rechazada (DUPLICADO)")
    void altaConDocumentoActivoExistenteEsRechazada() throws SQLException {
        String doc = TestDbSupport.documentoUnico();
        Cliente original = clienteValido(doc);
        assertEquals(ResultadoAlta.OK, clienteService.registrarCliente(original));

        Cliente duplicado = clienteValido(doc); // mismo tipo+número de documento
        ResultadoAlta resultado = clienteService.registrarCliente(duplicado);

        assertEquals(ResultadoAlta.DUPLICADO, resultado,
                "Un segundo alta con el mismo documento activo debe rechazarse como DUPLICADO");
    }

    @Test
    @DisplayName("HU1 - Límite: alta con documento de un cliente inactivo devuelve DUPLICADO_INACTIVO, no crea uno nuevo")
    void altaConDocumentoDeClienteInactivoDevuelveDuplicadoInactivo() throws SQLException {
        // El cliente id 11 del seed está inactivo, documento 40000002 / DNI.
        Cliente c = new Cliente();
        c.setNombre("Reintento");
        c.setApellido("Cliente");
        c.setTelefono("3572500998");
        c.setEmail("reintento@test.com");
        c.setCalle("Calle Falsa");
        c.setNumero("999");
        c.setNombreBarrio("Barrio Cívico (San Martín Mendoza)");
        c.setNombreTipoDocumento("DNI");
        c.setNumeroDocumento("40000002");

        ResultadoAlta resultado = clienteService.registrarCliente(c);

        assertEquals(ResultadoAlta.DUPLICADO_INACTIVO, resultado,
                "El service debe distinguir un documento de cliente inactivo (DUPLICADO_INACTIVO) de uno activo (DUPLICADO)");
    }

    @Test
    @DisplayName("HU1 - Negativo: email inválido es rechazado por la validación del Service")
    void validarEmailRechazaFormatoInvalido() {
        assertNotNull(clienteService.validarEmail("no-es-un-email"),
                "validarEmail debe devolver un mensaje de error para un formato inválido");
        assertNull(clienteService.validarEmail("valido@dominio.com"),
                "validarEmail no debe devolver error para un email válido");
    }
}
