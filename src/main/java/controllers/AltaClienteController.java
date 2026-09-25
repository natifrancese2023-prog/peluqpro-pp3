package controllers;

import claseslogicas.Cliente;
import claseslogicas.ClienteRedSocial;

import dao.ClienteDAO;
import service.ClienteService;

import javafx.collections.FXCollections; // Necesario para setAll/getItems
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;

import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import utilidades.AlertaUtil;


public class AltaClienteController implements Initializable {


    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final ClienteService clienteService = new ClienteService();

    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNumeroDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private ComboBox<String> cmbProvincia;
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private ComboBox<String> cmbBarrio;
    @FXML private ComboBox<String> cmbTipoRedSocial;
    @FXML private TextField txtUsuarioRedSocial;



    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cargarComboboxesIniciales();
        configurarListenersComboBox();
    }


    private void cargarComboboxesIniciales() {
        try {
            var documentos = clienteDAO.obtenerTiposDocumento();
            System.out.println("📄 Documentos cargados: " + documentos.size());
            cmbTipoDocumento.setItems(FXCollections.observableArrayList(documentos));
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar documentos: " + e.getMessage());
        }

        try {
            var provincias = clienteDAO.obtenerProvincias();
            System.out.println("🌎 Provincias cargadas: " + provincias.size());
            cmbProvincia.setItems(FXCollections.observableArrayList(provincias));
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar provincias: " + e.getMessage());
        }

        try {
            var redes = clienteDAO.obtenerTiposRedSocial();
            System.out.println("🔗 Redes cargadas: " + redes.size());
            cmbTipoRedSocial.setItems(FXCollections.observableArrayList(redes));
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar redes sociales: " + e.getMessage());
        }
    }


    private void configurarListenersComboBox() {

        cmbProvincia.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                cmbCiudad.getItems().clear();
                cmbBarrio.getItems().clear();

                try {
                    List<String> ciudades = clienteDAO.obtenerCiudadesPorProvincia(newVal);
                    cmbCiudad.setItems(FXCollections.observableArrayList(ciudades));
                } catch (SQLException e) {
                    System.err.println("❌ Error de BD al cargar ciudades: " + e.getMessage());
                    AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "Fallo al Cargar Ciudades", "No se pudieron cargar las ciudades.");
                }
            }
        });


        cmbCiudad.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                cmbBarrio.getItems().clear();

                try {
                    List<String> barrios = clienteDAO.obtenerBarriosPorCiudad(cmbProvincia.getValue(), newVal);
                    cmbBarrio.setItems(FXCollections.observableArrayList(barrios));
                } catch (SQLException e) {
                    System.err.println("❌ Error de BD al cargar barrios: " + e.getMessage());
                    AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "Fallo al Cargar Barrios", "No se pudieron cargar los barrios.");
                }
            }
        });
    }
    @FXML

    private void handleGuardarCliente(ActionEvent event) {
        System.out.println("====== INICIANDO PROCESO DE GUARDADO ======");

        if (!validarCampos()) {
            System.out.println("❌ VALIDACIÓN FALLIDA: Algún campo obligatorio está vacío o tiene formato incorrecto.");
            return;
        }

        System.out.println("✅ Campos validados con éxito.");


        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre(txtNombre.getText().trim());
        nuevoCliente.setApellido(txtApellido.getText().trim());
        nuevoCliente.setTelefono(txtTelefono.getText().trim());
        nuevoCliente.setEmail(txtEmail.getText().trim());
        nuevoCliente.setNombreTipoDocumento(cmbTipoDocumento.getValue());
        nuevoCliente.setNumeroDocumento(txtNumeroDocumento.getText().trim());
        nuevoCliente.setCalle(txtCalle.getText().trim());
        nuevoCliente.setNumero(txtNumero.getText().trim());
        nuevoCliente.setNombreProvincia(cmbProvincia.getValue());
        nuevoCliente.setNombreCiudad(cmbCiudad.getValue());
        nuevoCliente.setNombreBarrio(cmbBarrio.getValue());

        String usuarioRed = txtUsuarioRedSocial.getText().trim();
        String tipoRed = cmbTipoRedSocial.getValue();

        if (!usuarioRed.isEmpty() && tipoRed != null) {
            ClienteRedSocial rs = new ClienteRedSocial();
            rs.setNombreUsuario(usuarioRed);
            rs.setNombreTipoRedSocial(tipoRed);
            nuevoCliente.setRedSocial(rs);
        } else {
            nuevoCliente.setRedSocial(null);
        }

        try {
            System.out.println("Buscando duplicados en el Service para: "
                    + nuevoCliente.getNombreTipoDocumento() + " " + nuevoCliente.getNumeroDocumento());


            ClienteService.ResultadoAlta resultado = clienteService.registrarCliente(nuevoCliente);
            System.out.println("🔍 RESULTADO DEL SERVICE: " + resultado);

            switch (resultado) {
                case OK -> {
                    AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Alta Exitosa",
                            "El nuevo cliente ha sido registrado en la base de datos.");
                    handleCancelar();
                }
                case DUPLICADO -> {
                    AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR,
                            "Duplicado",
                            "Cliente ya registrado",
                            "Ya existe un cliente activo con el documento "
                                    + nuevoCliente.getNombreTipoDocumento() + " "
                                    + nuevoCliente.getNumeroDocumento() + ".");
                }
                case DUPLICADO_INACTIVO -> {
                    System.out.println("🔄 Procesando caso DUPLICADO_INACTIVO...");


                    Cliente existente = clienteDAO.consultarPorDocumentoCompletoGeneral(
                            nuevoCliente.getNombreTipoDocumento(),
                            nuevoCliente.getNumeroDocumento()
                    );

                    if (existente != null) {
                        System.out.println("Cliente inactivo encontrado en BD: " + existente.getNombreCompleto() + " (ID: " + existente.getIdCliente() + ")");

                        boolean reactivar = AlertaUtil.mostrarConfirmacion(
                                "Cliente inactivo",
                                "Ya existe un cliente con ese documento pero está inactivo.",
                                "¿Desea reactivarlo y recuperar/actualizar sus datos?"
                        );

                        if (reactivar) {
                            System.out.println("Enviando orden de reactivación para ID: " + existente.getIdCliente());

                            boolean reactivado = clienteDAO.reactivarCliente(existente.getIdCliente());

                            if (reactivado) {

                                nuevoCliente.setIdCliente(existente.getIdCliente());

                                nuevoCliente.setIdPersona(existente.getIdPersona());
                                nuevoCliente.setActivo(true);


                                boolean actualizado = clienteService.actualizarCliente(nuevoCliente);

                                if (actualizado) {
                                    AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION,
                                            "Reactivación Exitosa",
                                            null,
                                            "El cliente ha sido reactivado y sus datos se actualizaron correctamente.");
                                } else {
                                    AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING,
                                            "Reactivación Exitosa",
                                            null,
                                            "El cliente fue reactivado.");
                                }
                                handleCancelar();
                            } else {
                                AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR,
                                        "Error",
                                        "Error de Reactivación",
                                        "No se pudo reactivar el cliente en la base de datos.");
                            }
                        } else {
                            System.out.println("El usuario canceló la reactivación.");
                        }
                    } else {
                        System.err.println("⚠️ ERROR CRÍTICO: El service reportó DUPLICADO_INACTIVO pero al consultar con el DAO dio NULL.");
                    }
                }
                case ERROR_INSERCION -> {
                    AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR,
                            "Fallo de Lógica", "Error de Inserción",
                            "No se pudo registrar el cliente. Posiblemente faltan IDs de FKs (Tipo Documento, Barrio).");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ ERROR DE TRANSACCIÓN BD: " + e.getMessage());
            e.printStackTrace();
            AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR, "Fallo de Base de Datos", "Error de Conexión",
                    "Ocurrió un error al intentar registrar el cliente. Verifique la conexión.");
        }
    }

    @FXML
    private void handleCancelar() {
        limpiarCampos();
    }


    private boolean validarCampos() {

        if (txtNombre.getText().trim().isEmpty() ||
                txtApellido.getText().trim().isEmpty() ||
                txtNumeroDocumento.getText().trim().isEmpty() ||
                cmbTipoDocumento.getValue() == null ||
                cmbProvincia.getValue() == null || cmbCiudad.getValue() == null || cmbBarrio.getValue() == null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Validación Incompleta", "Campos Obligatorios Vacíos",
                    "Por favor, complete Nombre, Apellido, Documento y la Dirección completa.");
            return false;
        }


        String errorEmail = clienteService.validarEmail(txtEmail.getText().trim());
        if (errorEmail != null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Validación Email", "Formato inválido", errorEmail);
            return false;
        }


        String errorTelefono = clienteService.validarTelefono(txtTelefono.getText().trim());
        if (errorTelefono != null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Validación Teléfono", "Formato inválido", errorTelefono);
            return false;
        }


        String errorDocumento = clienteService.validarDocumento(txtNumeroDocumento.getText().trim());
        if (errorDocumento != null) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Validación Documento", "Formato inválido", errorDocumento);
            return false;
        }


        boolean usuarioVacio = txtUsuarioRedSocial.getText().trim().isEmpty();
        boolean tipoVacio = cmbTipoRedSocial.getValue() == null;
        if ((!usuarioVacio && tipoVacio) || (usuarioVacio && !tipoVacio)) {
            AlertaUtil.mostrarAlerta(Alert.AlertType.WARNING, "Validación Red Social", "Información Incompleta",
                    "Debe seleccionar el Tipo de Red Social Y escribir el Usuario, o dejar ambos campos vacíos.");
            return false;
        }
        if (!clienteService.validarNombre(txtNombre.getText().trim())) {
            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Dato inválido",
                    null,
                    "El nombre solo puede contener letras."
            );
            return false;
        }

        if (!clienteService.validarNombre(txtApellido.getText().trim())) {
            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Dato inválido",
                    null,
                    "El apellido solo puede contener letras."
            );
            return false;
        }


        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtNumeroDocumento.clear();
        txtCalle.clear();
        txtNumero.clear();
        txtUsuarioRedSocial.clear();

        cmbTipoDocumento.getSelectionModel().clearSelection();
        cmbProvincia.getSelectionModel().clearSelection();
        cmbCiudad.getItems().clear();
        cmbBarrio.getItems().clear();
        cmbTipoRedSocial.getSelectionModel().clearSelection();
    }



}