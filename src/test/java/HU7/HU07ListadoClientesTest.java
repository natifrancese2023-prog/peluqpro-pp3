package HU7;

import claseslogicas.Cliente;
import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import service.ClienteService;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU07ListadoClientesTest {

    private final ClienteService clienteService = new ClienteService();

    /**
     * HU7 - Criterio: Se genera listado completo.
     *
     * Verifica que el sistema obtenga todos los clientes registrados,
     * incluyendo activos e inactivos.
     *
     * Datos de prueba:
     * Cliente 10 - DNI 40000001 - Juan Perez - Activo
     * Cliente 11 - DNI 40000002 - Vieja Inactiva - Inactivo
     */
    @Test
    void listarClientesGeneraListadoCompleto() {

        List<Cliente> clientes = clienteService.obtenerTodos();

        assertNotNull(clientes);
        assertFalse(clientes.isEmpty());

        Cliente clienteActivo = clientes.stream()
                .filter(c -> c.getIdCliente() == 10)
                .findFirst()
                .orElse(null);

        Cliente clienteInactivo = clientes.stream()
                .filter(c -> c.getIdCliente() == 11)
                .findFirst()
                .orElse(null);

        assertNotNull(
                clienteActivo,
                "El listado completo debe incluir al cliente activo ID 10"
        );

        assertNotNull(
                clienteInactivo,
                "El listado completo debe incluir al cliente inactivo ID 11"
        );
    }

    /**
     * HU7 - Criterio: Incluye todos los campos relevantes.
     *
     * Verifica que los clientes obtenidos contengan los datos principales
     * utilizados por el listado: nombre, apellido, documento, fecha de alta
     * y estado.
     *
     * Datos de prueba:
     * Cliente 10 - DNI 40000001 - Juan Perez
     */
    @Test
    void listadoIncluyeCamposRelevantes() {

        List<Cliente> clientes = clienteService.obtenerTodos();

        Cliente cliente = clientes.stream()
                .filter(c -> c.getIdCliente() == 10)
                .findFirst()
                .orElse(null);

        assertNotNull(cliente);

        assertNotNull(cliente.getPersona());

        assertEquals(
                "Juan",
                cliente.getPersona().getNombre()
        );

        assertEquals(
                "Perez",
                cliente.getPersona().getApellido()
        );

        assertEquals(
                "40000001",
                cliente.getPersona().getNumeroDocumento()
        );

        assertNotNull(
                cliente.getFechaAlta(),
                "El cliente debe tener fecha de alta"
        );

        assertTrue(
                cliente.isActivo(),
                "El cliente 10 debe encontrarse activo"
        );
    }

    /**
     * HU7 - Criterio: Se puede exportar.
     *
     * Verifica que el sistema genere correctamente un archivo PDF
     * a partir del reporte de clientes.
     *
     * Datos de prueba:
     * Datos obtenidos mediante ClienteService.obtenerDatosClientesExtendido().
     */
    @Test
    void exportarListadoClientesPDF() throws Exception {

        List<ClienteReporteExtendido> datos =
                clienteService.obtenerDatosClientesExtendido();

        assertNotNull(datos);
        assertFalse(
                datos.isEmpty(),
                "Debe existir información para generar el reporte PDF"
        );

        ObservableList<ClienteReporteExtendido> clientes =
                FXCollections.observableArrayList(datos);

        Path archivoTemporal =
                Files.createTempFile("HU07_reporte_clientes_", ".pdf");

        File archivo = archivoTemporal.toFile();

        try {
            ExportadorPDF exportador = new ExportadorPDF();

            exportador.exportarClientesReporte(
                    clientes,
                    archivo
            );

            assertTrue(
                    archivo.exists(),
                    "El archivo PDF debe ser generado"
            );

            assertTrue(
                    archivo.length() > 0,
                    "El archivo PDF no debe estar vacío"
            );

        } finally {
            Files.deleteIfExists(archivoTemporal);
        }
    }

    /**
     * HU7 - Criterio: Se puede exportar.
     *
     * Verifica que el sistema genere correctamente un archivo Excel
     * a partir del reporte de clientes.
     *
     * Datos de prueba:
     * Datos obtenidos mediante ClienteService.obtenerDatosClientesExtendido().
     */
    @Test
    void exportarListadoClientesExcel() throws Exception {

        List<ClienteReporteExtendido> datos =
                clienteService.obtenerDatosClientesExtendido();

        assertNotNull(datos);
        assertFalse(
                datos.isEmpty(),
                "Debe existir información para generar el reporte Excel"
        );

        ObservableList<ClienteReporteExtendido> clientes =
                FXCollections.observableArrayList(datos);

        Path archivoTemporal =
                Files.createTempFile("HU07_reporte_clientes_", ".xlsx");

        File archivo = archivoTemporal.toFile();

        try {
            ExportadorExcel exportador = new ExportadorExcel();

            exportador.exportarClientesReporte(
                    clientes,
                    archivo
            );

            assertTrue(
                    archivo.exists(),
                    "El archivo Excel debe ser generado"
            );

            assertTrue(
                    archivo.length() > 0,
                    "El archivo Excel no debe estar vacío"
            );

        } finally {
            Files.deleteIfExists(archivoTemporal);
        }
    }

    /**
     * HU7 - Verificación complementaria de la información exportada.
     *
     * Verifica que el Excel generado contenga los encabezados definidos
     * por el exportador y al menos un registro de cliente.
     *
     * Datos de prueba:
     * Reporte generado con los datos existentes en la BD de prueba.
     */
    @Test
    void excelContieneInformacionDelReporte() throws Exception {

        List<ClienteReporteExtendido> datos =
                clienteService.obtenerDatosClientesExtendido();

        assertFalse(datos.isEmpty());

        ObservableList<ClienteReporteExtendido> clientes =
                FXCollections.observableArrayList(datos);

        Path archivoTemporal =
                Files.createTempFile("HU07_validacion_excel_", ".xlsx");

        File archivo = archivoTemporal.toFile();

        try {
            ExportadorExcel exportador = new ExportadorExcel();

            exportador.exportarClientesReporte(
                    clientes,
                    archivo
            );

            try (FileInputStream fis = new FileInputStream(archivo);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet hoja = workbook.getSheet("Reporte de Clientes");

                assertNotNull(
                        hoja,
                        "Debe existir la hoja 'Reporte de Clientes'"
                );

                Row encabezado = hoja.getRow(6);

                assertNotNull(encabezado);

                assertEquals(
                        "Nombre",
                        encabezado.getCell(0).getStringCellValue()
                );

                assertEquals(
                        "Email",
                        encabezado.getCell(1).getStringCellValue()
                );

                assertEquals(
                        "Visitas",
                        encabezado.getCell(2).getStringCellValue()
                );

                assertEquals(
                        "Gasto total",
                        encabezado.getCell(3).getStringCellValue()
                );

                assertEquals(
                        "Estado último turno",
                        encabezado.getCell(4).getStringCellValue()
                );

                Row primeraFilaDatos = hoja.getRow(7);

                assertNotNull(
                        primeraFilaDatos,
                        "El Excel debe contener al menos un cliente"
                );

                assertFalse(
                        primeraFilaDatos.getCell(0)
                                .getStringCellValue()
                                .isBlank(),
                        "El reporte debe contener el nombre del cliente"
                );
            }

        } finally {
            Files.deleteIfExists(archivoTemporal);
        }
    }
}

