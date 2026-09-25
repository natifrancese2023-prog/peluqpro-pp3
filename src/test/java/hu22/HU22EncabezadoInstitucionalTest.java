package hu22;

import claseslogicas.ExportadorPDF;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU22 - Encabezado institucional en PDF.
 * Probado sobre los dos reportes PDF que existen hoy: clientes y facturas
 * (exportarClientesReporte / exportarFacturasReporte), ambos usan el mismo
 * agregarEncabezadoInstitucional() -- formato consistente entre pantallas.
 */
class HU22EncabezadoInstitucionalTest {

    private final ExportadorPDF exportador = new ExportadorPDF();

    @Test
    @DisplayName("HU22 - Positivo: el encabezado incluye el nombre de la institución en el reporte de clientes")
    void encabezadoIncluyeNombreInstitucionEnReporteClientes() throws Exception {
        File destino = File.createTempFile("hu22_clientes_", ".pdf");
        destino.deleteOnExit();
        exportador.exportarClientesReporte(FXCollections.observableArrayList(), destino);

        String texto = extraerTexto(destino);
        assertTrue(texto.contains("PeluqPro"), "Debe incluir el nombre de la institución");
    }

    @Test
    @DisplayName("HU22 - Positivo: el encabezado incluye el nombre de la institución en el reporte de facturas, con el mismo formato")
    void encabezadoIncluyeNombreInstitucionEnReporteFacturas() throws Exception {
        File destino = File.createTempFile("hu22_facturas_", ".pdf");
        destino.deleteOnExit();
        exportador.exportarFacturasReporte(FXCollections.observableArrayList(), destino);

        String texto = extraerTexto(destino);
        assertTrue(texto.contains("PeluqPro"), "El formato del encabezado debe ser consistente entre los distintos reportes PDF");
    }

    @Test
    @DisplayName("HU22 - NEGATIVO/FAIL real: el encabezado NO incluye ningún logotipo")
    void encabezadoNoIncluyeLogotipo() throws Exception {
        File destino = File.createTempFile("hu22_sin_logo_", ".pdf");
        destino.deleteOnExit();
        exportador.exportarClientesReporte(FXCollections.observableArrayList(), destino);

        // BUG REAL: ExportadorPDF.agregarEncabezadoInstitucional() sólo agrega
        // texto (nombre, dirección, teléfono, email, fecha) -- no hay ningún
        // com.itextpdf.text.Image ni referencia a un archivo de logo en todo el
        // método. El criterio de HU22 pide explícitamente "incluye logotipo".
        // Se cuentan las imágenes embebidas en el PDF resultante:
        PdfReader reader = new PdfReader(destino.getAbsolutePath());
        int imagenesEncontradas = 0;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            var recursos = reader.getPageResources(i);
            if (recursos != null && recursos.getAsDict(com.itextpdf.text.pdf.PdfName.XOBJECT) != null) {
                imagenesEncontradas += recursos.getAsDict(com.itextpdf.text.pdf.PdfName.XOBJECT).getKeys().size();
            }
        }
        reader.close();

        assertTrue(imagenesEncontradas > 0,
                "FALLA HOY: no hay ninguna imagen/logo embebido en el PDF -- el encabezado institucional es sólo texto");
    }

    private String extraerTexto(File pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf.getAbsolutePath());
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            sb.append(PdfTextExtractor.getTextFromPage(reader, i));
        }
        reader.close();
        return sb.toString();
    }
}
