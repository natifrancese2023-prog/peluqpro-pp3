package hu05;

import claseslogicas.HistorialView;
import dao.visitaDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU05FiltroHistorialTest {

    @Test
    @DisplayName("HU5 - elegir un servicio y ver el historial filtrado")
    void filtrarHistorialPorServicio() {

        visitaDAO dao = new visitaDAO();

        // Cliente utilizado en los datos de prueba
        int idCliente = 10;

        // Servicio que debe existir en los datos de prueba
        String servicioSeleccionado = "Coloración";

        List<HistorialView> historialFiltrado =
                dao.obtenerHistorialPorClienteYServicio(
                        idCliente,
                        servicioSeleccionado
                );

        assertNotNull(
                historialFiltrado,
                "La consulta debe devolver una lista y no null."
        );

        for (HistorialView visita : historialFiltrado) {

            assertEquals(
                    servicioSeleccionado,
                    visita.getNombreServicio(),
                    "Todas las visitas devueltas deben corresponder al servicio seleccionado."
            );
        }
    }
}