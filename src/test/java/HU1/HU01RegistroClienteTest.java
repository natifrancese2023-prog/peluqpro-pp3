package HU1;



import claseslogicas.Cliente;
import dao.ClienteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClienteService;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class HU01RegistroClienteTest {

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService();
    }

    /**
     * HU1 - Criterio: Se ingresan datos del cliente.
     * HU1 - Criterio: El cliente se guarda correctamente.
     *
     * Verifica que un cliente nuevo pueda registrarse correctamente
     * y que quede almacenado en la base de datos.
     */
    @Test
    void registrarClienteCorrectamente() throws SQLException {

        String documento = "9" + System.currentTimeMillis();

        Cliente cliente = crearClienteValido(documento);

        ClienteService.ResultadoAlta resultado =
                clienteService.registrarCliente(cliente);

        assertEquals(
                ClienteService.ResultadoAlta.OK,
                resultado,
                "El registro de un cliente nuevo debe devolver OK"
        );

        assertTrue(
                cliente.getIdCliente() > 0,
                "El cliente registrado debe recibir un ID"
        );

        Cliente clienteGuardado =
                new ClienteDAO().obtenerPorId(cliente.getIdCliente());

        assertNotNull(
                clienteGuardado,
                "El cliente debe quedar almacenado en la base de datos"
        );

        assertEquals(
                cliente.getNombre(),
                clienteGuardado.getNombre(),
                "El nombre almacenado debe coincidir con el ingresado"
        );

        assertEquals(
                cliente.getApellido(),
                clienteGuardado.getApellido(),
                "El apellido almacenado debe coincidir con el ingresado"
        );

        assertEquals(
                documento,
                clienteGuardado.getNumeroDocumento(),
                "El documento almacenado debe coincidir con el ingresado"
        );
    }

    /**
     * Prueba negativa complementaria de HU1.
     *
     * Verifica que el sistema no permita registrar dos clientes
     * activos utilizando el mismo documento.
     */
    @Test
    void registrarClienteConDocumentoDuplicado() throws SQLException {

        String documento = "9" + System.currentTimeMillis();

        Cliente primerCliente = crearClienteValido(documento);

        ClienteService.ResultadoAlta primerResultado =
                clienteService.registrarCliente(primerCliente);

        assertEquals(
                ClienteService.ResultadoAlta.OK,
                primerResultado,
                "El primer cliente debe registrarse correctamente"
        );

        Cliente segundoCliente = crearClienteValido(documento);

        ClienteService.ResultadoAlta segundoResultado =
                clienteService.registrarCliente(segundoCliente);

        assertEquals(
                ClienteService.ResultadoAlta.DUPLICADO,
                segundoResultado,
                "No debe permitirse registrar otro cliente activo con el mismo documento"
        );
    }

    /**
     * Prueba de validación relacionada con los datos ingresados en HU1.
     *
     * Verifica que un email con formato incorrecto sea rechazado.
     */
    @Test
    void validarEmailInvalido() {

        String resultado =
                clienteService.validarEmail("correo-invalido");

        assertNotNull(
                resultado,
                "El sistema debe detectar un email con formato inválido"
        );
    }

    /**
     * Crea un cliente con los datos mínimos necesarios
     * para realizar el alta mediante ClienteService.
     */
    private Cliente crearClienteValido(String documento) {

        Cliente cliente = new Cliente();

        cliente.setNombre("ClienteTest");
        cliente.setApellido("HU1");
        cliente.setTelefono("3572500999");
        cliente.setEmail("cliente" + documento + "@test.com");

        cliente.setNombreTipoDocumento("DNI");
        cliente.setNumeroDocumento(documento);

        cliente.setCalle("Calle Test");
        cliente.setNumero("123");

        cliente.setNombreProvincia("Buenos Aires");
        cliente.setNombreCiudad("San Martín");
        cliente.setNombreBarrio("Centro (San Martín BA)");

        return cliente;
    }
}