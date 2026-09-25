package hu17;

import claseslogicas.Cliente;
import claseslogicas.EstadoTurno;
import claseslogicas.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.TurnoService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU17 - Gestión de estados de turno.
 * Estados: Pendiente -> Confirmado -> Finalizado -> Facturado, con Cancelado
 * alcanzable desde Pendiente o Confirmado. Reglas verificadas contra
 * Turno.puedeCambiarA() (claseslogicas/Turno.java), que es la fuente de
 * verdad real usada por TurnoService.cambiarEstado().
 */
class HU17GestionEstadosTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    private Turno turnoPendiente() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        return TestFixtures.crearTurnoPendiente(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("HU17 - Positivo: un turno nuevo se visualiza en estado Pendiente")
    void turnoNuevoQuedaEnPendiente() throws SQLException {
        Turno turno = turnoPendiente();
        assertEquals(EstadoTurno.PENDIENTE, turnoService.obtenerPorId(turno.getIdTurno()).getEstadoTurno());
    }

    @Test
    @DisplayName("HU17 - Positivo: Pendiente -> Confirmado es una transición válida y se confirma")
    void pendienteAConfirmadoEsValida() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "Confirmado por recepción");
        assertEquals(EstadoTurno.CONFIRMADO, turnoService.obtenerPorId(turno.getIdTurno()).getEstadoTurno());
    }

    @Test
    @DisplayName("HU17 - Positivo: Confirmado -> Finalizado es válida, y al finalizar se habilita la facturación")
    void confirmadoAFinalizadoHabilitaFacturar() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "ok");
        turnoService.cambiarEstado(turno, EstadoTurno.FINALIZADO, "Servicio realizado");

        Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());
        assertEquals(EstadoTurno.FINALIZADO, recuperado.getEstadoTurno());
        assertTrue(recuperado.puedeCambiarA(EstadoTurno.FACTURADO),
                "Desde FINALIZADO debe habilitarse la transición a FACTURADO");
    }

    @Test
    @DisplayName("HU17 - Positivo: Pendiente -> Cancelado es una transición válida")
    void pendienteACanceladoEsValida() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "El cliente canceló");
        assertEquals(EstadoTurno.CANCELADO, turnoService.obtenerPorId(turno.getIdTurno()).getEstadoTurno());
    }

    @Test
    @DisplayName("HU17 - Positivo: Confirmado -> Cancelado es una transición válida")
    void confirmadoACanceladoEsValida() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "ok");
        turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "Se canceló tras confirmar");
        assertEquals(EstadoTurno.CANCELADO, turnoService.obtenerPorId(turno.getIdTurno()).getEstadoTurno());
    }

    @Test
    @DisplayName("HU17 - Negativo: un turno FINALIZADO queda bloqueado para volver a Pendiente/Confirmado/Cancelado")
    void turnoFinalizadoQuedaBloqueadoParaEsosCambios() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "ok");
        turnoService.cambiarEstado(turno, EstadoTurno.FINALIZADO, "ok");

        assertThrows(IllegalStateException.class, () -> turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "x"));
        assertThrows(IllegalStateException.class, () -> turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "x"));
        assertThrows(IllegalStateException.class, () -> turnoService.cambiarEstado(turno, EstadoTurno.PENDIENTE, "x"));
    }

    @Test
    @DisplayName("HU17 - Negativo: transición inválida Pendiente -> Finalizado (saltea Confirmado) es rechazada")
    void pendienteAFinalizadoDirectoEsRechazada() throws SQLException {
        Turno turno = turnoPendiente();
        assertThrows(IllegalStateException.class,
                () -> turnoService.cambiarEstado(turno, EstadoTurno.FINALIZADO, "salteando confirmado"));
    }

    @Test
    @DisplayName("HU17 - Negativo: transición inválida Pendiente -> Facturado (saltea todo el circuito) es rechazada")
    void pendienteAFacturadoDirectoEsRechazada() throws SQLException {
        Turno turno = turnoPendiente();
        assertThrows(IllegalStateException.class,
                () -> turnoService.cambiarEstado(turno, EstadoTurno.FACTURADO, "salteando todo"));
    }

    @Test
    @DisplayName("HU17 - Negativo: CANCELADO es un estado terminal, no admite ninguna transición posterior")
    void canceladoEsTerminal() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "cancelado");

        for (EstadoTurno destino : EstadoTurno.values()) {
            if (destino == EstadoTurno.CANCELADO) continue;
            assertThrows(IllegalStateException.class, () -> turnoService.cambiarEstado(turno, destino, "x"),
                    "CANCELADO no debe poder pasar a " + destino);
        }
    }

    @Test
    @DisplayName("HU17 - Límite: FACTURADO es un estado terminal, no admite ninguna transición posterior")
    void facturadoEsTerminal() throws SQLException {
        Turno turno = turnoPendiente();
        turnoService.cambiarEstado(turno, EstadoTurno.CONFIRMADO, "ok");
        turnoService.cambiarEstado(turno, EstadoTurno.FINALIZADO, "ok");
        turnoService.cambiarEstado(turno, EstadoTurno.FACTURADO, "facturado directo (vía cambiarEstado, no vía visita)");

        for (EstadoTurno destino : EstadoTurno.values()) {
            if (destino == EstadoTurno.FACTURADO) continue;
            assertThrows(IllegalStateException.class, () -> turnoService.cambiarEstado(turno, destino, "x"),
                    "FACTURADO no debe poder pasar a " + destino);
        }
    }
}
