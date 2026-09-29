package controllers;

import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;
import claseslogicas.FacturaReporteDetalle;
import dao.ReporteFacturacionDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import utilidades.AlertaUtil;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReporteFacturacionController {

    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

    @FXML private LineChart<String, Number> graficoFacturacion;
    @FXML private StackedBarChart<String, Number> graficoMetodosPago;
    @FXML private PieChart graficoTortaMetodos;
    @FXML private BarChart<String, Number> graficoFacturacionCliente;

    @FXML private Button btnExportar;
    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblTotalFacturas;
    @FXML private Label lblTotalPagadas;

    @FXML private TableView<FacturaReporteDetalle> tablaResumen;
    @FXML private TableColumn<FacturaReporteDetalle, String> colCliente;
    @FXML private TableColumn<FacturaReporteDetalle, String> colFecha;
    @FXML private TableColumn<FacturaReporteDetalle, String> colMonto;
    @FXML private TableColumn<FacturaReporteDetalle, String> colFormaPago;

    private final ReporteFacturacionDAO dao = new ReporteFacturacionDAO();
    private final List<FacturaReporteDetalle> detalleParaExportar = new ArrayList<>();

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        graficoFacturacion.setTitle("Evolución diaria de facturación ($)");
        graficoMetodosPago.setTitle("Facturación por forma de pago");
        graficoTortaMetodos.setTitle("Distribución de formas de pago");
        graficoFacturacionCliente.setTitle("Facturación por cliente");

        graficoFacturacion.setAnimated(false);
        graficoMetodosPago.setAnimated(false);
        graficoFacturacionCliente.setAnimated(false);
        graficoFacturacion.setLegendVisible(false);
        graficoMetodosPago.setLegendVisible(true);
        graficoMetodosPago.setLegendSide(Side.BOTTOM);
        graficoTortaMetodos.setLegendVisible(true);
        graficoTortaMetodos.setLegendSide(Side.RIGHT);
        graficoFacturacionCliente.setLegendVisible(false);

        ocultarResultados();
        configurarTabla();
    }

    private void configurarTabla() {
        colCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCliente()));
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaHora() == null
                        ? "-"
                        : data.getValue().getFechaHora().format(FORMATO_FECHA_HORA)));
        colMonto.setCellValueFactory(data -> new SimpleStringProperty(
                formatearMoneda(data.getValue().getMonto())));
        colFormaPago.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFormaPago()));
    }

    private void ocultarResultados() {
        graficoFacturacion.setVisible(false);
        graficoMetodosPago.setVisible(false);
        graficoTortaMetodos.setVisible(false);
        graficoFacturacionCliente.setVisible(false);
        tablaResumen.setVisible(false);
    }

    private String formatearMoneda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance().format(valor == null ? BigDecimal.ZERO : valor);
    }

    @FXML
    public void generarReporte() {
        LocalDate inicio = fechaInicio.getValue();
        LocalDate fin = fechaFin.getValue();

        if (inicio == null || fin == null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Fechas inválidas", null,
                    "Debés seleccionar ambas fechas.");
            return;
        }
        if (fin.isBefore(inicio)) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Rango inválido", null,
                    "La fecha Hasta no puede ser anterior a la fecha Desde.");
            return;
        }

        List<FacturaReporteDetalle> detalle = dao.obtenerDetalleFacturacion(inicio, fin);
        detalleParaExportar.clear();
        detalleParaExportar.addAll(detalle);
        tablaResumen.setItems(FXCollections.observableArrayList(detalle));

        if (detalle.isEmpty()) {
            ocultarResultados();
            lblTotalPeriodo.setText("Total Facturado Período: $0.00");
            lblTotalFacturas.setText("Total Facturas: 0");
            lblTotalPagadas.setText("Total Pagadas: 0");
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", null,
                    "No se encontraron facturas para el período seleccionado.");
            return;
        }

        Map<LocalDate, BigDecimal> facturacion = dao.obtenerFacturacionPorDia(inicio, fin);
        Map<LocalDate, Map<String, BigDecimal>> importesPorDia =
                dao.obtenerImportesPorMetodoPagoPorDia(inicio, fin);

        cargarGraficoFacturacion(facturacion);
        cargarGraficoMetodosPago(importesPorDia);
        cargarGraficoTorta(importesPorDia);
        cargarGraficoFacturacionCliente(detalle);
        cargarResumen(detalle);

        graficoFacturacion.setVisible(true);
        graficoMetodosPago.setVisible(true);
        graficoTortaMetodos.setVisible(true);
        graficoFacturacionCliente.setVisible(true);
        tablaResumen.setVisible(true);
    }

    private void cargarGraficoFacturacion(Map<LocalDate, BigDecimal> datos) {
        graficoFacturacion.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Total facturado ($)");
        datos.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
                serie.getData().add(new XYChart.Data<>(entry.getKey().toString(),
                        entry.getValue() == null ? 0 : entry.getValue().setScale(2, RoundingMode.HALF_UP).doubleValue())));
        graficoFacturacion.getData().add(serie);
    }

    private void cargarGraficoMetodosPago(Map<LocalDate, Map<String, BigDecimal>> importesPorDia) {
        graficoMetodosPago.getData().clear();
        Map<String, Map<LocalDate, BigDecimal>> series = new LinkedHashMap<>();
        List<LocalDate> fechas = new ArrayList<>(importesPorDia.keySet());
        Collections.sort(fechas);

        for (LocalDate fecha : fechas) {
            for (Map.Entry<String, BigDecimal> item :
                    importesPorDia.getOrDefault(fecha, Collections.emptyMap()).entrySet()) {
                String metodo = item.getKey() == null || item.getKey().isBlank()
                        ? "Sin especificar" : item.getKey();
                series.computeIfAbsent(metodo, k -> new LinkedHashMap<>())
                        .put(fecha, item.getValue() == null ? BigDecimal.ZERO : item.getValue());
            }
        }

        for (Map.Entry<String, Map<LocalDate, BigDecimal>> entry : series.entrySet()) {
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName(entry.getKey());
            for (LocalDate fecha : fechas) {
                BigDecimal importe = entry.getValue().getOrDefault(fecha, BigDecimal.ZERO);
                serie.getData().add(new XYChart.Data<>(fecha.toString(),
                        importe.setScale(2, RoundingMode.HALF_UP).doubleValue()));
            }
            if (!serie.getData().isEmpty()) {
                graficoMetodosPago.getData().add(serie);
            }
        }
    }

    private void cargarGraficoTorta(Map<LocalDate, Map<String, BigDecimal>> importesPorDia) {
        graficoTortaMetodos.getData().clear();
        Map<String, BigDecimal> totales = new LinkedHashMap<>();

        for (Map<String, BigDecimal> porMetodo : importesPorDia.values()) {
            for (Map.Entry<String, BigDecimal> entry : porMetodo.entrySet()) {
                String metodo = entry.getKey() == null || entry.getKey().isBlank()
                        ? "Sin especificar" : entry.getKey();
                totales.merge(metodo, entry.getValue() == null ? BigDecimal.ZERO : entry.getValue(), BigDecimal::add);
            }
        }

        totales.forEach((metodo, importe) -> {
            if (importe != null && importe.compareTo(BigDecimal.ZERO) > 0) {
                graficoTortaMetodos.getData().add(new PieChart.Data(
                        metodo + " (" + formatearMoneda(importe) + ")", importe.doubleValue()));
            }
        });
    }

    private void cargarGraficoFacturacionCliente(List<FacturaReporteDetalle> detalle) {
        graficoFacturacionCliente.getData().clear();
        Map<String, BigDecimal> totales = detalle.stream()
                .collect(Collectors.groupingBy(
                        FacturaReporteDetalle::getCliente,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, FacturaReporteDetalle::getMonto, BigDecimal::add)));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Total facturado ($)");
        totales.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEach(entry -> serie.getData().add(
                        new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue())));
        graficoFacturacionCliente.getData().add(serie);
    }

    private void cargarResumen(List<FacturaReporteDetalle> detalle) {
        BigDecimal total = detalle.stream()
                .map(FacturaReporteDetalle::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pagadas = detalle.stream().filter(FacturaReporteDetalle::esPagada).count();

        lblTotalPeriodo.setText("Total Facturado Período: " + formatearMoneda(total));
        lblTotalFacturas.setText("Total Facturas: " + detalle.size());
        lblTotalPagadas.setText("Total Pagadas: " + pagadas);
    }

    @FXML
    private void exportarReporte(ActionEvent event) {
        if (detalleParaExportar.isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", null,
                    "Generá el reporte primero.");
            return;
        }

        Window ventana = btnExportar.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte como Imagen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG (*.png)", "*.png"));
        fileChooser.setInitialFileName("Reporte_Facturacion_" + LocalDate.now() + ".png");
        File archivoDestino = fileChooser.showSaveDialog(ventana);
        if (archivoDestino == null) return;

        try {
            VBox contenedor = (VBox) graficoFacturacion.getParent();
            WritableImage snapshot = contenedor.snapshot(new SnapshotParameters(), null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(bufferedImage, "png", archivoDestino);
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null,
                    "El reporte gráfico se ha guardado correctamente.");
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al exportar", null,
                    "No se pudo guardar la imagen.");
        }
    }

    @FXML
    private void exportarReportePDF(ActionEvent event) {
        if (detalleParaExportar.isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", null,
                    "Generá el reporte primero.");
            return;
        }

        Window ventana = btnExportar.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte de facturación (PDF)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        chooser.setInitialFileName("Reporte_Facturacion_" + LocalDate.now() + ".pdf");
        File destino = chooser.showSaveDialog(ventana);
        if (destino == null) return;

        try {
            new ExportadorPDF().exportarFacturasDetalleReporte(
                    FXCollections.observableArrayList(detalleParaExportar), destino);
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null,
                    "El reporte de facturación fue exportado correctamente a PDF.");
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de exportación", null,
                    "No se pudo generar el archivo PDF.");
        }
    }

    @FXML
    private void exportarReporteExcel(ActionEvent event) {
        if (detalleParaExportar.isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", null,
                    "Generá el reporte primero.");
            return;
        }

        Window ventana = btnExportar.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte de facturación (Excel)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Excel (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("Reporte_Facturacion_" + LocalDate.now() + ".xlsx");
        File destino = chooser.showSaveDialog(ventana);
        if (destino == null) return;

        try {
            new ExportadorExcel().exportarFacturasDetalleReporte(
                    FXCollections.observableArrayList(detalleParaExportar), destino);
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null,
                    "El reporte de facturación fue exportado correctamente a Excel (.xlsx).");
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de exportación", null,
                    "No se pudo generar el archivo Excel.");
        }
    }
}
