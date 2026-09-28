package controllers;

import claseslogicas.ComisionProfesional;
import claseslogicas.Empleado;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.ComisionService;
import utilidades.AlertaUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ComisionesProfesionalController {

    @FXML
    private Label lblProfesional;

    @FXML
    private Label lblPorcentaje;

    @FXML
    private DatePicker dpDesde;

    @FXML
    private DatePicker dpHasta;

    @FXML
    private TableView<ComisionProfesional> tablaComisiones;

    @FXML
    private TableColumn<ComisionProfesional, LocalDate> colFecha;

    @FXML
    private TableColumn<ComisionProfesional, String> colServicio;

    @FXML
    private TableColumn<ComisionProfesional, BigDecimal> colPrecio;

    @FXML
    private TableColumn<ComisionProfesional, BigDecimal> colComision;

    @FXML
    private Label lblTotal;

    private final ComisionService comisionService =
            new ComisionService();

    private Empleado profesionalActual;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    new Locale("es", "AR")
            );


    @FXML
    public void initialize() {

        configurarTabla();

        dpDesde.setValue(LocalDate.now());
        dpHasta.setValue(LocalDate.now());

        lblTotal.setText(
                formatoMoneda.format(0)
        );
    }
    public void setProfesional(Empleado profesional) {

        this.profesionalActual = profesional;

        if (profesional == null) {
            return;
        }

        String nombre = "-";
        String apellido = "";

        if (profesional.getPersona() != null) {

            if (profesional.getPersona().getNombre() != null) {
                nombre = profesional.getPersona().getNombre();
            }

            if (profesional.getPersona().getApellido() != null) {
                apellido = profesional.getPersona().getApellido();
            }
        }

        lblProfesional.setText(
                nombre + " " + apellido
        );

        lblPorcentaje.setText(
                String.format(
                        "%.2f %%",
                        profesional.getPorcentajeComision()
                )
        );
    }
    private void configurarTabla() {

        colFecha.setCellValueFactory(
                data ->
                        new SimpleObjectProperty<>(
                                data.getValue().getFecha()
                        )
        );

        colFecha.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            LocalDate fecha,
                            boolean empty) {

                        super.updateItem(fecha, empty);

                        if (empty || fecha == null) {
                            setText(null);
                        } else {
                            setText(
                                    fecha.format(formatoFecha)
                            );
                        }
                    }
                }
        );

        colServicio.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue().getServicio()
                        )
        );

        colPrecio.setCellValueFactory(
                data ->
                        new SimpleObjectProperty<>(
                                data.getValue().getPrecio()
                        )
        );

        colPrecio.setCellFactory(column ->
                crearCeldaMoneda()
        );

        colComision.setCellValueFactory(
                data ->
                        new SimpleObjectProperty<>(
                                data.getValue().getComision()
                        )
        );

        colComision.setCellFactory(column ->
                crearCeldaMoneda()
        );
    }

    private TableCell<ComisionProfesional, BigDecimal>
    crearCeldaMoneda() {

        return new TableCell<>() {

            @Override
            protected void updateItem(
                    BigDecimal valor,
                    boolean empty) {

                super.updateItem(valor, empty);

                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(
                            formatoMoneda.format(valor)
                    );
                }
            }
        };
    }

    @FXML
    private void consultarComisiones() {

        if (profesionalActual == null) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Aviso",
                    null,
                    "No hay un profesional seleccionado."
            );

            return;
        }

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde == null || hasta == null) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Período",
                    null,
                    "Debe seleccionar las fechas desde y hasta."
            );

            return;
        }

        if (desde.isAfter(hasta)) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Período inválido",
                    null,
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );

            return;
        }

        try {

            List<ComisionProfesional> resultados =
                    comisionService.obtenerComisiones(
                            profesionalActual.getIdEmpleado(),
                            desde,
                            hasta
                    );

            tablaComisiones.setItems(
                    FXCollections.observableArrayList(
                            resultados
                    )
            );

            calcularTotal(resultados);

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Consulta de comisiones",
                    "No se pudieron obtener las comisiones: "
                            + e.getMessage()
            );
        }
    }

    private void calcularTotal(
            List<ComisionProfesional> comisiones) {

        BigDecimal total =
                comisiones.stream()
                        .map(ComisionProfesional::getComision)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        lblTotal.setText(
                formatoMoneda.format(total)
        );
    }

    @FXML
    private void consultarHoy() {

        LocalDate hoy = LocalDate.now();

        dpDesde.setValue(hoy);
        dpHasta.setValue(hoy);

        consultarComisiones();
    }

    @FXML
    private void consultarMes() {

        LocalDate hoy = LocalDate.now();

        dpDesde.setValue(
                hoy.withDayOfMonth(1)
        );

        dpHasta.setValue(
                hoy.withDayOfMonth(
                        hoy.lengthOfMonth()
                )
        );

        consultarComisiones();
    }

    @FXML
    private void cerrar() {

        Stage stage =
                (Stage) tablaComisiones
                        .getScene()
                        .getWindow();

        stage.close();
    }
}