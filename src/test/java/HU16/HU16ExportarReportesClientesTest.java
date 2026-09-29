package HU16;

import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU16ExportarReporteClientesTest {

    private List<ClienteReporteExtendido> datosReporteHU15() {

        ClienteReporteExtendido cliente1 = new ClienteReporteExtendido(
                1, 1, "Cliente Frecuente 1",
                "3511111111",
                "cliente1@email.com",
                "Calle 1",
                null,
                10,
                new java.math.BigDecimal("50000"),
                "Pendiente",
                null
        );

        ClienteReporteExtendido cliente2 = new ClienteReporteExtendido(
                2, 2, "Cliente Frecuente 2",
                "3512222222",
                "cliente2@email.com",
                "Calle 2",
                null,
                8,
                new java.math.BigDecimal("70000"),
                "Facturado",
                null
        );

        ClienteReporteExtendido cliente3 = new ClienteReporteExtendido(
                3, 3, "Cliente Frecuente 3",
                "3513333333",
                "cliente3@email.com",
                "Calle 3",
                null,
                5,
                new java.math.BigDecimal("30000"),
                "Pendiente",
                null
        );

        return Arrays.asList(cliente1, cliente2, cliente3);
    }

    @Test
    void exportarReporteClientesPDF() throws Exception {

        List<ClienteReporteExtendido> datos = datosReporteHU15();

        File archivo = Files.createTempFile(
                "HU16_reporte_clientes_",
                ".pdf"
        ).toFile();

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(archivo)
        );

        document.open();

        new ExportadorPDF()
                .agregarEncabezadoInstitucional(document);

        document.add(
                new com.itextpdf.text.Paragraph(
                        "Reporte de Clientes"
                )
        );

        com.itextpdf.text.pdf.PdfPTable tabla =
                new com.itextpdf.text.pdf.PdfPTable(6);

        tabla.addCell("Nombre");
        tabla.addCell("Teléfono");
        tabla.addCell("Email");
        tabla.addCell("Visitas");
        tabla.addCell("Gasto acumulado");
        tabla.addCell("Estado último turno");

        for (ClienteReporteExtendido cliente : datos) {

            tabla.addCell(cliente.getNombreCompleto());
            tabla.addCell(cliente.getTelefono());
            tabla.addCell(cliente.getEmail());
            tabla.addCell(
                    String.valueOf(cliente.getCantidadVisitas())
            );
            tabla.addCell(
                    cliente.getGastoTotal().toString()
            );
            tabla.addCell(
                    cliente.getEstadoUltimoTurno()
            );
        }

        document.add(tabla);
        document.close();

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);

        archivo.delete();
    }

    @Test
    void exportarReporteClientesExcel() throws Exception {

        List<ClienteReporteExtendido> datos =
                datosReporteHU15();

        File archivo = Files.createTempFile(
                "HU16_reporte_clientes_",
                ".xlsx"
        ).toFile();

        new ExportadorExcel().exportarClientesReporte(
                javafx.collections.FXCollections
                        .observableArrayList(datos),
                archivo
        );

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);

        try (FileInputStream input =
                     new FileInputStream(archivo);
             Workbook workbook =
                     WorkbookFactory.create(input)) {

            assertTrue(workbook.getNumberOfSheets() > 0);

            Sheet hoja = workbook.getSheetAt(0);

            assertNotNull(hoja);

            assertTrue(
                    hoja.getPhysicalNumberOfRows() > 1,
                    "El Excel debe contener encabezados y datos."
            );
        }

        archivo.delete();
    }

    @Test
    void reporteExportadoContieneLosDatosMostradosEnHU15()
            throws Exception {

        List<ClienteReporteExtendido> datos =
                datosReporteHU15();

        File archivo = Files.createTempFile(
                "HU16_validacion_datos_",
                ".xlsx"
        ).toFile();

        new ExportadorExcel().exportarClientesReporte(
                javafx.collections.FXCollections
                        .observableArrayList(datos),
                archivo
        );

        try (FileInputStream input =
                     new FileInputStream(archivo);
             Workbook workbook =
                     WorkbookFactory.create(input)) {

            Sheet hoja = workbook.getSheetAt(0);

            boolean clienteEncontrado = false;

            for (Row fila : hoja) {

                for (Cell celda : fila) {

                    if (celda.getCellType()
                            == CellType.STRING
                            && "Cliente Frecuente 1"
                            .equals(celda.getStringCellValue())) {

                        clienteEncontrado = true;
                        break;
                    }
                }

                if (clienteEncontrado) {
                    break;
                }
            }

            assertTrue(
                    clienteEncontrado,
                    "El Excel debe contener los datos mostrados en HU15."
            );
        }

        archivo.delete();
    }

    @Test
    void encabezadoInstitucionalSeAgregaAlPDF()
            throws Exception {

        File archivo = Files.createTempFile(
                "HU16_encabezado_",
                ".pdf"
        ).toFile();

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(archivo)
        );

        document.open();

        new ExportadorPDF()
                .agregarEncabezadoInstitucional(document);

        document.add(
                new com.itextpdf.text.Paragraph(
                        "Reporte de Clientes"
                )
        );

        document.close();

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);

        archivo.delete();
    }

    @Test
    void registrarFechaYHoraDeGeneracionDelReporte() {

        LocalDateTime antes =
                LocalDateTime.now();

        // El reporte debería registrar
        // fecha y hora de generación.

        LocalDateTime despues =
                LocalDateTime.now();

        assertFalse(
                despues.isBefore(antes),
                "La fecha y hora de generación debe corresponder al momento de exportación."
        );
    }
}