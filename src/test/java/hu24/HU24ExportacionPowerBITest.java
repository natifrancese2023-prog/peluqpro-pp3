package hu24;

import claseslogicas.ExportadorExcel;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU24 - Exportación completa de las tablas reales de PeluqPro para Power BI.
 */
class HU24ExportacionPowerBITest {

    private static final Set<String> TABLAS_ESPERADAS = new HashSet<>(Arrays.asList(
            "barrio", "ciudad", "cliente", "detalle_factura", "detalle_servicio",
            "documento", "empleado", "estado", "estado_factura", "factura",
            "horario_atencion", "metodo_pago", "persona", "provincia", "red_social",
            "roles", "servicios", "tipo_documento", "tipos_red_social", "turno",
            "turno_servicios", "usuarios", "visita"
    ));

    @Test
    @DisplayName("HU24 - Exporta una hoja por cada tabla real y conserva sus encabezados")
    void exportacionConsolidadaParaPowerBI() throws Exception {
        File archivo = Files.createTempFile("PeluqPro_Datos_PowerBI_", ".xlsx").toFile();

        try {
            new ExportadorExcel().exportarTodo(archivo);

            assertTrue(archivo.exists(), "El archivo XLSX debe existir");
            assertTrue(archivo.length() > 0, "El archivo XLSX no debe estar vacío");

            try (Workbook workbook = WorkbookFactory.create(archivo)) {
                Set<String> hojas = new HashSet<>();
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    hojas.add(workbook.getSheetName(i));
                }

                assertEquals(TABLAS_ESPERADAS.size(), workbook.getNumberOfSheets(),
                        "Debe existir exactamente una hoja por tabla de PeluqPro");
                assertEquals(TABLAS_ESPERADAS, hojas,
                        "Las hojas deben corresponder exactamente a las tablas reales");

                assertNotNull(workbook.getSheet("cliente").getRow(0).getCell(0));
                assertEquals("id_cliente", workbook.getSheet("cliente").getRow(0).getCell(0).getStringCellValue());

                assertNotNull(workbook.getSheet("turno").getRow(0).getCell(0));
                assertEquals("id_turno", workbook.getSheet("turno").getRow(0).getCell(0).getStringCellValue());

                assertNotNull(workbook.getSheet("factura").getRow(0).getCell(0));
                assertEquals("id_factura", workbook.getSheet("factura").getRow(0).getCell(0).getStringCellValue());
            }
        } finally {
            Files.deleteIfExists(archivo.toPath());
        }
    }
}
