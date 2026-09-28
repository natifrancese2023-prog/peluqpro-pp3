package controllers;

import claseslogicas.Empleado;
import javafx.scene.Scene;
import service.EmpleadoService;
import utilidades.AlertaUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ListarProfesionalesController implements Initializable {

    private final EmpleadoService empleadoService =
            new EmpleadoService();

    @FXML
    private TableView<Empleado> tblProfesionales;

    @FXML
    private TableColumn<Empleado, String> colNombre;

    @FXML
    private TableColumn<Empleado, String> colApellido;

    @FXML
    private TableColumn<Empleado, String> colDocumento;

    @FXML
    private TableColumn<Empleado, String> colFechaIngreso;

    @FXML
    private TableColumn<Empleado, String> colComision;

    @FXML
    private TableColumn<Empleado, String> colEstado;

    @FXML
    private TableColumn<Empleado, Void> colAccion;

    @FXML
    private ComboBox<String> cmbFiltroEstado;


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        configurarColumnas();

        configurarFiltro();

        cargarDatosProfesionales();
    }


    // ==========================================================
    // COLUMNAS
    // ==========================================================

    private void configurarColumnas() {

        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        obtenerNombre(cellData.getValue())
                )
        );


        colApellido.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        obtenerApellido(cellData.getValue())
                )
        );


        colDocumento.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        obtenerDocumento(cellData.getValue())
                )
        );


        colFechaIngreso.setCellValueFactory(cellData -> {

            Empleado empleado = cellData.getValue();

            return new SimpleStringProperty(
                    empleado.getFechaIngreso() != null
                            ? empleado.getFechaIngreso().toString()
                            : "-"
            );
        });


        colComision.setCellValueFactory(cellData -> {

            Empleado empleado = cellData.getValue();

            return new SimpleStringProperty(
                    String.format(
                            "%.2f%%",
                            empleado.getPorcentajeComision()
                    )
            );
        });

        colEstado.setCellValueFactory(cellData -> {

            Empleado empleado = cellData.getValue();

            return new SimpleStringProperty(
                    empleado.isActivo()
                            ? "Activo"
                            : "Inactivo"
            );
        });


        configurarColumnaAcciones();
    }


    // ==========================================================
    // ACCIONES
    // ==========================================================

    private void configurarColumnaAcciones() {

        colAccion.setCellFactory(col -> new TableCell<>() {

            private final Button btnVer =
                    new Button("Ver");

            private final Button btnActivar =
                    new Button("Activar");

            private final Button btnDesactivar =
                    new Button("Desactivar");

            private final HBox contenedor =
                    new HBox(5);


            {
                contenedor.setAlignment(
                        javafx.geometry.Pos.CENTER
                );


                // VER
                btnVer.setOnAction(event -> {

                    Empleado empleado =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    abrirFichaProfesional(empleado);
                });


                // ACTIVAR
                btnActivar.setOnAction(event -> {

                    Empleado empleado =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    cambiarEstado(empleado, true);
                });


                // DESACTIVAR
                btnDesactivar.setOnAction(event -> {

                    Empleado empleado =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    cambiarEstado(empleado, false);
                });
            }


            @Override
            protected void updateItem(
                    Void item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty) {

                    setGraphic(null);

                    return;
                }


                Empleado empleado =
                        getTableView()
                                .getItems()
                                .get(getIndex());


                contenedor.getChildren().clear();

                // Siempre mostrar VER
                contenedor.getChildren().add(btnVer);

                boolean activo = empleado.isActivo();

                if (activo) {

                    contenedor.getChildren()
                            .add(btnDesactivar);

                } else {

                    contenedor.getChildren()
                            .add(btnActivar);
                }


                setGraphic(contenedor);
            }
        });
    }


    // ==========================================================
    // CAMBIAR ESTADO
    // ==========================================================

    private void cambiarEstado(
            Empleado empleado,
            boolean nuevoEstado) {

        if (empleado == null) {
            return;
        }


        String nombre =
                obtenerNombre(empleado)
                        + " "
                        + obtenerApellido(empleado);


        String titulo =
                nuevoEstado
                        ? "Activar profesional"
                        : "Desactivar profesional";


        String mensaje =
                nuevoEstado
                        ? "¿Desea activar al profesional "
                        + nombre
                        + "?"
                        : "¿Desea desactivar al profesional "
                        + nombre
                        + "?";


        boolean confirmar =
                AlertaUtil.mostrarConfirmacion(
                        titulo,
                        "Cambio de estado",
                        mensaje
                );


        if (!confirmar) {
            return;
        }


        try {

            boolean resultado =
                    empleadoService.actualizarEstado(
                            empleado.getIdEmpleado(),
                            nuevoEstado
                    );


            if (resultado) {

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Operación exitosa",
                        null,
                        nuevoEstado
                                ? "El profesional fue activado correctamente."
                                : "El profesional fue desactivado correctamente."
                );


                cargarDatosProfesionales();

            } else {

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "No se pudo actualizar",
                        null,
                        "No se pudo modificar el estado del profesional."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Cambio de estado",
                    "Ocurrió un error al actualizar el estado del profesional."
            );
        }
    }


    // ==========================================================
    // FILTRO
    // ==========================================================

    private void configurarFiltro() {

        cmbFiltroEstado.getItems().addAll(
                "Todos",
                "Activos",
                "Inactivos"
        );

        cmbFiltroEstado
                .getSelectionModel()
                .select("Activos");


        cmbFiltroEstado.setOnAction(
                event -> cargarDatosProfesionales()
        );
    }


    // ==========================================================
    // CARGAR PROFESIONALES
    // ==========================================================

    public void cargarDatosProfesionales() {

        try {

            String filtro =
                    cmbFiltroEstado.getValue();

            ObservableList<Empleado> listaProfesionales;


            if ("Activos".equals(filtro)) {

                listaProfesionales =
                        FXCollections.observableArrayList(
                                empleadoService
                                        .obtenerProfesionales()
                                        .stream()
                                        .filter(this::estaActivo)
                                        .toList()
                        );

            } else if ("Inactivos".equals(filtro)) {

                listaProfesionales =
                        FXCollections.observableArrayList(
                                empleadoService
                                        .obtenerProfesionales()
                                        .stream()
                                        .filter(empleado ->
                                                !estaActivo(empleado))
                                        .toList()
                        );

            } else {

                listaProfesionales =
                        FXCollections.observableArrayList(
                                empleadoService
                                        .obtenerProfesionales()
                        );
            }


            tblProfesionales.setItems(
                    listaProfesionales
            );

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Carga de profesionales",
                    "No se pudieron cargar los profesionales: "
                            + e.getMessage()
            );
        }
    }


    // ==========================================================
    // FICHA
    // ==========================================================

    private void abrirFichaProfesional(Empleado empleado) {

        if (empleado == null) {
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/interface/FichaProfesional.fxml"
                    )
            );

            Parent root = loader.load();

            FichaProfesionalController controller =
                    loader.getController();

            controller.setProfesional(empleado);

            Stage stage = new Stage();

            stage.setTitle(
                    "Ficha Profesional - "
                            + obtenerNombre(empleado)
                            + " "
                            + obtenerApellido(empleado)
            );

            stage.setScene(new Scene(root));

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    null,
                    "No se pudo abrir la ficha del profesional: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================================
    // UTILIDADES
    // ==========================================================
    private boolean estaActivo(Empleado empleado) {

        return empleado != null
                && empleado.isActivo();
    }

    private String obtenerNombre(Empleado empleado) {

        if (empleado == null) {
            return "-";
        }

        if (empleado.getPersona() != null) {
            return empleado.getPersona().getNombre() != null
                    ? empleado.getPersona().getNombre()
                    : "-";
        }

        return empleado.getNombre() != null
                ? empleado.getNombre()
                : "-";
    }


    private String obtenerApellido(Empleado empleado) {

        if (empleado == null) {
            return "-";
        }

        if (empleado.getPersona() != null) {
            return empleado.getPersona().getApellido() != null
                    ? empleado.getPersona().getApellido()
                    : "-";
        }

        return empleado.getApellido() != null
                ? empleado.getApellido()
                : "-";
    }

    private String obtenerDocumento(Empleado empleado) {

        if (empleado == null || empleado.getPersona() == null) {
            return "-";
        }

        String documento =
                empleado.getPersona().getNumeroDocumento();

        return (documento == null || documento.isBlank())
                ? "-"
                : documento;
    }


    // ==========================================================
    // CERRAR
    // ==========================================================

    @FXML
    private void handleCerrar(ActionEvent event) {

        try {

            Node source =
                    (Node) event.getSource();

            source.getScene()
                    .getWindow()
                    .hide();

        } catch (Exception e) {

            e.printStackTrace();

            System.err.println(
                    "Error al cerrar listado de profesionales: "
                            + e.getMessage()
            );
        }
    }
}
