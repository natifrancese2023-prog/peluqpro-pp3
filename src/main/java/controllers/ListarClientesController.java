package controllers;

import claseslogicas.Cliente;
import service.ClienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import utilidades.AlertaUtil;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;
import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ExportadorPDF;
import claseslogicas.ExportadorExcel;

import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ListarClientesController implements Initializable {

    private final ClienteService clienteService = new ClienteService();
    private final ExportadorPDF exportadorPDF = new ExportadorPDF();
    private final ExportadorExcel exportadorExcel = new ExportadorExcel();

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellido;
    @FXML private TableColumn<Cliente, String> colDocumento;
    @FXML private TableColumn<Cliente, String> colFechaAlta;
    @FXML private TableColumn<Cliente, Integer> colNumeroVisitas;
    @FXML private TableColumn<Cliente, Void> colAccion;
    @FXML private TableColumn<Cliente, String> colEstado;
    @FXML private ComboBox<String> cmbFiltroEstado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarFiltro();
        cargarDatosClientes();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getPersona() != null ? cellData.getValue().getPersona().getNombre() : "-"
                )
        );

        colApellido.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getPersona() != null ? cellData.getValue().getPersona().getApellido() : "-"
                )
        );

        colDocumento.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getPersona() != null ? cellData.getValue().getPersona().getNumeroDocumento() : "-"
                )
        );

        colFechaAlta.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getFechaAlta() != null
                                ? cellData.getValue().getFechaAlta().toString()
                                : "-"
                )
        );

        colNumeroVisitas.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            int cantidadVisitas = clienteService.contarVisitasPorIdCliente(cliente.getIdCliente());
            return new SimpleIntegerProperty(cantidadVisitas).asObject();
        });
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActivo() ? "Activo" : "Inactivo")
        );

        colAccion.setCellFactory(col -> new TableCell<>() {

            private final Button btnVer = new Button("Ver");
            private final Button btnActivar = new Button("Activar");
            private final HBox contenedor = new HBox(5);

            {
                btnVer.setOnAction(event -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());

                    if (cliente.getPersona() != null) {
                        String documento = cliente.getPersona().getNumeroDocumento();
                        abrirHistorialPorDocumento(cliente, documento);
                    }
                });

                btnActivar.setOnAction(event -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    activarCliente(cliente);
                });

                contenedor.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Cliente cliente = getTableView().getItems().get(getIndex());

                contenedor.getChildren().clear();


                contenedor.getChildren().add(btnVer);


                if (!cliente.isActivo()) {
                    contenedor.getChildren().add(btnActivar);
                }

                setGraphic(contenedor);
            }
        });
    }
    private void activarCliente(Cliente cliente) {

        if (cliente == null) {
            return;
        }

        boolean confirmar = AlertaUtil.mostrarConfirmacion(
                "Reactivar cliente",
                "Cliente inactivo",
                "¿Desea reactivar al cliente "
                        + cliente.getPersona().getNombre()
                        + " "
                        + cliente.getPersona().getApellido()
                        + "?"
        );

        if (!confirmar) {
            return;
        }

        try {

            boolean reactivado = clienteService.reactivarCliente(
                    cliente.getIdCliente()
            );

            if (reactivado) {

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Reactivación exitosa",
                        null,
                        "El cliente fue reactivado correctamente."
                );


                cargarDatosClientes();

            } else {

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "No se pudo reactivar",
                        null,
                        "No se pudo reactivar el cliente seleccionado."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Reactivación del cliente",
                    "Ocurrió un error al intentar reactivar el cliente."
            );
        }
    }
    private void configurarFiltro() {
        cmbFiltroEstado.getItems().addAll("Todos", "Activos", "Inactivos");
        cmbFiltroEstado.getSelectionModel().select("Activos"); // por defecto
        cmbFiltroEstado.setOnAction(e -> cargarDatosClientes());
    }
    public void cargarDatosClientes() {
        try {
            String filtro = cmbFiltroEstado.getValue();
            ObservableList<Cliente> listaClientes;
            if ("Activos".equals(filtro)) {
                listaClientes = FXCollections.observableArrayList(clienteService.obtenerPorEstado(true));
            } else if ("Inactivos".equals(filtro)) {
                listaClientes = FXCollections.observableArrayList(clienteService.obtenerPorEstado(false));
            } else {
                listaClientes = FXCollections.observableArrayList(clienteService.obtenerTodos());
            }
            tblClientes.setItems(listaClientes);
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error", "Carga de clientes", "No se pudieron cargar los clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private ObservableList<ClienteReporteExtendido> filtrarClientesParaExportar(
            List<ClienteReporteExtendido> datos) {

        String filtro = cmbFiltroEstado.getValue();

        if ("Todos".equals(filtro)) {
            return FXCollections.observableArrayList(datos);
        }

        boolean buscarActivos = "Activos".equals(filtro);

        List<Integer> idsPersonas = tblClientes.getItems().stream()
                .filter(cliente -> cliente.isActivo() == buscarActivos)
                .map(Cliente::getIdPersona)
                .collect(Collectors.toList());

        return FXCollections.observableArrayList(
                datos.stream()
                        .filter(cliente -> idsPersonas.contains(cliente.getIdPersona()))
                        .collect(Collectors.toList())
        );
    }


    @FXML
    public void handleRefrescarTabla(ActionEvent event) {
        cargarDatosClientes();
        System.out.println("Tabla de clientes refrescada.");
    }


    @FXML
    private void handleCerrar(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            source.getScene().getWindow().hide();
            System.out.println("✅ Submódulo Listar Clientes cerrado.");
        } catch (Exception e) {
            System.err.println("❌ Error al intentar cerrar el submódulo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleExportarPDF() {
        try {
            List<ClienteReporteExtendido> datos =
                    clienteService.obtenerDatosClientesExtendido();

            ObservableList<ClienteReporteExtendido> datosExportar =
                    filtrarClientesParaExportar(datos);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar listado de clientes en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("Listado_Clientes.pdf");

            File archivo = fileChooser.showSaveDialog(
                    tblClientes.getScene().getWindow()
            );

            if (archivo != null) {
                exportadorPDF.exportarClientesReporte(datosExportar, archivo);

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Exportación exitosa",
                        null,
                        "El listado de clientes fue exportado correctamente a PDF."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Exportación PDF",
                    "No se pudo exportar el listado de clientes: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleExportarExcel() {
        try {
            List<ClienteReporteExtendido> datos =
                    clienteService.obtenerDatosClientesExtendido();

            ObservableList<ClienteReporteExtendido> datosExportar =
                    filtrarClientesParaExportar(datos);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar listado de clientes en Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo Excel", "*.xlsx")
            );
            fileChooser.setInitialFileName("Listado_Clientes.xlsx");

            File archivo = fileChooser.showSaveDialog(
                    tblClientes.getScene().getWindow()
            );

            if (archivo != null) {
                exportadorExcel.exportarClientesReporte(datosExportar, archivo);

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Exportación exitosa",
                        null,
                        "El listado de clientes fue exportado correctamente a Excel."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Exportación Excel",
                    "No se pudo exportar el listado de clientes: " + e.getMessage()
            );
        }
    }


    private void abrirHistorialPorDocumento(Cliente cliente, String documento) {
        try {
            System.out.println("📖 Abriendo historial del cliente con documento: " + documento);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface/HistorialCliente.fxml"));
            Parent root = loader.load();

            HistorialClienteController controller = loader.getController();
            controller.setCliente(cliente);

            Stage stage = new Stage();
            stage.setTitle("Historial del Cliente");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error", "Historial", "No se pudo abrir el historial del cliente con documento " + documento);
        }
    }
}