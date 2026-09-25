package hu16;

import claseslogicas.Cliente;
import claseslogicas.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
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
 * HU16 - Listado de turnos.
 *
 * ESTADO: PARCIAL.
 * Lo que SÍ existe: TurnoDAO.obtenerTurnosFiltrados(fecha, idEmpleado) lista
 * los turnos de UN día (con cliente, estilista, servicio, horario) -- eso
 * cubre "incluye cliente/estilista/servicio/horario" para un día puntual.
 * Lo que NO existe: ningún método que reciba un RANGO (desde/hasta) de
 * fechas, y ninguna exportación de turnos (a diferencia de clientes/
 * facturas, que sí tienen su exportador). Se buscó "rango" y "exportar" en
 * TurnoDAO/TurnoService/GestionDiariaController/GestionTurnosController sin
 * resultados relacionados a turnos.
 */
class HU16ListadoTurnosTest {

    private final TurnoService turnoService = new TurnoService();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU16 - Positivo (parcial): el listado de un día incluye cliente, estilista, servicio y horario")
    void listadoDeUnDiaIncluyeCamposRelevantes() throws SQLException {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        LocalDate fecha = LocalDate.now().plusDays(1);
        Turno turno = TestFixtures.crearTurnoPendiente(c.getIdCliente(), fecha, LocalTime.of(10, 0));

        List<Turno> listado = turnoService.obtenerAgenda(fecha, null);
        Turno encontrado = listado.stream().filter(t -> t.getIdTurno() == turno.getIdTurno()).findFirst().orElseThrow();

        // BUG REAL (mismo hallazgo que HU12): obtenerTurnosFiltrados() arma
        // turno.getCliente()/turno.getEmpleado() pero no sincroniza los campos
        // planos idCliente/idEmpleado. Se documenta el FAIL real sin ajustar la
        // expectativa -- el dato "correcto" está en getCliente().getIdCliente().
        assertEquals(c.getIdCliente(), encontrado.getIdCliente(),
                "FALLA HOY: mismo bug de mapeo que HU12 (obtenerPorId) -- ver turno.getCliente().getIdCliente()");
        assertEquals(TestFixtures.ID_EMPLEADO_ESTILISTA, encontrado.getIdEmpleado(),
                "FALLA HOY: idEmpleado tampoco se sincroniza -- ver turno.getEmpleado().getIdEmpleado()");
        assertNotNull(encontrado.getHoraInicio());
        assertNotNull(encontrado.getServicios());
        assertFalse(encontrado.getServicios().isEmpty(), "Debe incluir el/los servicios del turno");
    }

    @Test
    @DisplayName("HU16 - Límite: fecha sin ningún turno agendado informa lista vacía (no rompe)")
    void fechaSinResultadosInformaListaVacia() throws SQLException {
        List<Turno> listado = turnoService.obtenerAgenda(LocalDate.now().plusYears(2), null);
        assertNotNull(listado);
        assertTrue(listado.isEmpty());
    }

    @Test
    @Disabled("NOT_IMPLEMENTED: no existe ningún método de listado de turnos por RANGO de fechas " +
            "(desde/hasta) -- solo por una fecha puntual. Ver TESTING-REPORT.md.")
    @DisplayName("HU16 - NOT_IMPLEMENTED: generar listado de turnos por un rango de fechas")
    void listadoPorRangoDeFechas() {
        fail("HU16 (rango) no está implementada: TurnoDAO/TurnoService no tienen ningún método con parámetros desde/hasta.");
    }

    @Test
    @Disabled("NOT_IMPLEMENTED: ningún controller de turnos tiene acción de exportar (a diferencia de " +
            "clientes/facturas/reportes, que sí la tienen). Ver TESTING-REPORT.md.")
    @DisplayName("HU16 - NOT_IMPLEMENTED: exportar el listado de turnos")
    void listadoDeTurnosSePuedeExportar() {
        fail("HU16 (exportar) no está implementada: no hay ninguna acción de exportación para turnos.");
    }

    @Test
    @Disabled("NOT_IMPLEMENTED: depende del listado por rango (no existe), por lo tanto la validación " +
            "'Hasta < Desde' tampoco existe para turnos. Ver TESTING-REPORT.md.")
    @DisplayName("HU16 - NOT_IMPLEMENTED: rango inválido (Hasta < Desde) genera error")
    void rangoInvalidoGeneraError() {
        fail("HU16 (validación de rango) no está implementada: no existe el concepto de rango de fechas para turnos.");
    }
}
