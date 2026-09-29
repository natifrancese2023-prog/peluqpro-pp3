package controllers;

import claseslogicas.ProductividadProfesional;
import dao.ProductividadProfesionalDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import utilidades.AlertaUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProductividadProfesionalController {

    @FXML
    private DatePicker dpDesde;

    @FXML
    private DatePicker dpHasta;

    @FXML
    private Label lblPeriodo;

    @FXML
    private TableView<ProductividadProfesional> tablaProductividad;

    @FXML
    private TableColumn<ProductividadProfesional, String> colProfesional;

    @FXML
    private TableColumn<ProductividadProfesional, Long> colCantidadServicios;

    @FXML
    private TableColumn<ProductividadProfesional, Double> colPromedioDuracion;

    private final ProductividadProfesionalDAO productividadDAO =
            new ProductividadProfesionalDAO();

    @FXML
    public void initialize() {
        colProfesional.setCellValueFactory(
                new PropertyValueFactory<>("nombreProfesional"));
        colCantidadServicios.setCellValueFactory(
                new PropertyValueFactory<>("cantidadServicios"));
        colPromedioDuracion.setCellValueFactory(
                new PropertyValueFactory<>("promedioDuracionMinutos"));

        colPromedioDuracion.setCellFactory(column ->
                new javafx.scene.control.TableCell<>() {
                    @Override
                    protected void updateItem(Double valor, boolean empty) {
                        super.updateItem(valor, empty);

                        if (empty) {
                            setText(null);
                        } else if (valor == null) {
                            setText("Sin servicios");
                        } else {
                            setText(String.format("%.2f min", valor));
                        }
                    }
                });

        LocalDate hoy = LocalDate.now();
        dpHasta.setValue(hoy);
        dpDesde.setValue(hoy.minusMonths(1));
    }

    @FXML
    private void consultarProductividad() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde == null || hasta == null) {
            mostrarAviso("Debe seleccionar las fechas Desde y Hasta.");
            return;
        }

        if (hasta.isBefore(desde)) {
            mostrarAviso("La fecha Hasta no puede ser anterior a Desde.");
            return;
        }

        try {
            List<ProductividadProfesional> resultados =
                    productividadDAO.obtenerPorPeriodo(desde, hasta);

            tablaProductividad.setItems(
                    FXCollections.observableArrayList(resultados));

            lblPeriodo.setText(
                    "Período consultado: " + desde + " al " + hasta
            );

            if (resultados.isEmpty()) {
                mostrarAviso("No hay profesionales para mostrar en el período seleccionado.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Productividad por profesional",
                    "No se pudo consultar la productividad del período seleccionado."
            );
        }
    }

    private void mostrarAviso(String mensaje) {
        AlertaUtil.mostrarAlerta(
                Alert.AlertType.WARNING,
                "Aviso",
                "Productividad por profesional",
                mensaje
        );
    }
}
