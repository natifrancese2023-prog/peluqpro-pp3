 package controllers;

import claseslogicas.Disponibilidad;
import claseslogicas.Empleado;
import claseslogicas.Especialidad;

import claseslogicas.HorarioAtencion;
import dao.DocumentoDAO;
import dao.EmpleadoEspecialidadDAO;
import dao.EspecialidadDAO;
import dao.PersonaDAO;
import dao.DisponibilidadDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import service.EmpleadoService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import service.HorarioAtencionService;

public class AltaProfesionalController {

    // ============================================================
    // DAOs / SERVICES
    // ============================================================

    private final EmpleadoService empleadoService =
            new EmpleadoService();

    private final DocumentoDAO documentoDAO =
            new DocumentoDAO();

    private final PersonaDAO personaDAO =
            new PersonaDAO();

    private final EspecialidadDAO especialidadDAO =
            new EspecialidadDAO();

    private final EmpleadoEspecialidadDAO empleadoEspecialidadDAO =
            new EmpleadoEspecialidadDAO();

    private final DisponibilidadDAO disponibilidadDAO =
            new DisponibilidadDAO();
    private final HorarioAtencionService horarioAtencionService =
            new HorarioAtencionService();



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
    // DOCUMENTACIÓN
    // ============================================================

    @FXML
    private ComboBox<String> cmbTipoDocumento;

    @FXML
    private TextField txtNumeroDocumento;


    // ============================================================
    // DOMICILIO
    // ============================================================

    @FXML
    private ComboBox<String> cmbProvincia;

    @FXML
    private ComboBox<String> cmbCiudad;

    @FXML
    private ComboBox<String> cmbBarrio;

    @FXML
    private TextField txtCalle;

    @FXML
    private TextField txtNumero;


    // ============================================================
    // DATOS PROFESIONALES
    // ============================================================

    @FXML
    private DatePicker dpFechaIngreso;

    @FXML
    private TextField txtPorcentajeComision;


    // ============================================================
    // ESPECIALIDADES
    // ============================================================

    @FXML
    private ComboBox<Especialidad> cmbEspecialidad;

    @FXML
    private ListView<String> lstEspecialidades;


    // ============================================================
    // DISPONIBILIDADES
    // ============================================================

    @FXML
    private ComboBox<String> cmbDiaSemana;

    @FXML
    private ComboBox<String> cmbHoraDesde;

    @FXML
    private ComboBox<String> cmbHoraHasta;

    @FXML
    private ListView<String> lstDisponibilidades;


    // ============================================================
    // DATOS TEMPORALES DEL FORMULARIO
    // ============================================================

    private final List<Especialidad> especialidadesSeleccionadas =
            new ArrayList<>();

    private final List<Disponibilidad> disponibilidadesSeleccionadas =
            new ArrayList<>();


    // ============================================================
    // INITIALIZE
    // ============================================================

    @FXML
    public void initialize() {

        cargarTiposDocumento();
        cargarProvincias();
        cargarEspecialidades();

        configurarDias();
        configurarHorarios();

        configurarComboEspecialidad();

        dpFechaIngreso.setValue(LocalDate.now());

        cmbProvincia.setOnAction(event -> cargarCiudades());

        cmbCiudad.setOnAction(event -> cargarBarrios());
        cmbDiaSemana.setOnAction(event -> cargarHorariosDelDia());
    }


    // ============================================================
    // DOCUMENTOS
    // ============================================================

    private void cargarTiposDocumento() {

        try {

            cmbTipoDocumento.setItems(
                    FXCollections.observableArrayList(
                            documentoDAO.obtenerTiposDocumento()
                    )
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar los tipos de documento."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // PROVINCIAS
    // ============================================================

    private void cargarProvincias() {

        try {

            cmbProvincia.setItems(
                    FXCollections.observableArrayList(
                            personaDAO.obtenerProvincias()
                    )
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar las provincias."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // CIUDADES
    // ============================================================

    private void cargarCiudades() {

        String provincia = cmbProvincia.getValue();

        if (provincia == null || provincia.trim().isEmpty()) {
            return;
        }

        try {

            cmbCiudad.setItems(
                    FXCollections.observableArrayList(
                            personaDAO.obtenerCiudadesPorProvincia(
                                    provincia
                            )
                    )
            );

            cmbBarrio.getItems().clear();
            cmbBarrio.setValue(null);

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar las ciudades."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // BARRIOS
    // ============================================================

    private void cargarBarrios() {

        String provincia = cmbProvincia.getValue();
        String ciudad = cmbCiudad.getValue();

        if (provincia == null ||
                provincia.trim().isEmpty() ||
                ciudad == null ||
                ciudad.trim().isEmpty()) {

            return;
        }

        try {

            cmbBarrio.setItems(
                    FXCollections.observableArrayList(
                            personaDAO.obtenerBarriosPorCiudad(
                                    provincia,
                                    ciudad
                            )
                    )
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar los barrios."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // ESPECIALIDADES
    // ============================================================

    private void cargarEspecialidades() {

        try {

            List<Especialidad> especialidades =
                    especialidadDAO.obtenerTodas();

            cmbEspecialidad.setItems(
                    FXCollections.observableArrayList(
                            especialidades
                    )
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudieron cargar las especialidades."
            );

            e.printStackTrace();
        }
    }


    private void configurarComboEspecialidad() {

        cmbEspecialidad.setCellFactory(
                listView -> new ListCell<Especialidad>() {

                    @Override
                    protected void updateItem(
                            Especialidad especialidad,
                            boolean empty) {

                        super.updateItem(
                                especialidad,
                                empty
                        );

                        if (empty || especialidad == null) {
                            setText(null);
                        } else {
                            setText(
                                    especialidad.getNombre()
                            );
                        }
                    }
                }
        );

        cmbEspecialidad.setButtonCell(
                new ListCell<Especialidad>() {

                    @Override
                    protected void updateItem(
                            Especialidad especialidad,
                            boolean empty) {

                        super.updateItem(
                                especialidad,
                                empty
                        );

                        if (empty || especialidad == null) {
                            setText(null);
                        } else {
                            setText(
                                    especialidad.getNombre()
                            );
                        }
                    }
                }
        );
    }


    @FXML
    private void agregarEspecialidad() {

        Especialidad especialidad =
                cmbEspecialidad.getValue();

        if (especialidad == null) {

            mostrarAdvertencia(
                    "Especialidad",
                    "Seleccione una especialidad."
            );

            return;
        }

        if (especialidadesSeleccionadas.contains(
                especialidad)) {

            mostrarAdvertencia(
                    "Especialidad",
                    "La especialidad ya fue agregada."
            );

            return;
        }

        especialidadesSeleccionadas.add(
                especialidad
        );

        lstEspecialidades.getItems().add(
                especialidad.getNombre()
        );

        cmbEspecialidad.setValue(null);
    }


    // ============================================================
    // DÍAS DE LA SEMANA
    // ============================================================

    private void configurarDias() {

        cmbDiaSemana.setItems(
                FXCollections.observableArrayList(
                        "Lunes",
                        "Martes",
                        "Miércoles",
                        "Jueves",
                        "Viernes",
                        "Sábado",
                        "Domingo"
                )
        );
    }


    // ============================================================
    // HORARIOS
    // ============================================================
    private void configurarHorarios() {
        cmbHoraDesde.setItems(FXCollections.observableArrayList());
        cmbHoraHasta.setItems(FXCollections.observableArrayList());
    }
    // ============================================================
    // AGREGAR DISPONIBILIDAD
    // ============================================================
    @FXML
    private void agregarDisponibilidad() {

        String dia = cmbDiaSemana.getValue();
        String desde = cmbHoraDesde.getValue();
        String hasta = cmbHoraHasta.getValue();

        if (dia == null || desde == null || hasta == null) {

            mostrarAdvertencia(
                    "Disponibilidad",
                    "Complete día, hora desde y hora hasta."
            );

            return;
        }

        LocalTime horaDesde = LocalTime.parse(desde);
        LocalTime horaHasta = LocalTime.parse(hasta);

        if (!horaDesde.isBefore(horaHasta)) {

            mostrarAdvertencia(
                    "Horario inválido",
                    "La hora desde debe ser anterior a la hora hasta."
            );

            return;
        }

        try {

            HorarioAtencion horario =
                    horarioAtencionService.obtenerHorarioPorDiaSemana(dia);

            if (horario == null
                    || horario.getHoraApertura().equals(horario.getHoraCierre())) {

                mostrarAdvertencia(
                        "Horario de atención",
                        "La peluquería no tiene atención configurada para " + dia + "."
                );

                return;
            }

            LocalTime horaApertura = horario.getHoraApertura();
            LocalTime horaCierre = horario.getHoraCierre();

            if (horaDesde.isBefore(horaApertura)
                    || horaHasta.isAfter(horaCierre)) {

                mostrarAdvertencia(
                        "Horario inválido",
                        "La disponibilidad debe estar comprendida entre "
                                + horaApertura + " y " + horaCierre + "."
                );

                return;
            }

            for (Disponibilidad d : disponibilidadesSeleccionadas) {

                if (d.getDiaSemana().equals(dia)
                        && d.getHoraDesde().equals(horaDesde)
                        && d.getHoraHasta().equals(horaHasta)) {

                    mostrarAdvertencia(
                            "Disponibilidad duplicada",
                            "Ya agregó esa disponibilidad para el día seleccionado."
                    );

                    return;
                }
            }

            Disponibilidad disponibilidad = new Disponibilidad();

            disponibilidad.setDiaSemana(dia);
            disponibilidad.setHoraDesde(horaDesde);
            disponibilidad.setHoraHasta(horaHasta);
            disponibilidad.setActivo(true);

            disponibilidadesSeleccionadas.add(disponibilidad);

            lstDisponibilidades.getItems().add(
                    dia + " | " + desde + " - " + hasta
            );

            cmbDiaSemana.setValue(null);
            cmbHoraDesde.setValue(null);
            cmbHoraHasta.setValue(null);

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudo consultar el horario de atención."
            );

            e.printStackTrace();
        }
    }
    // ============================================================
    // GUARDAR PROFESIONAL
    // ============================================================

    @FXML
    private void guardarProfesional() {

        if (!validarFormulario()) {
            return;
        }


        try {

            Empleado empleado =
                    new Empleado();


            // --------------------------------------------
            // DATOS PERSONALES
            // --------------------------------------------

            empleado.setNombre(
                    txtNombre.getText().trim()
            );

            empleado.setApellido(
                    txtApellido.getText().trim()
            );

            empleado.setTelefono(
                    txtTelefono.getText().trim()
            );

            empleado.setEmail(
                    txtEmail.getText().trim()
            );


            // --------------------------------------------
            // DOMICILIO
            // --------------------------------------------

            empleado.setCalle(
                    txtCalle.getText().trim()
            );

            empleado.setNumero(
                    txtNumero.getText().trim()
            );


            // --------------------------------------------
            // DATOS PROFESIONALES
            // --------------------------------------------

            empleado.setFechaIngreso(
                    dpFechaIngreso.getValue()
            );


            double porcentajeComision =
                    Double.parseDouble(
                            txtPorcentajeComision
                                    .getText()
                                    .trim()
                    );


            empleado.setPorcentajeComision(
                    porcentajeComision
            );


            // --------------------------------------------
            // CREAR PROFESIONAL
            // --------------------------------------------

            boolean registrado =
                    empleadoService.registrarProfesional(
                            empleado,
                            cmbTipoDocumento.getValue(),
                            txtNumeroDocumento
                                    .getText()
                                    .trim(),
                            cmbBarrio.getValue()
                    );


            if (!registrado) {

                mostrarError(
                        "Error",
                        "No se pudo registrar el profesional."
                );

                return;
            }


            // --------------------------------------------
            // GUARDAR ESPECIALIDADES
            // --------------------------------------------

            for (Especialidad especialidad :
                    especialidadesSeleccionadas) {

                empleadoEspecialidadDAO
                        .asignarEspecialidad(
                                empleado.getIdEmpleado(),
                                especialidad
                                        .getIdEspecialidad()
                        );
            }


            // --------------------------------------------
            // GUARDAR DISPONIBILIDADES
            // --------------------------------------------

            for (Disponibilidad disponibilidad :
                    disponibilidadesSeleccionadas) {

                disponibilidad.setIdEmpleado(
                        empleado.getIdEmpleado()
                );

                disponibilidadDAO.insertar(
                        disponibilidad
                );
            }


            // --------------------------------------------
            // ÉXITO
            // --------------------------------------------

            mostrarInformacion(
                    "Profesional registrado",
                    "El profesional fue registrado correctamente."
            );


            limpiarFormulario();


        } catch (NumberFormatException e) {

            mostrarAdvertencia(
                    "Datos incorrectos",
                    "El porcentaje de comisión debe ser numérico."
            );


        } catch (SQLException e) {

            mostrarError(
                    "Error al registrar",
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // VALIDACIONES
    // ============================================================
    private boolean validarFormulario() {

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String numeroDocumento = txtNumeroDocumento.getText().trim();
        String calle = txtCalle.getText().trim();
        String numero = txtNumero.getText().trim();
        String porcentajeTexto = txtPorcentajeComision.getText().trim();

        // NOMBRE
        if (nombre.isEmpty()) {
            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese el nombre."
            );
            return false;
        }

        if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
            mostrarAdvertencia(
                    "Dato inválido",
                    "El nombre solo puede contener letras y espacios."
            );
            return false;
        }

        // APELLIDO
        if (apellido.isEmpty()) {
            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese el apellido."
            );
            return false;
        }

        if (!apellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
            mostrarAdvertencia(
                    "Dato inválido",
                    "El apellido solo puede contener letras y espacios."
            );
            return false;
        }

        // TELÉFONO
        if (telefono.isEmpty()) {
            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese el teléfono."
            );
            return false;
        }

        if (!telefono.matches("[0-9+()\\- ]{7,20}")) {
            mostrarAdvertencia(
                    "Dato inválido",
                    "Ingrese un número de teléfono válido."
            );
            return false;
        }

        // EMAIL
        if (!email.isEmpty()) {

            if (!Pattern.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                    email)) {

                mostrarAdvertencia(
                        "Dato inválido",
                        "Ingrese un correo electrónico válido."
                );

                return false;
            }
        }

        // DOCUMENTO
        if (cmbTipoDocumento.getValue() == null) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Seleccione el tipo de documento."
            );

            return false;
        }

        if (numeroDocumento.isEmpty()) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese el número de documento."
            );

            return false;
        }

        if (!numeroDocumento.matches("\\d{7,10}")) {

            mostrarAdvertencia(
                    "Dato inválido",
                    "El número de documento debe contener entre 7 y 10 dígitos."
            );

            return false;
        }

        // DOMICILIO
        if (cmbProvincia.getValue() == null) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Seleccione la provincia."
            );

            return false;
        }

        if (cmbCiudad.getValue() == null) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Seleccione la ciudad."
            );

            return false;
        }

        if (cmbBarrio.getValue() == null) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Seleccione el barrio."
            );

            return false;
        }

        if (calle.isEmpty()) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese la calle."
            );

            return false;
        }

        if (numero.isEmpty()) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese el número del domicilio."
            );

            return false;
        }

        if (!numero.matches("\\d+")) {

            mostrarAdvertencia(
                    "Dato inválido",
                    "El número del domicilio debe ser numérico."
            );

            return false;
        }

        // FECHA DE INGRESO
        if (dpFechaIngreso.getValue() == null) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Seleccione la fecha de ingreso."
            );

            return false;
        }

        if (dpFechaIngreso.getValue().isAfter(LocalDate.now())) {

            mostrarAdvertencia(
                    "Fecha inválida",
                    "La fecha de ingreso no puede ser posterior a la fecha actual."
            );

            return false;
        }

        // COMISIÓN
        if (porcentajeTexto.isEmpty()) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Ingrese el porcentaje de comisión."
            );

            return false;
        }

        double porcentaje;

        try {

            porcentaje = Double.parseDouble(
                    porcentajeTexto.replace(",", ".")
            );

        } catch (NumberFormatException e) {

            mostrarAdvertencia(
                    "Dato inválido",
                    "El porcentaje de comisión debe ser numérico."
            );

            return false;
        }

        if (porcentaje < 0 || porcentaje > 100) {

            mostrarAdvertencia(
                    "Dato inválido",
                    "El porcentaje de comisión debe estar entre 0 y 100."
            );

            return false;
        }

        // ESPECIALIDADES
        if (especialidadesSeleccionadas.isEmpty()) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Debe asignar al menos una especialidad al profesional."
            );

            return false;
        }

        // DISPONIBILIDADES
        if (disponibilidadesSeleccionadas.isEmpty()) {

            mostrarAdvertencia(
                    "Datos incompletos",
                    "Debe registrar al menos una disponibilidad."
            );

            return false;
        }

        return true;
    }

    // ============================================================
    // LIMPIAR
    // ============================================================

    @FXML
    private void limpiarFormulario() {

        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtEmail.clear();


        cmbTipoDocumento.setValue(null);
        txtNumeroDocumento.clear();


        cmbProvincia.setValue(null);

        cmbCiudad.getItems().clear();
        cmbCiudad.setValue(null);

        cmbBarrio.getItems().clear();
        cmbBarrio.setValue(null);


        txtCalle.clear();
        txtNumero.clear();


        dpFechaIngreso.setValue(
                LocalDate.now()
        );


        txtPorcentajeComision.clear();


        cmbEspecialidad.setValue(null);

        especialidadesSeleccionadas.clear();

        lstEspecialidades.getItems().clear();


        cmbDiaSemana.setValue(null);
        cmbHoraDesde.setValue(null);
        cmbHoraHasta.setValue(null);

        disponibilidadesSeleccionadas.clear();

        lstDisponibilidades.getItems().clear();
    }


    // ============================================================
    // MENSAJES
    // ============================================================

    private void mostrarAdvertencia(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }


    private void mostrarError(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }


    private void mostrarInformacion(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }
    private void cargarHorariosDelDia() {

        String dia = cmbDiaSemana.getValue();

        cmbHoraDesde.getItems().clear();
        cmbHoraHasta.getItems().clear();

        if (dia == null) {
            return;
        }

        try {

            HorarioAtencion horario =
                    horarioAtencionService.obtenerHorarioPorDiaSemana(dia);

            if (horario == null) {
                mostrarAdvertencia(
                        "Horario de atención",
                        "No existe un horario configurado para " + dia + "."
                );
                return;
            }

            LocalTime apertura = horario.getHoraApertura();
            LocalTime cierre = horario.getHoraCierre();

            // Día cerrado
            if (apertura.equals(cierre)) {
                mostrarAdvertencia(
                        "Horario de atención",
                        "La peluquería no tiene atención los días " + dia + "."
                );
                return;
            }

            List<String> horarios = new ArrayList<>();

            LocalTime hora = apertura;

            while (!hora.isAfter(cierre)) {

                horarios.add(
                        String.format(
                                "%02d:%02d",
                                hora.getHour(),
                                hora.getMinute()
                        )
                );

                hora = hora.plusMinutes(30);
            }

            cmbHoraDesde.setItems(
                    FXCollections.observableArrayList(horarios)
            );

            cmbHoraHasta.setItems(
                    FXCollections.observableArrayList(horarios)
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudo consultar el horario de atención."
            );

            e.printStackTrace();
        }
    }
}