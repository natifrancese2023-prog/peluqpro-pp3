package controllers;

import claseslogicas.Cliente;
import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ClienteRiesgo;
import claseslogicas.HistorialView;
import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;
import dao.ReporteDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import service.ClienteService;
import service.ReporteService;
import service.VisitaService;
import utilidades.AlertaUtil;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReporteClienteController {

    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private DatePicker fechaDesdeCliente;
    @FXML private DatePicker fechaHastaCliente;
    @FXML private Button btnConsultarCliente;
    @FXML private Label lblTicketPromedio;

    @FXML private TableView<HistorialView> tablaServiciosCliente;
    @FXML private TableColumn<HistorialView, String> colFechaServicio;
    @FXML private TableColumn<HistorialView, String> colServicioCliente;
    @FXML private TableColumn<HistorialView, String> colEstilistaCliente;
    @FXML private TableColumn<HistorialView, String> colObservacionesCliente;

    @FXML private TableView<ClienteReporteExtendido> tablaClientes;
    @FXML private TableColumn<ClienteReporteExtendido, String> colNombre;
    @FXML private TableColumn<ClienteReporteExtendido, String> colTelefono;
    @FXML private TableColumn<ClienteReporteExtendido, String> colEmail;
    @FXML private TableColumn<ClienteReporteExtendido, Integer> colVisitas;
    @FXML private TableColumn<ClienteReporteExtendido, BigDecimal> colGasto;
    @FXML private TableColumn<ClienteReporteExtendido, String> colEstadoTurno;

    @FXML private ComboBox<String> cbFiltro;
    @FXML private BarChart<String, Number> graficoBarras;
    @FXML private PieChart graficoTorta;
    @FXML private BarChart<String, Number> graficoHistograma;

    @FXML private TableView<ClienteRiesgo> tablaClientesRiesgo;
    @FXML private TableColumn<ClienteRiesgo, String> colRiesgoNombre;
    @FXML private TableColumn<ClienteRiesgo, String> colRiesgoTelefono;
    @FXML private TableColumn<ClienteRiesgo, String> colRiesgoEmail;
    @FXML private TableColumn<ClienteRiesgo, String> colRiesgoUltimaVisita;
    @FXML private TableColumn<ClienteRiesgo, Integer> colRiesgoDias;
    @FXML private DatePicker fechaDesdeTicket;
    @FXML private DatePicker fechaHastaTicket;
    @FXML private Label lblTicketPromedioGeneral;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ReporteService reporteService = new ReporteService();
    private final ClienteService clienteService = new ClienteService();
    private final VisitaService visitaService = new VisitaService();
    private List<ClienteReporteExtendido> datosClientes;

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        configurarTitulosGraficos();
        configurarTabla();
        configurarFiltro();
        configurarConsultaPorCliente();
        configurarTablaRiesgo();
        cargarClientesParaConsulta();
        cargarDatosClientes();
        cargarClientesEnRiesgo();
    }

    private void configurarConsultaPorCliente() {
        cbCliente.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente == null ? "" : (cliente.getPersona() != null ? cliente.getPersona().getNombre() + " " + cliente.getPersona().getApellido() : cliente.getNombreCompleto());
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });

        colFechaServicio.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaHora() == null
                        ? "-"
                        : data.getValue().getFechaHora().format(FORMATO_FECHA_HORA)));
        colServicioCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getNombreServicio() != null ? data.getValue().getNombreServicio() : "-"));
        colEstilistaCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getNombreEstilista() != null ? data.getValue().getNombreEstilista() : "-"));
        colObservacionesCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getObservaciones() != null ? data.getValue().getObservaciones() : "-"));
    }

    private String nombreCliente(Cliente cliente) {
        if (cliente == null) return "";
        if (cliente.getPersona() != null) {
            return ((cliente.getPersona().getNombre() == null ? "" : cliente.getPersona().getNombre()) + " " +
                    (cliente.getPersona().getApellido() == null ? "" : cliente.getPersona().getApellido())).trim();
        }
        return cliente.getNombreCompleto();
    }

    private void cargarClientesParaConsulta() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodos().stream()
                    .filter(Cliente::isActivo)
                    .sorted(Comparator.comparing(this::nombreCliente, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
            cbCliente.setItems(FXCollections.observableArrayList(clientes));
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar clientes", null,
                    "No se pudieron cargar los clientes para la consulta del reporte.");
        }
    }

    @FXML
    private void consultarCliente() {
        Cliente cliente = cbCliente.getValue();
        LocalDate desde = fechaDesdeCliente.getValue();
        LocalDate hasta = fechaHastaCliente.getValue();

        if (cliente == null || desde == null || hasta == null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", null,
                    "Seleccioná un cliente y el período Desde/Hasta.");
            return;
        }
        if (hasta.isBefore(desde)) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Período inválido", null,
                    "La fecha Hasta no puede ser anterior a la fecha Desde.");
            return;
        }

        List<HistorialView> historial = visitaService.obtenerHistorialPorCliente(cliente.getIdCliente()).stream()
                .filter(h -> h.getFechaHora() != null
                        && !h.getFechaHora().toLocalDate().isBefore(desde)
                        && !h.getFechaHora().toLocalDate().isAfter(hasta))
                .collect(Collectors.toList());

        BigDecimal ticketPromedio;
        try {
            ticketPromedio = reporteService.obtenerTicketPromedioCliente(
                    cliente.getIdCliente(), desde, hasta);
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al consultar", null,
                    "No se pudo obtener el ticket promedio del cliente.");
            return;
        }

        if (historial.isEmpty() && ticketPromedio == null) {
            tablaServiciosCliente.setItems(FXCollections.observableArrayList());
            lblTicketPromedio.setText("Ticket promedio: Sin datos");
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", null,
                    "No se encontraron resultados para el cliente y período seleccionados.");
            return;
        }

        tablaServiciosCliente.setItems(FXCollections.observableArrayList(historial));
        lblTicketPromedio.setText(ticketPromedio == null
                ? "Ticket promedio: Sin datos"
                : "Ticket promedio: " + String.format("$%.2f", ticketPromedio));
    }

    private void configurarTitulosGraficos() {
        graficoBarras.setTitle("Gasto Total por Cliente");
        graficoTorta.setTitle("Distribución de Ingresos por Cliente");
        graficoHistograma.setTitle("Frecuencia de Visitas de Clientes");
        graficoBarras.setLegendVisible(false);
        graficoHistograma.setLegendVisible(false);
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
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item.doubleValue()));
            }
        });
    }

    private void configurarFiltro() {
        cbFiltro.setItems(FXCollections.observableArrayList("Todos", "Frecuentes", "Mayor gasto"));
        cbFiltro.getSelectionModel().selectFirst();
        cbFiltro.setOnAction(e -> actualizarVisualizacion());
    }

    private void configurarTablaRiesgo() {
        colRiesgoNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colRiesgoTelefono.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTelefono() != null ? data.getValue().getTelefono() : "-"));
        colRiesgoEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail() != null ? data.getValue().getEmail() : "-"));
        colRiesgoUltimaVisita.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUltimaVisita() == null
                        ? "-"
                        : data.getValue().getUltimaVisita().format(FORMATO_FECHA_HORA)));
        colRiesgoDias.setCellValueFactory(data -> {
            LocalDateTime ultima = data.getValue().getUltimaVisita();

            int dias = ultima == null
                    ? 0
                    : (int) java.time.temporal.ChronoUnit.DAYS.between(
                    ultima.toLocalDate(),
                    LocalDate.now());

            return new javafx.beans.property.SimpleObjectProperty<Integer>(dias);
        });
    }

    private void cargarClientesEnRiesgo() {
        try {
            List<ClienteRiesgo> clientesRiesgo = reporteService.obtenerClientesEnRiesgo();
            tablaClientesRiesgo.setItems(FXCollections.observableArrayList(clientesRiesgo));
        } catch (Exception e) {
            tablaClientesRiesgo.setItems(FXCollections.observableArrayList());
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al consultar", null,
                    "No se pudieron obtener los clientes en riesgo.");
        }
    }

    @FXML
    private void actualizarClientesEnRiesgo() {
        cargarClientesEnRiesgo();
        if (tablaClientesRiesgo.getItems().isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", null,
                    "No se encontraron clientes cuya última visita supere los tres meses.");
        }
    }

    @FXML
    private void consultarTicketPromedio() {
        LocalDate desde = fechaDesdeTicket.getValue();
        LocalDate hasta = fechaHastaTicket.getValue();

        if (desde == null || hasta == null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", null,
                    "Seleccioná las fechas Desde y Hasta.");
            return;
        }
        if (hasta.isBefore(desde)) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Período inválido", null,
                    "La fecha Hasta no puede ser anterior a la fecha Desde.");
            return;
        }

        try {
            BigDecimal ticket = reporteService.obtenerTicketPromedio(desde, hasta);
            if (ticket == null) {
                lblTicketPromedioGeneral.setText("Ticket promedio: Sin datos");
                AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", null,
                        "No se encontraron facturas para el período seleccionado.");
                return;
            }

            lblTicketPromedioGeneral.setText(
                    "Ticket promedio: " + String.format("$%.2f", ticket));
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al consultar", null,
                    "No se pudo calcular el ticket promedio del período seleccionado.");
        }
    }

    private void cargarDatosClientes() {
        try {
            datosClientes = reporteDAO.obtenerDatosClientesExtendido();
            tablaClientes.setItems(FXCollections.observableArrayList(datosClientes));
            actualizarVisualizacion();
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar datos", null,
                    "No se pudieron obtener los datos de clientes.");
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
                    .sorted(Comparator.comparing(ClienteReporteExtendido::getCantidadVisitas).reversed())
                    .limit(5).collect(Collectors.toList());
            case "Mayor gasto" -> filtrados = datosClientes.stream()
                    .sorted(Comparator.comparing(ClienteReporteExtendido::getGastoTotal,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5).collect(Collectors.toList());
            default -> filtrados = datosClientes;
        }

        tablaClientes.setItems(FXCollections.observableArrayList(filtrados));
        if (filtrados.isEmpty() && filtro != null && !"Todos".equals(filtro)) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", null,
                    "No se encontraron clientes que cumplan el criterio: " + filtro + ".");
        }
        actualizarGraficoBarras(filtrados);
        actualizarGraficoTorta(filtrados);
        actualizarGraficoHistograma(filtrados);
    }

    private void actualizarGraficoBarras(List<ClienteReporteExtendido> clientes) {
        graficoBarras.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Gasto total por cliente");
        for (ClienteReporteExtendido c : clientes) {
            serie.getData().add(new XYChart.Data<>(c.getNombreCompleto(),
                    c.getGastoTotal() != null ? c.getGastoTotal().doubleValue() : 0));
        }
        graficoBarras.getData().add(serie);
    }

    private void actualizarGraficoTorta(List<ClienteReporteExtendido> clientes) {
        graficoTorta.getData().clear();
        for (ClienteReporteExtendido c : clientes) {
            graficoTorta.getData().add(new PieChart.Data(c.getNombreCompleto(),
                    c.getGastoTotal() != null ? c.getGastoTotal().doubleValue() : 0));
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
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        File archivo = chooser.showSaveDialog(null);
        if (archivo == null) return;

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
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null,
                    "El reporte fue exportado correctamente a PDF.");
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de exportación", null,
                    "Ocurrió un error al generar el archivo PDF.");
        }
    }

    @FXML
    private void exportarExcel(ActionEvent event) {
        if (tablaClientes.getItems().isEmpty()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", null, "No hay clientes para exportar.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Excel (*.xlsx)", "*.xlsx"));
        File archivo = chooser.showSaveDialog(null);
        if (archivo == null) return;
        try {
            new ExportadorExcel().exportarClientesReporte(
                    FXCollections.observableArrayList(tablaClientes.getItems()), archivo);
            AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación exitosa", null,
                    "El reporte se exportó a Excel (.xlsx) correctamente.");
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de exportación", null,
                    "Ocurrió un error al generar el archivo de Excel.");
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
