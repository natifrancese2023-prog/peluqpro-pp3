package hu03;

import claseslogicas.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ClienteService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** HU3 - Eliminación de cliente (baja lógica: activo=false). */
class HU03EliminacionClienteTest {

    private final ClienteService clienteService = new ClienteService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU3 - Positivo: eliminar cliente sin turnos lo saca del listado de activos")
    void eliminarClienteSinTurnosLoSacaDelListado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());

        boolean eliminado = clienteService.eliminarCliente(c);

        assertTrue(eliminado, "El service debe confirmar la baja");
        boolean sigueEnActivos = clienteService.obtenerPorEstado(true).stream()
                .anyMatch(x -> x.getIdCliente() == c.getIdCliente());
        assertFalse(sigueEnActivos, "El cliente dado de baja no debe aparecer en el listado de activos");
    }

    @Test
    @DisplayName("HU3 - Límite: eliminar un cliente CON turno asignado no rompe (baja lógica, no hay restricción de FK real)")
    void eliminarClienteConTurnoAsignadoNoFalla() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0));

        boolean eliminado = clienteService.eliminarCliente(c);

        assertTrue(eliminado,
                "ClienteDAO.eliminar() hace baja lógica (UPDATE activo=false); un turno asociado no debe impedirla");
    }

    @Test
    @DisplayName("HU3 - Negativo: eliminar un id de cliente inexistente no debe romper ni afectar otras filas")
    void eliminarClienteInexistenteNoRompe() throws SQLException {
        Cliente fantasma = new Cliente();
        fantasma.setIdCliente(999999);

        assertDoesNotThrow(() -> clienteService.eliminarCliente(fantasma),
                "Un UPDATE ... WHERE id_cliente = 999999 que no matchea ninguna fila no debe tirar excepción");
    }
}
