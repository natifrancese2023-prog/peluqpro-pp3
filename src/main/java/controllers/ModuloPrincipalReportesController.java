package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import utilidades.AlertaUtil;

import java.io.IOException;
import java.util.Objects;

/**
 * Navegación visual del módulo de reportes.
 * No contiene lógica de negocio: cada reporte conserva su FXML y controller.
 */
public class ModuloPrincipalReportesController {

    @FXML private AnchorPane panelContenidoReportes;
    @FXML private Button btnReporteCliente;
    @FXML private Button btnListadoFacturas;
    @FXML private Button btnReporteFacturacion;
    @FXML private Button btnProductividad;
    @FXML private Button btnRankingServicios;
    @FXML private Button btnExportarTodoPower;

    @FXML
    private void initialize() {
        aplicarPermisos();
    }

    private void aplicarPermisos() {
        boolean esGerente = utilidades.PermisosUtil.esGerente();
        boolean esEstilista = utilidades.PermisosUtil.esEstilista();

        btnReporteCliente.setDisable(!esGerente);
        btnReporteFacturacion.setDisable(!esGerente);
        btnExportarTodoPower.setDisable(!esGerente);
        btnListadoFacturas.setDisable(esEstilista);
    }

    @FXML
    private void cargarSubModulo(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String ruta = (String) sourceButton.getUserData();

        if (ruta == null || ruta.isBlank()) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error", null,
                    "El botón no tiene una ruta FXML configurada.");
            return;
        }

        cargarVista(ruta);
    }

    @FXML
    private void abrirReporteClientes(ActionEvent event) {
        cargarVista("/interface/Reporte_Cliente.fxml");
    }

    @FXML
    private void abrirReporteFacturacion() {
        cargarVista("/interface/ReporteFacturacion.fxml");
    }

    @FXML
    private void abrirListadoFacturas() {
        cargarVista("/interface/ListadoFacturas.fxml");
    }

    @FXML
    private void exportarReporteGeneral(ActionEvent event) {
        new ReporteGeneralController().exportarTodoEnExcel();
    }

    private void cargarVista(String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(rutaFXML))
            );
            Node vista = loader.load();

            panelContenidoReportes.getChildren().setAll(vista);
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error", null,
                    "No se pudo cargar la vista: " + rutaFXML);
        }
    }
}
