package controllers;

import claseslogicas.ClienteReporteExtendido;
import dao.ReporteDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import utilidades.AlertaUtil;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReporteClienteController {

    @FXML private TableView<ClienteReporteExtendido> tablaClientes;
    @FXML private TableColumn<ClienteReporteExtendido, String> colNombre;
    @FXML private TableColumn<ClienteReporteExtendido, String> colTelefono;
    @FXML private TableColumn<ClienteReporteExtendido, String> colEmail;
    @FXML private TableColumn<ClienteReporteExtendido, Integer> colVisitas;
    @FXML private TableColumn<ClienteReporteExtendido, java.math.BigDecimal> colGasto;
    @FXML private TableColumn<ClienteReporteExtendido, String> colEstadoTurno;

    @FXML private ComboBox<String> cbFiltro;


    @FXML private BarChart<String, Number> graficoBarras;
    @FXML private PieChart graficoTorta;
    @FXML private BarChart<String, Number> graficoHistograma;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private List<ClienteReporteExtendido> datosClientes;

    public void initialize() {
        configurarTitulosGraficos();
        configurarTabla();
        configurarFiltro();
        cargarDatosClientes();
    }

    private void configurarTitulosGraficos() {

        graficoBarras.setTitle("Gasto Total por Cliente");
        graficoTorta.setTitle("Distribución de Ingresos por Cliente");
        graficoHistograma.setTitle("Frecuencia de Visitas de Clientes");

        graficoBarras.setLegendVisible(false);
        graficoHistograma.setLegendVisible(false);


        javafx.scene.chart.CategoryAxis xAxisBarras = (javafx.scene.chart.CategoryAxis) graficoBarras.getXAxis();
        xAxisBarras.setTickLabelsVisible(true);
        xAxisBarras.setTickLabelRotation(45);
        xAxisBarras.setTickLabelFill(javafx.scene.paint.Color.BLACK);
        javafx.scene.chart.CategoryAxis xAxisHistograma = (javafx.scene.chart.CategoryAxis) graficoHistograma.getXAxis();
        xAxisHistograma.setTickLabelsVisible(true);
        xAxisHistograma.setTickLabelRotation(45);
        xAxisHistograma.setTickLabelFill(javafx.scene.paint.Color.BLACK);


        String estiloTextoGraficos =
                "-fx-text-background-color: #000000; "
                        + "-fx-mark-highlight-inner: #000000; "
                        + "-fx-legend-text-fill: #000000;";

        graficoBarras.setStyle(estiloTextoGraficos);
        graficoTorta.setStyle(estiloTextoGraficos);
        graficoHistograma.setStyle(estiloTextoGraficos);


        graficoBarras.getXAxis().setStyle("-fx-tick-label-fill: #000000; -fx-label-padding: 5;");
        graficoBarras.getYAxis().setStyle("-fx-tick-label-fill: #000000;");
        graficoHistograma.getXAxis().setStyle("-fx-tick-label-fill: #000000; -fx-label-padding: 5;");
        graficoHistograma.getYAxis().setStyle("-fx-tick-label-fill: #000000;");
    }
    private void configurarTabla() {
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
        colVisitas.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cantidadVisitas"));
        colGasto.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("gastoTotal"));
        colEstadoTurno.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estadoUltimoTurno"));


        colGasto.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(java.math.BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {

                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });
    }

    private void configurarFiltro() {
        cbFiltro.setItems(FXCollections.observableArrayList("Todos", "Frecuentes", "Mayor gasto"));
        cbFiltro.getSelectionModel().selectFirst();
        cbFiltro.setOnAction(e -> actualizarVisualizacion());
    }

    private void cargarDatosClientes() {
        try {
            datosClientes = reporteDAO.obtenerDatosClientesExtendido();
            tablaClientes.setItems(FXCollections.observableArrayList(datosClientes));
            actualizarVisualizacion();
        } catch (SQLException e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar datos", null, "No se pudieron obtener los datos de clientes.");
            System.err.println("🧨 Error SQL: " + e.getMessage());
        }
    }
    private void actualizarVisualizacion() {
        String filtro = cbFiltro.getValue();

        if (datosClientes == null || datosClientes.isEmpty()) {
            tablaClientes.setItems(FXCollections.observableArrayList());
            graficoBarras.getData().clear();
            graficoTorta.getData().clear();
            graficoHistograma.getData().clear();
            return;
        }

        List<ClienteReporteExtendido> filtrados;

        switch (filtro) {

            case "Frecuentes" -> filtrados = datosClientes.stream()
                    .sorted(
                            Comparator.comparing(
                                    ClienteReporteExtendido::getCantidadVisitas,
                                    Comparator.reverseOrder()
                            )
                    )
                    .limit(5)
                    .collect(Collectors.toList());

            case "Mayor gasto" -> filtrados = datosClientes.stream()
                    .sorted(
                            Comparator.comparing(
                                    ClienteReporteExtendido::getGastoTotal,
                                    Comparator.nullsLast(Comparator.reverseOrder())
                            )
                    )
                    .limit(5)
                    .collect(Collectors.toList());

            default -> filtrados = datosClientes;
        }

        tablaClientes.setItems(
                FXCollections.observableArrayList(filtrados)
        );

        actualizarGraficoBarras(filtrados);
        actualizarGraficoTorta(filtrados);
        actualizarGraficoHistograma(filtrados);
    }


    private void actualizarGraficoBarras(List<ClienteReporteExtendido> clientes) {
        graficoBarras.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Gasto total por cliente");
        for (ClienteReporteExtendido c : clientes) {
            double gasto = c.getGastoTotal() != null ? c.getGastoTotal().doubleValue() : 0.0;
            serie.getData().add(new XYChart.Data<>(c.getNombreCompleto(), gasto));
        }
        graficoBarras.getData().add(serie);
    }


    private void actualizarGraficoTorta(List<ClienteReporteExtendido> clientes) {
        graficoTorta.getData().clear();
        for (ClienteReporteExtendido c : clientes) {
            double gasto = c.getGastoTotal() != null ? c.getGastoTotal().doubleValue() : 0.0;
            graficoTorta.getData().add(new PieChart.Data(c.getNombreCompleto(), gasto));
        }
    }


    private void actualizarGraficoHistograma(List<ClienteReporteExtendido> clientes) {
        graficoHistograma.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Distribución de visitas");
        for (ClienteReporteExtendido c : clientes) {
            serie.getData().add(new XYChart.Data<>(c.getNombreCompleto(), c.getCantidadVisitas()));
        }
        graficoHistograma.getData().add(serie);
    }
    @FXML
    private void exportarPDF(ActionEvent event) {
        if (tablaClientes.getItems().isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", null, "No hay clientes para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        File archivo = fileChooser.showSaveDialog(null);

        if (archivo != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(archivo));
                document.open();

                new ExportadorPDF().agregarEncabezadoInstitucional(document);
                document.add(new com.itextpdf.text.Paragraph("Reporte de Clientes",
                        com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14)));
                document.add(new com.itextpdf.text.Paragraph(" "));

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                for (String header : new String[]{"Nombre", "Teléfono", "Email", "Visitas", "Gasto acumulado", "Estado último turno"}) {
                    PdfPCell cell = new PdfPCell(new com.itextpdf.text.Phrase(header,
                            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10)));
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    table.addCell(cell);
                }
                for (ClienteReporteExtendido c : tablaClientes.getItems()) {
                    table.addCell(c.getNombreCompleto());
                    table.addCell(c.getTelefono() != null ? c.getTelefono() : "-");
                    table.addCell(c.getEmail() != null ? c.getEmail() : "-");
                    table.addCell(String.valueOf(c.getCantidadVisitas()));
                    table.addCell(c.getGastoTotal() != null ? c.getGastoTotal().toString() : "0.00");
                    table.addCell(c.getEstadoUltimoTurno() != null ? c.getEstadoUltimoTurno() : "-");
                }
                document.add(table);


                agregarGraficoPDF(document, graficoBarras, "barras.png");
                agregarGraficoPDF(document, graficoTorta, "torta.png");
                agregarGraficoPDF(document, graficoHistograma, "histograma.png");

                document.close();
                AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null, "El reporte fue exportado correctamente a PDF.");
            } catch (Exception e) {
                AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de exportación", null, "Ocurrió un error al generar el archivo PDF.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void exportarExcel(ActionEvent event) {
        if (tablaClientes.getItems().isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", null, "No hay clientes para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Excel (*.xlsx)", "*.xlsx"));
        File archivo = fileChooser.showSaveDialog(null);

        if (archivo != null) {

            try {
                new ExportadorExcel().exportarClientesReporte(
                        FXCollections.observableArrayList(tablaClientes.getItems()), archivo);
                AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null, "El reporte se exportó a Excel (.xlsx) correctamente.");
            } catch (Exception e) {
                AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de exportación", null, "Ocurrió un error al generar el archivo de Excel.");
                e.printStackTrace();
            }
        }
    }

    private void agregarGraficoPDF(Document document, javafx.scene.Node grafico, String nombreArchivo) throws Exception {
        WritableImage snapshot = grafico.snapshot(new SnapshotParameters(), null);
        File file = new File(nombreArchivo);
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        Image img = Image.getInstance(nombreArchivo);

        float anchoDisponible = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        img.scaleToFit(anchoDisponible, 320f);
        document.add(img);
        file.delete();
    }

}
