package hu15;

import claseslogicas.Cliente;
import claseslogicas.EstadoTurno;
import claseslogicas.Servicio;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU15 - Eliminación de turno.
 * En este sistema la "eliminación" es una baja lógica: cambia el estado a
 * CANCELADO (no hay DELETE físico; el motivo se guarda en turno.motivo_log
 * a través del mismo cambio de estado).
 */
class HU15EliminacionTurnoTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU15 - Positivo: cancelar un turno confirma el cambio y conserva el motivo de baja")
    void cancelarTurnoConfirmaYConservaElMotivo() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0));

        turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "El cliente avisó que no puede venir");

        Turno recuperado = turnoService.obtenerPorId(turno.getIdTurno());
        assertEquals(EstadoTurno.CANCELADO, recuperado.getEstadoTurno());
        assertEquals("El cliente avisó que no puede venir", recuperado.getMotivoLog(),
                "El motivo de baja debe quedar conservado en motivo_log");
    }

    @Test
    @DisplayName("HU15 - Positivo: cancelar un turno libera automáticamente el horario para un nuevo turno")
    void cancelarTurnoLiberaElHorario() throws SQLException {
        Cliente c1 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Cliente c2 = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(11, 0);

        Turno original = TestFixtures.crearTurnoConfirmado(c1.getIdCliente(), fecha, hora);
        turnoService.cambiarEstado(original, EstadoTurno.CANCELADO, "Cancelado por el cliente");

        Turno nuevo = new Turno();
        nuevo.setIdCliente(c2.getIdCliente());
        nuevo.setIdEmpleado(TestFixtures.ID_EMPLEADO_ESTILISTA);
        nuevo.setFecha(fecha);
        nuevo.setHoraInicio(hora);
        nuevo.setHoraFin(hora.plusMinutes(30));
        Servicio corte = new Servicio();
        corte.setIdServicio(TestFixtures.ID_SERVICIO_CORTE);
        nuevo.setServicios(List.of(corte));

        assertTrue(turnoService.registrarTurno(nuevo),
                "El mismo horario, ya liberado por la cancelación, debe poder reutilizarse para otro cliente");
    }

    @Test
    @DisplayName("HU15 - Negativo: cancelar un turno ya FACTURADO (estado terminal) es rechazado")
    void cancelarTurnoFacturadoEsRechazado() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(12, 0));
        turnoService.cambiarEstado(turno, EstadoTurno.FINALIZADO, "Finalizado manualmente para el test");

        assertThrows(IllegalStateException.class,
                () -> turnoService.cambiarEstado(turno, EstadoTurno.CANCELADO, "Intento de cancelar tras finalizar"),
                "FINALIZADO solo puede pasar a FACTURADO; cancelar debe rechazarse");
    }

    @Test
    @DisplayName("HU15 - Negativo: intentar cancelar un turno inexistente no debe completarse silenciosamente")
    void cancelarTurnoInexistenteGeneraError() {
        Turno fantasma = new Turno();
        fantasma.setIdTurno(999999);
        fantasma.setEstadoTurno(EstadoTurno.PENDIENTE);

        assertThrows(Exception.class,
                () -> turnoService.cambiarEstado(fantasma, EstadoTurno.CANCELADO, "no existe"),
                "Cancelar un turno sin datos reales cargados debe fallar, no completarse silenciosamente");
    }
}
