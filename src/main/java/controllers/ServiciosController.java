package controllers;

import claseslogicas.Especialidad;
import claseslogicas.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.EspecialidadService;
import service.EspecialidadServicioService;
import service.ServicioService;

import java.sql.SQLException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ServiciosController {

    // ==========================================================
    // BOTONES PRINCIPALES
    // ==========================================================

    @FXML
    private Button btnGestionarServicios;

    @FXML
    private Button btnGestionarEspecialidades;


    // ==========================================================
    // CONTENEDOR PRINCIPAL
    // ==========================================================

    @FXML
    private StackPane contenedorVistas;

    @FXML
    private VBox vistaInicio;

    @FXML
    private VBox panelServicios;

    @FXML
    private VBox panelEspecialidades;


    // ==========================================================
    // SERVICIOS
    // ==========================================================

    @FXML
    private TextField txtBuscarServicio;

    @FXML
    private TableView<Servicio> tablaServicios;

    @FXML
    private TableColumn<Servicio, String> colNombreServicio;

    @FXML
    private TableColumn<Servicio, String> colDescripcionServicio;

    @FXML
    private TableColumn<Servicio, String> colDuracionServicio;

    @FXML
    private TableColumn<Servicio, String> colPrecioServicio;

    @FXML
    private TableColumn<Servicio, String> colCostoServicio;

    @FXML
    private TableColumn<Servicio, String> colEstadoServicio;


    // ==========================================================
    // ESPECIALIDADES
    // ==========================================================

    @FXML
    private TextField txtBuscarEspecialidad;

    @FXML
    private ListView<Especialidad> listaEspecialidades;

    @FXML
    private Label lblEspecialidadSeleccionada;

    @FXML
    private TableView<Servicio> tablaServiciosEspecialidad;

    @FXML
    private TableColumn<Servicio, String> colServicioEspecialidad;

    @FXML
    private TableColumn<Servicio, String> colDuracionEspecialidad;

    @FXML
    private TableColumn<Servicio, String> colPrecioEspecialidad;

    @FXML
    private TableColumn<Servicio, String> colEstadoEspecialidadServicio;


    // ==========================================================
    // SERVICES
    // ==========================================================

    private final ServicioController servicioController =
            new ServicioController();

    private final EspecialidadController especialidadController =
            new EspecialidadController();

    private final EspecialidadServicioService
            especialidadServicioService =
            new EspecialidadServicioService();


    // ==========================================================
    // DATOS
    // ==========================================================

    private ObservableList<Servicio> servicios;

    private FilteredList<Servicio> serviciosFiltrados;

    private ObservableList<Especialidad> especialidades;

    private FilteredList<Especialidad> especialidadesFiltradas;

    private Especialidad especialidadSeleccionada;


    // ==========================================================
    // INITIALIZE
    // ==========================================================

    @FXML
    private void initialize() {

        configurarTablaServicios();
        configurarTablaServiciosEspecialidad();

        configurarBusquedaServicios();
        configurarBusquedaEspecialidades();

        configurarSeleccionEspecialidad();

        cargarServicios();
        cargarEspecialidades();

        mostrarInicio();
    }


    // ==========================================================
    // CAMBIO DE PANEL
    // ==========================================================

    @FXML
    private void mostrarGestionServicios() {

        vistaInicio.setVisible(false);
        vistaInicio.setManaged(false);

        panelEspecialidades.setVisible(false);
        panelEspecialidades.setManaged(false);

        panelServicios.setVisible(true);
        panelServicios.setManaged(true);

        cargarServicios();
    }


    @FXML
    private void verRankingMargen() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/interface/RankingServicios.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ranking de servicios por margen estimado");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            mostrarError(
                    "Error",
                    "No se pudo abrir el ranking de servicios.",
                    e.getMessage()
            );
        }
    }


    @FXML
    private void mostrarGestionEspecialidades() {

        vistaInicio.setVisible(false);
        vistaInicio.setManaged(false);

        panelServicios.setVisible(false);
        panelServicios.setManaged(false);

        panelEspecialidades.setVisible(true);
        panelEspecialidades.setManaged(true);

        cargarEspecialidades();
    }


    private void mostrarInicio() {

        vistaInicio.setVisible(true);
        vistaInicio.setManaged(true);

        panelServicios.setVisible(false);
        panelServicios.setManaged(false);

        panelEspecialidades.setVisible(false);
        panelEspecialidades.setManaged(false);
    }


    // ==========================================================
    // TABLA DE SERVICIOS
    // ==========================================================

    private void configurarTablaServicios() {

        colNombreServicio.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue().getNombreServicio()
                )
        );

        colDescripcionServicio.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue().getDescripcion()
                )
        );

        colDuracionServicio.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue().getDuracionMinutos()
                                + " min"
                )
        );

        colPrecioServicio.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        String.format(
                                "$ %.2f",
                                celda.getValue().getPrecio()
                        )
                )
        );

        colCostoServicio.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        String.format(
                                "$ %.2f",
                                celda.getValue().getCosto()
                        )
                )
        );

        colEstadoServicio.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue().isActivo()
                                ? "Activo"
                                : "Inactivo"
                )
        );
    }


    // ==========================================================
    // CARGAR SERVICIOS
    // ==========================================================

    private void cargarServicios() {

        try {

            List<Servicio> lista =
                    servicioController.listarServicios();

            servicios =
                    FXCollections.observableArrayList(lista);

            serviciosFiltrados =
                    new FilteredList<>(
                            servicios,
                            servicio -> true
                    );

            tablaServicios.setItems(serviciosFiltrados);

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar los servicios.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // BÚSQUEDA DE SERVICIOS
    // ==========================================================

    private void configurarBusquedaServicios() {

        txtBuscarServicio.textProperty().addListener(
                (observable, anterior, nuevo) -> {

                    String filtro =
                            nuevo == null
                                    ? ""
                                    : nuevo.trim().toLowerCase();

                    if (serviciosFiltrados == null) {
                        return;
                    }

                    serviciosFiltrados.setPredicate(servicio -> {

                        if (filtro.isEmpty()) {
                            return true;
                        }

                        return servicio.getNombreServicio()
                                .toLowerCase()
                                .contains(filtro);
                    });
                }
        );
    }


    // ==========================================================
    // NUEVO SERVICIO
    // ==========================================================

    @FXML
    private void nuevoServicio() {

        Dialog<ButtonType> dialog =
                crearDialogoServicio(
                        "Nuevo servicio",
                        null
                );

        Optional<ButtonType> resultado =
                dialog.showAndWait();

        if (resultado.isPresent()
                && resultado.get().getButtonData()
                == ButtonBar.ButtonData.OK_DONE) {

            cargarServicios();
        }
    }


    // ==========================================================
    // MODIFICAR SERVICIO
    // ==========================================================

    @FXML
    private void modificarServicio() {

        Servicio seleccionado =
                tablaServicios.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarAdvertencia(
                    "Seleccione un servicio",
                    "Debe seleccionar un servicio para modificar."
            );

            return;
        }

        Dialog<ButtonType> dialog =
                crearDialogoServicio(
                        "Modificar servicio",
                        seleccionado
                );

        Optional<ButtonType> resultado =
                dialog.showAndWait();

        if (resultado.isPresent()
                && resultado.get().getButtonData()
                == ButtonBar.ButtonData.OK_DONE) {

            cargarServicios();
        }
    }


    // ==========================================================
    // ACTIVAR / INACTIVAR SERVICIO
    // ==========================================================

    @FXML
    private void cambiarEstadoServicio() {

        Servicio seleccionado =
                tablaServicios.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarAdvertencia(
                    "Seleccione un servicio",
                    "Debe seleccionar un servicio."
            );

            return;
        }

        boolean nuevoEstado =
                !seleccionado.isActivo();

        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Cambiar estado");
        confirmacion.setHeaderText(
                nuevoEstado
                        ? "Activar servicio"
                        : "Inactivar servicio"
        );

        confirmacion.setContentText(
                "¿Desea "
                        + (nuevoEstado
                        ? "activar"
                        : "inactivar")
                        + " el servicio \""
                        + seleccionado.getNombreServicio()
                        + "\"?"
        );

        Optional<ButtonType> resultado =
                confirmacion.showAndWait();

        if (resultado.isEmpty()
                || resultado.get() != ButtonType.OK) {

            return;
        }

        try {

            servicioController.cambiarEstado(
                    seleccionado.getIdServicio(),
                    nuevoEstado
            );

            cargarServicios();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo cambiar el estado.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // DIÁLOGO SERVICIO
    // ==========================================================

    private Dialog<ButtonType> crearDialogoServicio(
            String titulo,
            Servicio servicio) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(titulo);

        ButtonType btnGuardar =
                new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnGuardar,
                        ButtonType.CANCEL
                );

        TextField txtNombre =
                new TextField();

        TextField txtDescripcion =
                new TextField();

        TextField txtDuracion =
                new TextField();

        TextField txtPrecio =
                new TextField();

        TextField txtCosto =
                new TextField();

        txtNombre.setPromptText("Nombre del servicio");
        txtDescripcion.setPromptText("Descripción");
        txtDuracion.setPromptText("Duración en minutos");
        txtPrecio.setPromptText("Precio");
        txtCosto.setPromptText("Costo");

        if (servicio != null) {

            txtNombre.setText(
                    servicio.getNombreServicio()
            );

            txtDescripcion.setText(
                    servicio.getDescripcion()
            );

            txtDuracion.setText(
                    String.valueOf(
                            servicio.getDuracionMinutos()
                    )
            );

            txtPrecio.setText(
                    String.valueOf(
                            servicio.getPrecio()
                    )
            );

            txtCosto.setText(
                    String.valueOf(
                            servicio.getCosto()
                    )
            );
        }

        VBox contenido =
                new VBox(8);

        contenido.getChildren().addAll(
                new Label("Nombre"),
                txtNombre,

                new Label("Descripción"),
                txtDescripcion,

                new Label("Duración (minutos)"),
                txtDuracion,

                new Label("Precio"),
                txtPrecio,

                new Label("Costo"),
                txtCosto
        );

        contenido.setPrefWidth(400);

        dialog.getDialogPane()
                .setContent(contenido);

        Button botonGuardar =
                (Button) dialog.getDialogPane()
                        .lookupButton(btnGuardar);

        botonGuardar.setDisable(true);

        Runnable validar = () -> {

            boolean valido = true;

            if (txtNombre.getText()
                    .trim()
                    .isEmpty()) {

                valido = false;
            }

            try {

                int duracion =
                        Integer.parseInt(
                                txtDuracion.getText().trim()
                        );

                double precio =
                        Double.parseDouble(
                                txtPrecio.getText().trim()
                        );

                double costo =
                        Double.parseDouble(
                                txtCosto.getText().trim()
                        );

                if (duracion <= 0
                        || precio < 0
                        || costo < 0) {

                    valido = false;
                }

            } catch (NumberFormatException e) {

                valido = false;
            }

            botonGuardar.setDisable(!valido);
        };

        txtNombre.textProperty()
                .addListener(
                        (obs, old, nuevo) -> validar.run()
                );

        txtDuracion.textProperty()
                .addListener(
                        (obs, old, nuevo) -> validar.run()
                );

        txtPrecio.textProperty()
                .addListener(
                        (obs, old, nuevo) -> validar.run()
                );

        txtCosto.textProperty()
                .addListener(
                        (obs, old, nuevo) -> validar.run()
                );

        botonGuardar.setOnAction(event -> {

            try {

                String nombre =
                        txtNombre.getText().trim();

                String descripcion =
                        txtDescripcion.getText().trim();

                int duracion =
                        Integer.parseInt(
                                txtDuracion.getText().trim()
                        );

                double precio =
                        Double.parseDouble(
                                txtPrecio.getText().trim()
                        );

                double costo =
                        Double.parseDouble(
                                txtCosto.getText().trim()
                        );

                if (servicio == null) {

                    servicioController.registrarServicio(
                            nombre,
                            descripcion,
                            duracion,
                            precio,
                            costo
                    );

                } else {

                    servicioController.modificarServicio(
                            servicio.getIdServicio(),
                            nombre,
                            descripcion,
                            duracion,
                            precio,
                            costo
                    );
                }

                dialog.setResult(btnGuardar);
                dialog.close();

            } catch (Exception e) {

                mostrarError(
                        "Error",
                        "No se pudo guardar el servicio.",
                        e.getMessage()
                );
            }
        });

        validar.run();

        return dialog;
    }


    // ==========================================================
    // ESPECIALIDADES
    // ==========================================================

    private void configurarBusquedaEspecialidades() {

        txtBuscarEspecialidad.textProperty().addListener(
                (observable, anterior, nuevo) -> {

                    String filtro =
                            nuevo == null
                                    ? ""
                                    : nuevo.trim().toLowerCase();

                    if (especialidadesFiltradas == null) {
                        return;
                    }

                    especialidadesFiltradas.setPredicate(
                            especialidad -> {

                                if (filtro.isEmpty()) {
                                    return true;
                                }

                                return especialidad
                                        .getNombre()
                                        .toLowerCase()
                                        .contains(filtro);
                            }
                    );
                }
        );
    }


    private void cargarEspecialidades() {

        try {

            List<Especialidad> lista =
                    especialidadController
                            .listarEspecialidades();

            especialidades =
                    FXCollections.observableArrayList(lista);

            especialidadesFiltradas =
                    new FilteredList<>(
                            especialidades,
                            especialidad -> true
                    );

            listaEspecialidades
                    .setItems(especialidadesFiltradas);

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar las especialidades.",
                    e.getMessage()
            );
        }
    }


    private void configurarSeleccionEspecialidad() {

        listaEspecialidades
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, nueva) -> {

                            especialidadSeleccionada =
                                    nueva;

                            if (nueva == null) {

                                lblEspecialidadSeleccionada
                                        .setText(
                                                "SERVICIOS ASOCIADOS"
                                        );

                                tablaServiciosEspecialidad
                                        .getItems()
                                        .clear();

                                return;
                            }

                            lblEspecialidadSeleccionada
                                    .setText(
                                            "SERVICIOS DE: "
                                                    + nueva.getNombre()
                                    );

                            cargarServiciosEspecialidad();
                        }
                );
    }


    // ==========================================================
    // TABLA SERVICIOS DE ESPECIALIDAD
    // ==========================================================

    private void configurarTablaServiciosEspecialidad() {

        colServicioEspecialidad.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue()
                                .getNombreServicio()
                )
        );

        colDuracionEspecialidad.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue()
                                .getDuracionMinutos()
                                + " min"
                )
        );

        colPrecioEspecialidad.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        String.format(
                                "$ %.2f",
                                celda.getValue().getPrecio()
                        )
                )
        );

        colEstadoEspecialidadServicio
                .setCellValueFactory(
                        celda -> new SimpleStringProperty(
                                celda.getValue().isActivo()
                                        ? "Activo"
                                        : "Inactivo"
                        )
                );
    }


    private void cargarServiciosEspecialidad() {

        if (especialidadSeleccionada == null) {
            return;
        }

        try {

            List<Servicio> lista =
                    especialidadServicioService
                            .listarServiciosPorEspecialidad(
                                    especialidadSeleccionada
                                            .getIdEspecialidad()
                            );

            tablaServiciosEspecialidad.setItems(
                    FXCollections.observableArrayList(lista)
            );

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar los servicios de la especialidad.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // NUEVA ESPECIALIDAD
    // ==========================================================

    @FXML
    private void nuevaEspecialidad() {

        TextInputDialog dialog =
                new TextInputDialog();

        dialog.setTitle("Nueva especialidad");
        dialog.setHeaderText(
                "Registrar nueva especialidad"
        );
        dialog.setContentText(
                "Nombre:"
        );

        Optional<String> resultado =
                dialog.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        String nombre =
                resultado.get().trim();

        if (nombre.isEmpty()) {

            mostrarAdvertencia(
                    "Dato obligatorio",
                    "Debe ingresar el nombre de la especialidad."
            );

            return;
        }

        try {

            especialidadController
                    .registrarEspecialidad(nombre);

            cargarEspecialidades();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo registrar la especialidad.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // MODIFICAR ESPECIALIDAD
    // ==========================================================

    @FXML
    private void modificarEspecialidad() {

        if (especialidadSeleccionada == null) {

            mostrarAdvertencia(
                    "Seleccione una especialidad",
                    "Debe seleccionar una especialidad."
            );

            return;
        }

        TextInputDialog dialog =
                new TextInputDialog(
                        especialidadSeleccionada.getNombre()
                );

        dialog.setTitle("Modificar especialidad");
        dialog.setHeaderText(
                "Modificar especialidad"
        );
        dialog.setContentText(
                "Nombre:"
        );

        Optional<String> resultado =
                dialog.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        String nombre =
                resultado.get().trim();

        if (nombre.isEmpty()) {

            mostrarAdvertencia(
                    "Dato obligatorio",
                    "Debe ingresar el nombre de la especialidad."
            );

            return;
        }

        try {

            especialidadController
                    .modificarEspecialidad(
                            especialidadSeleccionada
                                    .getIdEspecialidad(),
                            nombre
                    );

            cargarEspecialidades();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo modificar la especialidad.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // ACTIVAR / INACTIVAR ESPECIALIDAD
    // ==========================================================

    @FXML
    private void cambiarEstadoEspecialidad() {

        if (especialidadSeleccionada == null) {

            mostrarAdvertencia(
                    "Seleccione una especialidad",
                    "Debe seleccionar una especialidad."
            );

            return;
        }

        boolean nuevoEstado =
                !especialidadSeleccionada.isActivo();

        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Cambiar estado");
        confirmacion.setHeaderText(
                nuevoEstado
                        ? "Activar especialidad"
                        : "Inactivar especialidad"
        );

        confirmacion.setContentText(
                "¿Desea "
                        + (nuevoEstado
                        ? "activar"
                        : "inactivar")
                        + " la especialidad \""
                        + especialidadSeleccionada.getNombre()
                        + "\"?"
        );

        Optional<ButtonType> resultado =
                confirmacion.showAndWait();

        if (resultado.isEmpty()
                || resultado.get() != ButtonType.OK) {

            return;
        }

        try {

            especialidadController
                    .cambiarEstado(
                            especialidadSeleccionada
                                    .getIdEspecialidad(),
                            nuevoEstado
                    );

            cargarEspecialidades();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo cambiar el estado de la especialidad.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // ASOCIAR SERVICIO
    // ==========================================================

    @FXML
    private void asociarServicio() {

        if (especialidadSeleccionada == null) {

            mostrarAdvertencia(
                    "Seleccione una especialidad",
                    "Debe seleccionar primero una especialidad."
            );

            return;
        }

        try {

            List<Servicio> disponibles =
                    especialidadServicioService
                            .listarServiciosNoAsociados(
                                    especialidadSeleccionada
                                            .getIdEspecialidad()
                            );

            if (disponibles.isEmpty()) {

                mostrarAdvertencia(
                        "Sin servicios disponibles",
                        "No hay servicios activos disponibles para asociar."
                );

                return;
            }

            ChoiceDialog<Servicio> dialog =
                    new ChoiceDialog<>(
                            disponibles.get(0),
                            disponibles
                    );

            dialog.setTitle("Asociar servicio");
            dialog.setHeaderText(
                    "Asociar servicio a "
                            + especialidadSeleccionada
                            .getNombre()
            );
            dialog.setContentText(
                    "Seleccione un servicio:"
            );

            Optional<Servicio> resultado =
                    dialog.showAndWait();

            if (resultado.isEmpty()) {
                return;
            }

            Servicio servicio =
                    resultado.get();

            especialidadServicioService
                    .asociarServicio(
                            especialidadSeleccionada
                                    .getIdEspecialidad(),
                            servicio.getIdServicio()
                    );

            cargarServiciosEspecialidad();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo asociar el servicio.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // DESASOCIAR SERVICIO
    // ==========================================================

    @FXML
    private void desasociarServicio() {

        if (especialidadSeleccionada == null) {

            mostrarAdvertencia(
                    "Seleccione una especialidad",
                    "Debe seleccionar primero una especialidad."
            );

            return;
        }

        Servicio servicio =
                tablaServiciosEspecialidad
                        .getSelectionModel()
                        .getSelectedItem();

        if (servicio == null) {

            mostrarAdvertencia(
                    "Seleccione un servicio",
                    "Debe seleccionar un servicio para desasociarlo."
            );

            return;
        }

        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle(
                "Desasociar servicio"
        );

        confirmacion.setHeaderText(
                "Desasociar servicio"
        );

        confirmacion.setContentText(
                "¿Desea quitar el servicio \""
                        + servicio.getNombreServicio()
                        + "\" de la especialidad \""
                        + especialidadSeleccionada.getNombre()
                        + "\"?"
        );

        Optional<ButtonType> resultado =
                confirmacion.showAndWait();

        if (resultado.isEmpty()
                || resultado.get() != ButtonType.OK) {

            return;
        }

        try {

            especialidadServicioService
                    .desasociarServicio(
                            especialidadSeleccionada
                                    .getIdEspecialidad(),
                            servicio.getIdServicio()
                    );

            cargarServiciosEspecialidad();

        } catch (Exception e) {

            mostrarError(
                    "Error",
                    "No se pudo desasociar el servicio.",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // MENSAJES
    // ==========================================================

    private void mostrarAdvertencia(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }


    private void mostrarError(
            String titulo,
            String mensaje,
            String detalle) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(titulo);
        alert.setHeaderText(mensaje);
        alert.setContentText(
                detalle == null
                        ? ""
                        : detalle
        );

        alert.showAndWait();
    }
}