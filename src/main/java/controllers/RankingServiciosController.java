package controllers;

import claseslogicas.DetalleRankingServicio;
import claseslogicas.RankingServicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import service.RankingServicioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RankingServiciosController {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Button btnConsultar;
    @FXML private TableView<RankingServicio> tablaRanking;
    @FXML private TableColumn<RankingServicio, String> colPosicion;
    @FXML private TableColumn<RankingServicio, String> colServicio;
    @FXML private TableColumn<RankingServicio, String> colRealizados;
    @FXML private TableColumn<RankingServicio, String> colIngresos;
    @FXML private TableColumn<RankingServicio, String> colCosto;
    @FXML private TableColumn<RankingServicio, String> colComision;
    @FXML private TableColumn<RankingServicio, String> colMargen;

    @FXML private Label lblServicioSeleccionado;
    @FXML private Label lblResumen;
    @FXML private TableView<DetalleRankingServicio> tablaDetalle;
    @FXML private TableColumn<DetalleRankingServicio, String> colProfesional;
    @FXML private TableColumn<DetalleRankingServicio, String> colCantidadDetalle;
    @FXML private TableColumn<DetalleRankingServicio, String> colPorcentaje;
    @FXML private TableColumn<DetalleRankingServicio, String> colIngresosDetalle;
    @FXML private TableColumn<DetalleRankingServicio, String> colCostoDetalle;
    @FXML private TableColumn<DetalleRankingServicio, String> colComisionDetalle;
    @FXML private TableColumn<DetalleRankingServicio, String> colMargenDetalle;

    private final RankingServicioService service = new RankingServicioService();

    @FXML
    private void initialize() {
        dpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(LocalDate.now());

        configurarTablaRanking();
        configurarTablaDetalle();

        tablaRanking.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> cargarDetalle(seleccionado)
        );

        consultar();
    }

    private void configurarTablaRanking() {
        colPosicion.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPosicion())));

        colServicio.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getServicio()));

        colRealizados.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCantidadRealizada())));

        colIngresos.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getIngresos())));

        colCosto.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getCostoEstimado())));

        colComision.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getComisiones())));

        colMargen.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getMargenEstimado())));
    }

    private void configurarTablaDetalle() {
        colProfesional.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getProfesional()));

        colCantidadDetalle.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));

        colPorcentaje.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.format("%.2f %%",
                                c.getValue().getPorcentajeComision().doubleValue())));

        colIngresosDetalle.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getIngresos())));

        colCostoDetalle.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getCostoEstimado())));

        colComisionDetalle.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getComision())));

        colMargenDetalle.setCellValueFactory(c ->
                new SimpleStringProperty(formatearMonto(c.getValue().getMargenEstimado())));
    }

    @FXML
    private void consultar() {
        try {
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            List<RankingServicio> datos = service.obtenerRanking(desde, hasta);
            tablaRanking.setItems(FXCollections.observableArrayList(datos));
            tablaDetalle.getItems().clear();
            lblServicioSeleccionado.setText("Seleccione un servicio para ver el detalle por profesional");

            if (datos.isEmpty()) {
                lblResumen.setText("No hay servicios realizados en el período seleccionado.");
            } else {
                BigDecimal margenTotal = datos.stream()
                        .map(RankingServicio::getMargenEstimado)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                lblResumen.setText(
                        "Servicios analizados: " + datos.size()
                                + "   |   Margen estimado total: "
                                + formatearMonto(margenTotal)
                );

                tablaRanking.getSelectionModel().selectFirst();
            }

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
        } catch (Exception e) {
            mostrarError("No se pudo generar el ranking.", e.getMessage());
        }
    }

    @FXML
    private void consultarHoy() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy);
        dpHasta.setValue(hoy);
        consultar();
    }

    @FXML
    private void consultarMes() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);
        consultar();
    }

    private void cargarDetalle(RankingServicio servicioSeleccionado) {
        if (servicioSeleccionado == null) {
            tablaDetalle.getItems().clear();
            return;
        }

        lblServicioSeleccionado.setText(
                "Detalle de: " + servicioSeleccionado.getServicio()
        );

        try {
            List<DetalleRankingServicio> detalle =
                    service.obtenerDetalleServicio(
                            servicioSeleccionado.getIdServicio(),
                            dpDesde.getValue(),
                            dpHasta.getValue()
                    );

            tablaDetalle.setItems(FXCollections.observableArrayList(detalle));

        } catch (Exception e) {
            tablaDetalle.getItems().clear();
            mostrarError("No se pudo cargar el detalle del servicio.", e.getMessage());
        }
    }

    @FXML
    private void cerrar() {
        if (tablaRanking.getScene() != null
                && tablaRanking.getScene().getWindow() != null) {
            tablaRanking.getScene().getWindow().hide();
        }
    }

    private String formatearMonto(BigDecimal monto) {
        if (monto == null) {
            return "$ 0,00";
        }
        return String.format("$ %.2f", monto.doubleValue());
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ranking de servicios");
        alert.setHeaderText("Datos inválidos");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje, String detalle) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ranking de servicios");
        alert.setHeaderText(mensaje);
        alert.setContentText(detalle == null ? mensaje : detalle);
        alert.showAndWait();
    }
}
