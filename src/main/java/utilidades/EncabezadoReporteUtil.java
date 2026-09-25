package utilidades;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;

import java.net.URL;
import java.time.LocalDate;

public final class EncabezadoReporteUtil {

    private EncabezadoReporteUtil() {
        // Clase utilitaria
    }

    public static void agregarEncabezadoPDF(Document documento)
            throws DocumentException {

        try {
            URL logoUrl = EncabezadoReporteUtil.class
                    .getResource("/resource/logo.png");

            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                documento.add(logo);
            }

        } catch (Exception e) {
            System.err.println(
                    "No se pudo cargar el logo del reporte: "
                            + e.getMessage()
            );
        }

        Paragraph nombre = new Paragraph(
                "PeluqPro",
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        18
                )
        );
        nombre.setAlignment(Element.ALIGN_CENTER);
        documento.add(nombre);

        documento.add(new Paragraph(
                "Dirección: Av. Central 123, Laguna Larga"
        ));

        documento.add(new Paragraph(
                "Teléfono: 03572-400000"
        ));

        documento.add(new Paragraph(
                "Email: contacto@peluqpro.com"
        ));

        documento.add(new Paragraph(
                "Fecha de generación: " + LocalDate.now()
        ));

        documento.add(new Paragraph(" "));
    }
}