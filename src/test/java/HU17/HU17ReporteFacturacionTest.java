package HU17;

import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;
import claseslogicas.FacturaReporteDetalle;
import controllers.ReporteFacturacionController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HU17ReporteFacturacionTest {

    @BeforeAll
    static void iniciarJavaFX() throws Exception {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX ya estaba iniciado.
        }
    }

    private ReporteFacturacionController crearController()
            throws Exception {

        ReporteFacturacionController controller =
                new ReporteFacturacionController();

        asignar(controller, "fechaInicio",
                new DatePicker());

        asignar(controller, "fechaFin",
                new DatePicker());

        asignar(
                controller,
                "graficoFacturacion",
                new LineChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                )
        );

        asignar(
                controller,
                "graficoMetodosPago",
                new StackedBarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                )
        );

        asignar(
                controller,
                "graficoTortaMetodos",
                new PieChart()
        );

        asignar(
                controller,
                "graficoFacturacionCliente",
                new BarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                )
        );

        asignar(controller, "btnExportar",
                new Button());

        asignar(controller, "lblTotalPeriodo",
                new Label());

        asignar(controller, "lblTotalFacturas",
                new Label());

        asignar(controller, "lblTotalPagadas",
                new Label());

        asignar(controller, "tablaResumen",
                new TableView<>());

        asignar(controller, "colCliente",
                new TableColumn<>());

        asignar(controller, "colFecha",
                new TableColumn<>());

        asignar(controller, "colMonto",
                new TableColumn<>());

        asignar(controller, "colFormaPago",
                new TableColumn<>());

        controller.initialize();

        return controller;
    }

    private void asignar(
            Object objeto,
            String nombreCampo,
            Object valor)
            throws Exception {

        Field campo =
                objeto.getClass()
                        .getDeclaredField(nombreCampo);

        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    private Object obtener(
            Object objeto,
            String nombreCampo)
            throws Exception {

        Field campo =
                objeto.getClass()
                        .getDeclaredField(nombreCampo);

        campo.setAccessible(true);

        return campo.get(objeto);
    }

    private List<FacturaReporteDetalle> datosDePrueba() {

        return Arrays.asList(

                new FacturaReporteDetalle(
                        "Juan Perez",
                        LocalDateTime.of(
                                2026, 9, 1,
                                10, 30
                        ),
                        new BigDecimal("8000.00"),
                        "Efectivo",
                        "Pagada"
                ),

                new FacturaReporteDetalle(
                        "Maria Gomez",
                        LocalDateTime.of(
                                2026, 9, 2,
                                11, 00
                        ),
                        new BigDecimal("25000.00"),
                        "Débito",
                        "Pagada"
                ),

                new FacturaReporteDetalle(
                        "Juan Perez",
                        LocalDateTime.of(
                                2026, 9, 3,
                                15, 30
                        ),
                        new BigDecimal("6000.00"),
                        "Transferencia",
                        "Facturada"
                )
        );
    }

    @Test
    void seleccionarRangoDeFechas()
            throws Exception {

        ReporteFacturacionController controller =
                crearController();

        DatePicker fechaInicio =
                (DatePicker) obtener(
                        controller,
                        "fechaInicio"
                );

        DatePicker fechaFin =
                (DatePicker) obtener(
                        controller,
                        "fechaFin"
                );

        LocalDate desde =
                LocalDate.of(2026, 9, 1);

        LocalDate hasta =
                LocalDate.of(2026, 9, 30);

        fechaInicio.setValue(desde);
        fechaFin.setValue(hasta);

        assertEquals(
                desde,
                fechaInicio.getValue()
        );

        assertEquals(
                hasta,
                fechaFin.getValue()
        );
    }

    @Test
    void validarFechaHastaAnteriorAFechaDesde() {

        LocalDate desde =
                LocalDate.of(2026, 9, 30);

        LocalDate hasta =
                LocalDate.of(2026, 9, 1);

        assertTrue(
                hasta.isBefore(desde),
                "La fecha Hasta es anterior a Desde y el rango debe considerarse inválido."
        );
    }

    @Test
    void tablaContieneClienteFechaMontoYFormaDePago()
            throws Exception {

        ReporteFacturacionController controller =
                crearController();

        TableView<FacturaReporteDetalle> tabla =
                (TableView<FacturaReporteDetalle>)
                        obtener(
                                controller,
                                "tablaResumen"
                        );

        List<FacturaReporteDetalle> datos =
                datosDePrueba();

        tabla.setItems(
                FXCollections.observableArrayList(datos)
        );

        assertEquals(
                3,
                tabla.getItems().size()
        );

        FacturaReporteDetalle factura =
                tabla.getItems().get(0);

        assertEquals(
                "Juan Perez",
                factura.getCliente()
        );

        assertEquals(
                LocalDateTime.of(
                        2026, 9, 1,
                        10, 30
                ),
                factura.getFechaHora()
        );

        assertEquals(
                new BigDecimal("8000.00"),
                factura.getMonto()
        );

        assertEquals(
                "Efectivo",
                factura.getFormaPago()
        );
    }

    @Test
    void informacionCorrespondeAlPeriodoSeleccionado()
            throws Exception {

        ReporteFacturacionController controller =
                crearController();

        Method metodo =
                ReporteFacturacionController.class
                        .getDeclaredMethod(
                                "cargarGraficoFacturacionCliente",
                                List.class
                        );

        metodo.setAccessible(true);

        List<FacturaReporteDetalle> datos =
                datosDePrueba();

        metodo.invoke(
                controller,
                datos
        );

        BarChart<String, Number> grafico =
                (BarChart<String, Number>)
                        obtener(
                                controller,
                                "graficoFacturacionCliente"
                        );

        assertEquals(
                1,
                grafico.getData().size()
        );

        assertEquals(
                2,
                grafico.getData()
                        .get(0)
                        .getData()
                        .size()
        );

        /*
         * Juan Perez:
         * 8000 + 6000 = 14000
         */
        assertEquals(
                "Juan Perez",
                grafico.getData()
                        .get(0)
                        .getData()
                        .stream()
                        .filter(
                                dato -> dato.getXValue()
                                        .equals("Juan Perez")
                        )
                        .findFirst()
                        .orElseThrow()
                        .getXValue()
        );
    }

    @Test
    void identificarFacturacionPorFormaDePago()
            throws Exception {

        ReporteFacturacionController controller =
                crearController();

        Method metodo =
                ReporteFacturacionController.class
                        .getDeclaredMethod(
                                "cargarGraficoMetodosPago",
                                Map.class
                        );

        metodo.setAccessible(true);

        Map<LocalDate, Map<String, BigDecimal>>
                importesPorDia =
                Map.of(
                        LocalDate.of(2026, 9, 1),
                        Map.of(
                                "Efectivo",
                                new BigDecimal("8000.00")
                        ),

                        LocalDate.of(2026, 9, 2),
                        Map.of(
                                "Débito",
                                new BigDecimal("25000.00")
                        ),

                        LocalDate.of(2026, 9, 3),
                        Map.of(
                                "Transferencia",
                                new BigDecimal("6000.00")
                        )
                );

        metodo.invoke(
                controller,
                importesPorDia
        );

        StackedBarChart<String, Number> grafico =
                (StackedBarChart<String, Number>)
                        obtener(
                                controller,
                                "graficoMetodosPago"
                        );

        assertEquals(
                3,
                grafico.getData().size()
        );
    }

    @Test
    void graficoRepresentaFacturacionPorCliente()
            throws Exception {

        ReporteFacturacionController controller =
                crearController();

        Method metodo =
                ReporteFacturacionController.class
                        .getDeclaredMethod(
                                "cargarGraficoFacturacionCliente",
                                List.class
                        );

        metodo.setAccessible(true);

        metodo.invoke(
                controller,
                datosDePrueba()
        );

        BarChart<String, Number> grafico =
                (BarChart<String, Number>)
                        obtener(
                                controller,
                                "graficoFacturacionCliente"
                        );

        assertFalse(
                grafico.getData().isEmpty()
        );

        assertFalse(
                grafico.getData()
                        .get(0)
                        .getData()
                        .isEmpty()
        );
    }

    @Test
    void exportarReporteFacturacionPDF()
            throws Exception {

        File archivo =
                Files.createTempFile(
                        "HU17_reporte_facturacion_",
                        ".pdf"
                ).toFile();

        new ExportadorPDF()
                .exportarFacturasDetalleReporte(
                        FXCollections.observableArrayList(
                                datosDePrueba()
                        ),
                        archivo
                );

        assertTrue(
                archivo.exists()
        );

        assertTrue(
                archivo.length() > 0
        );

        assertTrue(
                archivo.delete()
        );
    }

    @Test
    void exportarReporteFacturacionExcel()
            throws Exception {

        File archivo =
                Files.createTempFile(
                        "HU17_reporte_facturacion_",
                        ".xlsx"
                ).toFile();

        new ExportadorExcel()
                .exportarFacturasDetalleReporte(
                        FXCollections.observableArrayList(
                                datosDePrueba()
                        ),
                        archivo
                );

        assertTrue(
                archivo.exists()
        );

        assertTrue(
                archivo.length() > 0
        );

        try (
                FileInputStream input =
                        new FileInputStream(archivo);

                Workbook workbook =
                        WorkbookFactory.create(input)
        ) {

            assertTrue(
                    workbook.getNumberOfSheets() > 0
            );

            Sheet hoja =
                    workbook.getSheetAt(0);

            assertTrue(
                    hoja.getPhysicalNumberOfRows() > 1,
                    "El Excel debe contener encabezados y datos."
            );
        }

        assertTrue(
                archivo.delete()
        );
    }
}