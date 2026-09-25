package hu19;

import claseslogicas.Cliente;
import claseslogicas.ClienteReporteExtendido;
import dao.ReporteDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestDbSupport;
import support.TestFixtures;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU19 - Filtro dinámico por cliente: Todos / Frecuentes / Mayor gasto.
 *
 * NOTA METODOLÓGICA: el filtro real vive en un método PRIVADO de
 * ReporteClienteController (actualizarVisualizacion(), línea ~138), que no
 * se puede invocar sin cargar el FXML completo (TestFX, BLOCKED en este
 * entorno -- ver TESTING-REPORT.md). Estos tests replican EXACTAMENTE la
 * misma lógica que ese método (mismo predicado, mismo comparator, mismo
 * límite de 10), aplicada sobre datos reales de ReporteDAO, para validar el
 * comportamiento esperado del filtro sin necesitar la UI.
 */
class HU19FiltroDinamicoTest {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    private List<ClienteReporteExtendido> filtroFrecuentes(List<ClienteReporteExtendido> datos) {
        return datos.stream().filter(c -> c.getCantidadVisitas() >= 2).limit(10).collect(Collectors.toList());
    }

    private List<ClienteReporteExtendido> filtroMayorGasto(List<ClienteReporteExtendido> datos) {
        return datos.stream().sorted(Comparator.comparing(ClienteReporteExtendido::getGastoTotal).reversed())
                .limit(10).collect(Collectors.toList());
    }

    @Test
    @DisplayName("HU19 - Positivo: filtro 'Frecuentes' sólo incluye clientes con 2 o más visitas")
    void filtroFrecuentesSoloIncluyeDosOMasVisitas() throws SQLException {
        Cliente frecuente = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(frecuente.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Corte de pelo", "Efectivo");
        TestFixtures.crearFacturaPagada(frecuente.getIdCliente(), LocalDate.now().plusDays(2), LocalTime.of(9, 0), "Peinado", "Efectivo");

        Cliente ocasional = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(ocasional.getIdCliente(), LocalDate.now().plusDays(3), LocalTime.of(9, 0), "Corte de pelo", "Efectivo");

        List<ClienteReporteExtendido> filtrados = filtroFrecuentes(reporteDAO.obtenerDatosClientesExtendido());

        assertTrue(filtrados.stream().anyMatch(c -> c.getIdPersona() == frecuente.getIdPersona()));
        assertFalse(filtrados.stream().anyMatch(c -> c.getIdPersona() == ocasional.getIdPersona()),
                "Con una sola visita, el cliente ocasional no debe pasar el filtro 'Frecuentes' (>= 2)");
    }

    @Test
    @DisplayName("HU19 - Positivo: filtro 'Mayor gasto' ordena de mayor a menor gasto total")
    void filtroMayorGastoOrdenaDescendente() throws SQLException {
        Cliente gastaMucho = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(gastaMucho.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Coloración", "Efectivo"); // $25000

        Cliente gastaPoco = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(gastaPoco.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Corte de pelo", "Efectivo"); // $8000

        List<ClienteReporteExtendido> filtrados = filtroMayorGasto(reporteDAO.obtenerDatosClientesExtendido());

        int posMucho = indexOfPersona(filtrados, gastaMucho.getIdPersona());
        int posPoco = indexOfPersona(filtrados, gastaPoco.getIdPersona());
        assertTrue(posMucho < posPoco, "El cliente que gastó $25000 debe aparecer antes que el que gastó $8000");
    }

    @Test
    @DisplayName("HU19 - Límite: filtro 'Frecuentes' sin ningún cliente que cumpla la condición devuelve lista vacía")
    void filtroFrecuentesSinResultadosDevuelveVacio() throws SQLException {
        TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico()); // 0 visitas
        List<ClienteReporteExtendido> filtrados = filtroFrecuentes(reporteDAO.obtenerDatosClientesExtendido());
        assertTrue(filtrados.isEmpty());
    }

    private int indexOfPersona(List<ClienteReporteExtendido> lista, int idPersona) {
        for (int i = 0; i < lista.size(); i++) if (lista.get(i).getIdPersona() == idPersona) return i;
        throw new AssertionError("idPersona " + idPersona + " no encontrado en la lista filtrada");
    }
}
