package hu20;

import claseslogicas.Cliente;
import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ExportadorPDF;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import dao.ReporteDAO;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestDbSupport;
import support.TestFixtures;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU20 - Exportar reporte por cliente en PDF. */
class HU20ExportarPdfClienteTest {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ExportadorPDF exportador = new ExportadorPDF();

    @BeforeEach
    void limpiar() {
        TestDbSupport.limpiarDatosTransaccionales();
    }

    @Test
    @DisplayName("HU20 - Positivo: generación con datos produce un PDF válido y no vacío con la tabla de clientes")
    void generacionConDatosProduceArchivoValido() throws Exception {
        Cliente c = TestFixtures.crearClienteActivo(TestDbSupport.documentoUnico());
        TestFixtures.crearFacturaPagada(c.getIdCliente(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Corte de pelo", "Efectivo");
        List<ClienteReporteExtendido> datos = reporteDAO.obtenerDatosClientesExtendido();

        File destino = File.createTempFile("hu20_reporte_clientes_", ".pdf");
        destino.deleteOnExit();
        exportador.exportarClientesReporte(FXCollections.observableArrayList(datos), destino);

        assertTrue(destino.exists() && destino.length() > 0, "El archivo PDF debe generarse y no estar vacío");

        String texto = extraerTexto(destino);
        assertTrue(texto.contains("Fixture") || texto.contains(String.valueOf(c.getIdPersona())),
                "El contenido del PDF debe incluir al cliente cargado (trazabilidad básica)");
    }

    @Test
    @DisplayName("HU20 - Límite: generación sin datos (lista vacía) igual produce un archivo válido, no rompe")
    void generacionSinDatosNoRompe() throws Exception {
        File destino = File.createTempFile("hu20_reporte_vacio_", ".pdf");
        destino.deleteOnExit();

        assertDoesNotThrow(() -> exportador.exportarClientesReporte(FXCollections.observableArrayList(), destino));
        assertTrue(destino.exists() && destino.length() > 0, "Debe generarse igual un PDF (con encabezado, aunque sin filas)");
    }

    @Test
    @DisplayName("HU20 - Positivo: el PDF incluye fecha y hora de emisión (encabezado institucional)")
    void pdfIncluyeFechaDeEmision() throws Exception {
        File destino = File.createTempFile("hu20_fecha_emision_", ".pdf");
        destino.deleteOnExit();
        exportador.exportarClientesReporte(FXCollections.observableArrayList(), destino);

        String texto = extraerTexto(destino);
        int anioActual = LocalDate.now().getYear();
        assertTrue(texto.contains(String.valueOf(anioActual)),
                "El encabezado institucional debe incluir la fecha de generación (se busca el año actual en el texto)");
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
