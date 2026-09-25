package hu10;

import claseslogicas.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ClienteService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/** HU10 - Modificación de cliente: editar datos, guardar, validar que se reflejen. */
class HU10ModificacionClienteTest {

    private final ClienteService clienteService = new ClienteService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU10 - Positivo: modificar teléfono y email se guarda y se refleja al recuperar el cliente")
    void modificarDatosSeGuardaYSeRefleja() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        c.setTelefono("3572999999");
        c.setEmail("nuevo.email@test.com");
        boolean actualizado = clienteService.actualizarCliente(c);

        assertTrue(actualizado);
        Cliente recuperado = clienteService.obtenerPorId(c.getIdCliente());
        assertEquals("3572999999", recuperado.getTelefono());
        assertEquals("nuevo.email@test.com", recuperado.getEmail());
    }

    @Test
    @DisplayName("HU10 - Positivo: modificar la ciudad/barrio del cliente (cambio de domicilio completo) se refleja")
    void modificarBarrioSeReflejaCorrectamente() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        assertEquals("Centro (San Martín BA)", c.getNombreBarrio());

        c.setNombreBarrio("Barrio Cívico (San Martín Mendoza)");
        clienteService.actualizarCliente(c);

        Cliente recuperado = clienteService.obtenerPorId(c.getIdCliente());
        assertEquals("Barrio Cívico (San Martín Mendoza)", recuperado.getNombreBarrio(),
                "El cambio de barrio (incluyendo cruzar de provincia, caso homónimo #18) debe reflejarse");
    }

    @Test
    @DisplayName("HU10 - Negativo: modificar un cliente sin idPersona válido (objeto mal formado) es rechazado")
    void modificarClienteSinIdPersonaEsRechazado() throws SQLException {
        Cliente incompleto = new Cliente();
        incompleto.setIdCliente(1);
        // idPersona queda en 0 -> actualizar() debe rechazarlo (ver ClienteDAO.actualizar)

        boolean actualizado = clienteService.actualizarCliente(incompleto);

        assertFalse(actualizado, "Sin idPersona > 0, ClienteDAO.actualizar() debe devolver false sin tocar la base");
    }

    @Test
    @DisplayName("HU10 - Negativo: modificar con un barrio inexistente lanza SQLException, no corrompe el dato original")
    void modificarConBarrioInexistenteLanzaExcepcionYNoCorrompe() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        c.setNombreBarrio("Barrio Que No Existe En Ningún Lado");

        assertThrows(SQLException.class, () -> clienteService.actualizarCliente(c));

        Cliente recuperado = clienteService.obtenerPorId(c.getIdCliente());
        assertEquals("Centro (San Martín BA)", recuperado.getNombreBarrio(),
                "El update debe haber hecho rollback: el barrio original no debe haberse perdido");
    }
}
