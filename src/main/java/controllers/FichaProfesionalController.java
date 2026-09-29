package controllers;

import claseslogicas.Empleado;
import dao.EmpleadoDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utilidades.AlertaUtil;

import java.io.IOException;
import java.sql.SQLException;
import service.EmpleadoService;
import claseslogicas.Disponibilidad;
import claseslogicas.Especialidad;
import dao.DisponibilidadDAO;
import dao.EmpleadoEspecialidadDAO;

import java.time.LocalTime;
import java.util.List;

public class FichaProfesionalController {

    // ============================================================
    // BÚSQUEDA
    // ============================================================

    @FXML
    private ComboBox<String> cmbTipoDocumento;

    @FXML
    private TextField txtDocumento;


    // ============================================================
    // DATOS PERSONALES
    // ============================================================

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtEmail;


    // ============================================================
    // DOMICILIO
    // ============================================================

    @FXML
    private TextField txtProvincia;

    @FXML
    private TextField txtCiudad;

    @FXML
    private TextField txtBarrio;

    @FXML
    private TextField txtCalle;

    @FXML
    private TextField txtNumero;


    // ============================================================
    // DATOS PROFESIONALES
    // ============================================================

    @FXML
    private TextField txtFechaIngreso;

    @FXML
    private TextField txtComision;

    @FXML
    private Label lblEstado;

    @FXML
    private TextField txtRol;


    // ============================================================
    // LISTAS
    // ============================================================

    @FXML
    private ListView<String> lstEspecialidades;

    @FXML
    private ListView<String> lstDisponibilidades;


    // ============================================================
    // BOTONES
    // ============================================================

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnEstado;

    @FXML
    private Button btnComisiones;

    @FXML
    private Button btnProductividad;




    // ============================================================
    // DAO
    // ============================================================

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final EmpleadoService empleadoService = new EmpleadoService();
    private final EmpleadoEspecialidadDAO empleadoEspecialidadDAO =
            new EmpleadoEspecialidadDAO();

    private final DisponibilidadDAO disponibilidadDAO =
            new DisponibilidadDAO();


    // ============================================================
    // PROFESIONAL ACTUAL
    // ============================================================

    private Empleado profesionalActual;
    public void setProfesional(Empleado profesional) {

        this.profesionalActual = profesional;

        if (profesional != null) {
            mostrarDatos(profesional);
        }
    }

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================

    @FXML
    public void initialize() {

        try {

            cmbTipoDocumento.setItems(
                    FXCollections.observableArrayList(
                            empleadoDAO.obtenerTiposDocumento()
                    )
            );

            cmbTipoDocumento.getSelectionModel().selectFirst();

        } catch (SQLException e) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de Conexión",
                    null,
                    "No se pudieron cargar los tipos de documento."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // BUSCAR PROFESIONAL
    // ============================================================

    @FXML
    private void buscarProfesional() {

        String tipoDoc = cmbTipoDocumento.getValue();
        String nroDoc = txtDocumento.getText().trim();

        if (nroDoc.isEmpty()) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Aviso",
                    null,
                    "Debe ingresar un número de documento."
            );

            return;
        }

        if (tipoDoc == null || tipoDoc.isBlank()) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Aviso",
                    null,
                    "Debe seleccionar un tipo de documento."
            );

            return;
        }

        try {

            profesionalActual =
                    empleadoDAO.consultarPorDocumentoCompleto(
                            tipoDoc,
                            nroDoc
                    );

            if (profesionalActual != null) {

                mostrarDatos(profesionalActual);

            } else {

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Aviso",
                        null,
                        "Profesional no encontrado."
                );

                limpiarCampos();
            }

        } catch (SQLException e) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    null,
                    "Error al consultar el profesional."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // MOSTRAR DATOS
    // ============================================================

    private void mostrarDatos(Empleado profesional) {

        if (profesional == null ||
                profesional.getPersona() == null) {
            return;
        }

        var persona = profesional.getPersona();


        txtNombre.setText(
                obtenerValor(persona.getNombre())
        );

        txtApellido.setText(
                obtenerValor(persona.getApellido())
        );

        txtTelefono.setText(
                obtenerValor(persona.getTelefono())
        );

        txtEmail.setText(
                obtenerValor(persona.getEmail())
        );


        txtDocumento.setText(
                obtenerValor(persona.getNumeroDocumento())
        );

        txtDocumento.setText(
                obtenerValor(persona.getNumeroDocumento())
        );


        txtProvincia.setText(
                obtenerValor(persona.getNombreProvincia())
        );

        txtCiudad.setText(
                obtenerValor(persona.getNombreCiudad())
        );

        txtBarrio.setText(
                obtenerValor(persona.getNombreBarrio())
        );

        txtCalle.setText(
                obtenerValor(persona.getCalle())
        );

        txtNumero.setText(
                obtenerValor(persona.getNumero())
        );


        if (profesional.getFechaIngreso() != null) {

            txtFechaIngreso.setText(
                    profesional.getFechaIngreso().toString()
            );

        } else {

            txtFechaIngreso.setText("-");
        }


        txtComision.setText(
                String.format(
                        "%.2f%%",
                        profesional.getPorcentajeComision()
                )
        );


        if (profesional.isActivo()) {

            lblEstado.setText("Activo");

            if (btnEstado != null) {
                btnEstado.setText("Desactivar");
            }

        } else {

            lblEstado.setText("Inactivo");

            if (btnEstado != null) {
                btnEstado.setText("Activar");
            }
        }


        if (profesional.getRol() != null) {

            txtRol.setText(
                    obtenerValor(
                            profesional.getRol().getNombre()
                    )
            );

        } else {

            txtRol.setText("-");
        }

        cargarEspecialidadesYDisponibilidades(
                profesional.getIdEmpleado()
        );

    }

    private void cargarEspecialidadesYDisponibilidades(int idEmpleado) {

        try {

            // ==============================
            // ESPECIALIDADES
            // ==============================
            lstEspecialidades.getItems().clear();

            List<Especialidad> especialidades =
                    empleadoEspecialidadDAO
                            .obtenerEspecialidadesPorEmpleado(idEmpleado);

            for (Especialidad especialidad : especialidades) {

                if (especialidad != null && especialidad.isActivo()) {
                    lstEspecialidades.getItems().add(
                            especialidad.getNombre()
                    );
                }
            }


            // ==============================
            // DISPONIBILIDADES
            // ==============================
            lstDisponibilidades.getItems().clear();

            List<Disponibilidad> disponibilidades =
                    disponibilidadDAO.obtenerPorEmpleado(idEmpleado);

            for (Disponibilidad disponibilidad : disponibilidades) {

                if (disponibilidad != null && disponibilidad.isActivo()) {

                    lstDisponibilidades.getItems().add(
                            disponibilidad.getDiaSemana()
                                    + "    "
                                    + formatearHora(disponibilidad.getHoraDesde())
                                    + " - "
                                    + formatearHora(disponibilidad.getHoraHasta())
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Datos del profesional",
                    "No se pudieron cargar las especialidades y disponibilidades."
            );
        }
    }

    private String formatearHora(LocalTime hora) {

        if (hora == null) {
            return "-";
        }

        return String.format(
                "%02d:%02d",
                hora.getHour(),
                hora.getMinute()
        );
    }

    // ============================================================
    // MODIFICAR PROFESIONAL
    // ============================================================

    @FXML
    private void modificarProfesional() {

        if (profesionalActual == null) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Aviso",
                    null,
                    "Debe buscar un profesional primero."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/interface/ModificarProfesional.fxml"
                            )
                    );

            Parent root = loader.load();

            ModificarProfesionalController controller =
                    loader.getController();

            controller.setIdEmpleado(
                    profesionalActual.getIdEmpleado()
            );

            Stage stage = new Stage();

            stage.setTitle(
                    "Modificar Profesional"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    null,
                    "No se pudo abrir el formulario de modificación."
            );
        }
    }


    // ============================================================
    // ACTIVAR / DESACTIVAR
    // ============================================================
    @FXML
    private void cambiarEstado() {

        if (profesionalActual == null) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Aviso",
                    null,
                    "Debe buscar un profesional primero."
            );

            return;
        }

        boolean nuevoEstado = !profesionalActual.isActivo();

        String nombre = obtenerValor(
                profesionalActual.getPersona().getNombre()
        ) + " " +
                obtenerValor(
                        profesionalActual.getPersona().getApellido()
                );

        String titulo = nuevoEstado
                ? "Activar profesional"
                : "Desactivar profesional";

        String mensaje = nuevoEstado
                ? "¿Desea activar al profesional " + nombre + "?"
                : "¿Desea desactivar al profesional " + nombre + "?";

        boolean confirmar = AlertaUtil.mostrarConfirmacion(
                titulo,
                "Cambio de estado",
                mensaje
        );

        if (!confirmar) {
            return;
        }

        try {

            boolean resultado = empleadoService.actualizarEstado(
                    profesionalActual.getIdEmpleado(),
                    nuevoEstado
            );

            if (resultado) {

                // Actualizar el objeto actual
                profesionalActual.setActivo(nuevoEstado);

                // Actualizar la información visual
                if (nuevoEstado) {

                    lblEstado.setText("Activo");

                    if (btnEstado != null) {
                        btnEstado.setText("Desactivar");
                    }

                } else {

                    lblEstado.setText("Inactivo");

                    if (btnEstado != null) {
                        btnEstado.setText("Activar");
                    }
                }

                AlertaUtil.mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Operación exitosa",
                        null,
                        nuevoEstado
                                ? "El profesional fue activado correctamente."
                                : "El profesional fue desactivado correctamente."
                );

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


    // ============================================================
    // COMISIONES
    // ============================================================
    @FXML
    private void verComisiones() {

        if (profesionalActual == null) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Aviso",
                    null,
                    "Debe buscar un profesional primero."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/interface/ComisionesProfesional.fxml"
                            )
                    );

            Parent root = loader.load();

            ComisionesProfesionalController controller =
                    loader.getController();

            controller.setProfesional(profesionalActual);

            Stage stage = new Stage();

            stage.setTitle(
                    "Comisiones - "
                            + profesionalActual.getPersona().getNombre()
                            + " "
                            + profesionalActual.getPersona().getApellido()
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    null,
                    "No se pudo abrir la consulta de comisiones."
            );
        }
    }



    // ============================================================
    // CERRAR
    // ============================================================



    // ============================================================
    // LIMPIAR
    // ============================================================

    private void limpiarCampos() {

        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();

        txtProvincia.clear();
        txtCiudad.clear();
        txtBarrio.clear();
        txtCalle.clear();
        txtNumero.clear();

        txtFechaIngreso.clear();
        txtComision.clear();
        txtRol.clear();

        lblEstado.setText("");

        if (lstEspecialidades != null) {
            lstEspecialidades.getItems().clear();
        }

        if (lstDisponibilidades != null) {
            lstDisponibilidades.getItems().clear();
        }

        profesionalActual = null;
    }


    // ============================================================
    // UTILIDAD
    // ============================================================

    private String obtenerValor(String valor) {

        if (valor == null || valor.isBlank()) {
            return "-";
        }

        return valor;
    }
}