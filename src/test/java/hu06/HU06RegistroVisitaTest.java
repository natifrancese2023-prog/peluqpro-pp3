package hu06;

import claseslogicas.Cliente;
import claseslogicas.ServicioTemp;
import claseslogicas.Turno;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.VisitaService;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** HU6 - Registro de visita: se ingresan datos, se guarda, se actualiza el historial. */
class HU06RegistroVisitaTest {

    private final VisitaService visitaService = new VisitaService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    private ObservableList<ServicioTemp> unServicio() {
        return FXCollections.observableArrayList(
                new ServicioTemp(LocalDate.now(), "Corte de pelo", "Estela Estilista", "Observación de test", "Pendiente")
        );
    }

    @Test
    @DisplayName("HU6 - Positivo: registrar visita para un turno confirmado se guarda y aparece en el historial")
    void registrarVisitaSeGuardaYActualizaHistorial() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(11, 0));

        boolean guardado = visitaService.registrarVisita(c, unServicio(), TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());

        assertTrue(guardado, "El registro de la visita debe confirmar éxito");
        var historial = visitaService.obtenerHistorialPorCliente(c.getIdCliente());
        assertEquals(1, historial.size(), "El historial del cliente debe reflejar la visita recién cargada");
        assertEquals("Corte de pelo", historial.get(0).getNombreServicio());
    }

    @Test
    @DisplayName("HU6 - Negativo: registrar visita sin ningún servicio seleccionado no debe guardar nada")
    void registrarVisitaSinServiciosNoGuardaNada() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        Turno turno = TestFixtures.crearTurnoConfirmado(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(12, 0));

        boolean guardado = visitaService.registrarVisita(c, FXCollections.observableArrayList(),
                TestFixtures.ID_EMPLEADO_ESTILISTA, turno.getIdTurno());

        assertFalse(guardado, "Sin servicios seleccionados, guardarNuevaVisita debe devolver false y no crear nada");
        assertTrue(visitaService.obtenerHistorialPorCliente(c.getIdCliente()).isEmpty());
    }

    @Test
    @DisplayName("HU6 - Negativo: registrar visita sin cliente (null) no debe guardar nada")
    void registrarVisitaSinClienteNoGuardaNada() {
        boolean guardado = visitaService.registrarVisita(null, unServicio(), TestFixtures.ID_EMPLEADO_ESTILISTA, 1);
        assertFalse(guardado, "Sin cliente, guardarNuevaVisita debe devolver false");
    }
}
