package controllers;

import claseslogicas.Turno;
import claseslogicas.Servicio;
import service.TurnoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utilidades.AlertaUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ListadoTurnosController {
    @FXML private DatePicker fechaDesde;
    @FXML private DatePicker fechaHasta;
    @FXML private TableView<Turno> tablaTurnos;
    @FXML private TableColumn<Turno,String> colCliente;
    @FXML private TableColumn<Turno,String> colProfesional;
    @FXML private TableColumn<Turno,String> colServicio;
    @FXML private TableColumn<Turno,String> colFecha;
    @FXML private TableColumn<Turno,String> colHora;
    @FXML private TableColumn<Turno,String> colEstado;

    private final TurnoService turnoService = new TurnoService();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        colCliente.setCellValueFactory(d -> new SimpleStringProperty(nombreCliente(d.getValue())));
        colProfesional.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEmpleado() == null ? "-" : d.getValue().getEmpleado().getNombreCompleto()));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getServicios() == null || d.getValue().getServicios().isEmpty() ? "-" :
                        d.getValue().getServicios().stream().map(Servicio::getNombreServicio).collect(Collectors.joining(", "))));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFecha() == null ? "-" : d.getValue().getFecha().format(FECHA)));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraInicio() == null ? "-" : d.getValue().getHoraInicio().toString() +
                        (d.getValue().getHoraFin() == null ? "" : " - " + d.getValue().getHoraFin())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEstadoTurno() == null ? "-" : d.getValue().getEstadoTurno().getNombre()));
    }

    @FXML
    private void generarListado() {
        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();
        if (desde == null || hasta == null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Período incompleto", null,
                    "Seleccioná las fechas Desde y Hasta.");
            return;
        }
        if (hasta.isBefore(desde)) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Período inválido", null,
                    "La fecha Hasta no puede ser anterior a la fecha Desde.");
            return;
        }
        try {
            List<Turno> turnos = turnoService.obtenerTurnosPorPeriodo(desde, hasta);
            tablaTurnos.setItems(FXCollections.observableArrayList(turnos));
            if (turnos.isEmpty()) {
                AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", null,
                        "No se encontraron turnos para el período seleccionado.");
            }
        } catch (Exception e) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error", null,
                    "No se pudo generar el listado de turnos: " + e.getMessage());
        }
    }

    private String nombreCliente(Turno turno) {
        if (turno.getCliente() == null) return "-";
        if (turno.getCliente().getPersona() != null) {
            return (turno.getCliente().getPersona().getNombre() + " " +
                    turno.getCliente().getPersona().getApellido()).trim();
        }
        return turno.getCliente().getNombreCompleto();
    }
}
