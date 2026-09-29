package HU08;

import claseslogicas.Cliente;
import org.junit.jupiter.api.Test;
import service.ClienteService;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class HU08ModificacionClienteTest {

    private final ClienteService clienteService = new ClienteService();

    /**
     * HU8 - Criterio: Se puede editar los datos.
     *
     * Verifica que los datos de un cliente existente puedan modificarse.
     *
     * Datos de prueba:
     * Cliente ID 10 - Juan Perez - DNI 40000001
     */
    @Test
    void editarDatosCliente() throws SQLException {

        Cliente cliente = clienteService.obtenerPorId(10);

        assertNotNull(
                cliente,
                "Debe existir el cliente ID 10 en la BD de prueba"
        );

        String nombreOriginal = cliente.getNombre();
        String apellidoOriginal = cliente.getApellido();
        String telefonoOriginal = cliente.getTelefono();
        String emailOriginal = cliente.getEmail();

        try {
            cliente.setNombre("Juan Modificado");
            cliente.setApellido("Perez Modificado");
            cliente.setTelefono("3572500099");
            cliente.setEmail("juan.modificado@test.com");

            boolean actualizado =
                    clienteService.actualizarCliente(cliente);

            assertTrue(
                    actualizado,
                    "La actualización del cliente debe realizarse correctamente"
            );

        } finally {
            cliente.setNombre(nombreOriginal);
            cliente.setApellido(apellidoOriginal);
            cliente.setTelefono(telefonoOriginal);
            cliente.setEmail(emailOriginal);

            clienteService.actualizarCliente(cliente);
        }
    }

    /**
     * HU8 - Criterio: Se guarda correctamente.
     *
     * Verifica que la operación de actualización retorne correctamente
     * y que el cliente continúe existiendo después de guardar.
     *
     * Datos de prueba:
     * Cliente ID 10 - Juan Perez - DNI 40000001
     */
    @Test
    void guardarModificacionCliente() throws SQLException {

        Cliente cliente = clienteService.obtenerPorId(10);

        assertNotNull(cliente);

        String emailOriginal = cliente.getEmail();

        try {
            cliente.setEmail("juan.guardado@test.com");

            boolean resultado =
                    clienteService.actualizarCliente(cliente);

            assertTrue(
                    resultado,
                    "El sistema debe confirmar que la modificación fue guardada"
            );

            Cliente clienteGuardado =
                    clienteService.obtenerPorId(10);

            assertNotNull(
                    clienteGuardado,
                    "El cliente debe continuar existiendo después de guardar"
            );

        } finally {
            cliente.setEmail(emailOriginal);
            clienteService.actualizarCliente(cliente);
        }
    }

    /**
     * HU8 - Criterio: Se valida que los cambios se reflejen.
     *
     * Verifica consultando nuevamente la BD que los nuevos datos
     * realmente hayan quedado persistidos.
     *
     * Datos de prueba:
     * Cliente ID 10 - Juan Perez - DNI 40000001
     */
    @Test
    void cambiosSeReflejanEnLaBaseDeDatos() throws SQLException {

        Cliente cliente = clienteService.obtenerPorId(10);

        assertNotNull(cliente);

        String telefonoOriginal = cliente.getTelefono();

        try {
            String nuevoTelefono = "3572500088";

            cliente.setTelefono(nuevoTelefono);

            boolean actualizado =
                    clienteService.actualizarCliente(cliente);

            assertTrue(actualizado);

            Cliente clienteConsultado =
                    clienteService.obtenerPorId(10);

            assertNotNull(clienteConsultado);

            assertEquals(
                    nuevoTelefono,
                    clienteConsultado.getTelefono(),
                    "El teléfono modificado debe persistir en la BD"
            );

        } finally {
            cliente.setTelefono(telefonoOriginal);
            clienteService.actualizarCliente(cliente);
        }
    }

    /**
     * Verificación complementaria de las validaciones implementadas.
     *
     * Verifica que el sistema rechace formatos inválidos de email.
     */
    @Test
    void validarEmailInvalido() {

        String error =
                clienteService.validarEmail("correo-invalido");

        assertNotNull(
                error,
                "Un email inválido debe generar un mensaje de validación"
        );
    }

    /**
     * Verificación complementaria de las validaciones implementadas.
     *
     * Verifica que el sistema rechace teléfonos que no cumplen
     * con la cantidad/formato de dígitos requerido.
     */
    @Test
    void validarTelefonoInvalido() {

        String error =
                clienteService.validarTelefono("123");

        assertNotNull(
                error,
                "Un teléfono inválido debe generar un mensaje de validación"
        );
    }
}

