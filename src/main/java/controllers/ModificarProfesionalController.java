package controllers;

import claseslogicas.Disponibilidad;
import claseslogicas.Empleado;
import claseslogicas.Especialidad;
import claseslogicas.HorarioAtencion;
import claseslogicas.Persona;

import dao.DisponibilidadDAO;
import dao.EmpleadoDAO;
import dao.EmpleadoEspecialidadDAO;
import dao.EspecialidadDAO;
import dao.PersonaDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import service.HorarioAtencionService;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ModificarProfesionalController {

    // ==========================================================
    // DATOS PERSONALES
    // ==========================================================

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtEmail;


    // ==========================================================
    // DOCUMENTACIÓN
    // ==========================================================

    @FXML
    private TextField txtTipoDocumento;

    @FXML
    private TextField txtNumeroDocumento;


    // ==========================================================
    // DOMICILIO
    // ==========================================================

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


    // ==========================================================
    // DATOS PROFESIONALES
    // ==========================================================

    @FXML
    private DatePicker dpFechaIngreso;

    @FXML
    private TextField txtPorcentajeComision;

    @FXML
    private TextField txtRol;


    // ==========================================================
    // ESPECIALIDADES
    // ==========================================================

    @FXML
    private ComboBox<Especialidad> cmbEspecialidad;

    @FXML
    private ListView<String> lstEspecialidades;


    // ==========================================================
    // DISPONIBILIDADES
    // ==========================================================

    @FXML
    private ComboBox<String> cmbDiaSemana;

    @FXML
    private ComboBox<String> cmbHoraDesde;

    @FXML
    private ComboBox<String> cmbHoraHasta;

    @FXML
    private TableView<Disponibilidad> tblDisponibilidades;

    @FXML
    private TableColumn<Disponibilidad, String> colDia;

    @FXML
    private TableColumn<Disponibilidad, String> colHoraDesde;

    @FXML
    private TableColumn<Disponibilidad, String> colHoraHasta;

    @FXML
    private TableColumn<Disponibilidad, String> colEstado;


    // ==========================================================
    // DAOs / SERVICES
    // ==========================================================

    private final EmpleadoDAO empleadoDAO =
            new EmpleadoDAO();

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


    // ==========================================================
    // DATOS
    // ==========================================================

    private int idEmpleado;

    private Empleado empleadoActual;


    private final List<Especialidad> especialidadesSeleccionadas =
            new ArrayList<>();

    private final List<Especialidad> especialidadesOriginales =
            new ArrayList<>();


    private final List<Disponibilidad> disponibilidadesSeleccionadas =
            new ArrayList<>();

    private final List<Integer> disponibilidadesQuitadas =
            new ArrayList<>();


    // ==========================================================
    // RECIBIR ID DEL PROFESIONAL
    // ==========================================================

    public void setIdEmpleado(int idEmpleado) {

        this.idEmpleado = idEmpleado;

        cargarProfesional();
    }


    // ==========================================================
    // INITIALIZE
    // ==========================================================

    @FXML
    public void initialize() {

        cargarProvincias();

        cargarEspecialidades();

        configurarDias();

        configurarTablaDisponibilidades();

        cmbProvincia.setOnAction(
                event -> cargarCiudades()
        );

        cmbCiudad.setOnAction(
                event -> cargarBarrios()
        );

        cmbDiaSemana.setOnAction(
                event -> cargarHorariosDelDia()
        );
        tblDisponibilidades
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, seleccionada) -> {

                            if (seleccionada != null) {

                                cmbDiaSemana.setValue(
                                        seleccionada.getDiaSemana()
                                );

                                cargarHorariosDelDia();

                                cmbHoraDesde.setValue(
                                        formatearHora(
                                                seleccionada.getHoraDesde()
                                        )
                                );

                                cmbHoraHasta.setValue(
                                        formatearHora(
                                                seleccionada.getHoraHasta()
                                        )
                                );
                            }
                        }
                );
    }


    // ==========================================================
    // CARGAR PROFESIONAL
    // ==========================================================

    private void cargarProfesional() {

        try {

            Empleado empleado =
                    empleadoDAO.obtenerPorId(idEmpleado);

            if (empleado == null) {

                mostrarError(
                        "Profesional no encontrado",
                        "No se encontró el profesional seleccionado."
                );

                return;
            }

            empleadoActual = empleado;

            cargarDatos(empleado);

            cargarEspecialidadesProfesional();

            cargarDisponibilidadesProfesional();

        } catch (SQLException e) {

            mostrarError(
                    "Error de base de datos",
                    "No se pudieron cargar los datos del profesional."
            );

            e.printStackTrace();
        }
    }


    // ==========================================================
    // CARGAR DATOS PERSONALES
    // ==========================================================

    private void cargarDatos(Empleado empleado) {

        Persona persona = empleado.getPersona();

        if (persona != null) {

            txtNombre.setText(
                    persona.getNombre() != null
                            ? persona.getNombre()
                            : ""
            );

            txtApellido.setText(
                    persona.getApellido() != null
                            ? persona.getApellido()
                            : ""
            );

            txtTelefono.setText(
                    persona.getTelefono() != null
                            ? persona.getTelefono()
                            : ""
            );

            txtEmail.setText(
                    persona.getEmail() != null
                            ? persona.getEmail()
                            : ""
            );

            txtCalle.setText(
                    persona.getCalle() != null
                            ? persona.getCalle()
                            : ""
            );

            txtNumero.setText(
                    persona.getNumero() != null
                            ? persona.getNumero()
                            : ""
            );

            txtTipoDocumento.setText(
                    persona.getNombreTipoDocumento() != null
                            ? persona.getNombreTipoDocumento()
                            : ""
            );

            txtNumeroDocumento.setText(
                    persona.getNumeroDocumento() != null
                            ? persona.getNumeroDocumento()
                            : ""
            );


            // Provincia
            if (persona.getNombreProvincia() != null) {

                cmbProvincia.getSelectionModel()
                        .select(persona.getNombreProvincia());

                cargarCiudades();
            }


            // Ciudad
            if (persona.getNombreCiudad() != null) {

                cmbCiudad.getSelectionModel()
                        .select(persona.getNombreCiudad());

                cargarBarrios();
            }


            // Barrio
            if (persona.getNombreBarrio() != null) {

                cmbBarrio.getSelectionModel()
                        .select(persona.getNombreBarrio());
            }
        }


        // Fecha de ingreso
        if (empleado.getFechaIngreso() != null) {

            dpFechaIngreso.setValue(
                    empleado.getFechaIngreso()
            );
        }


        // Comisión
        txtPorcentajeComision.setText(
                String.valueOf(
                        empleado.getPorcentajeComision()
                )
        );


        // Rol
        if (empleado.getRol() != null) {

            txtRol.setText(
                    empleado.getRol().getNombre()
            );
        }
    }


    // ==========================================================
    // PROVINCIAS
    // ==========================================================

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


    // ==========================================================
    // CIUDADES
    // ==========================================================

    private void cargarCiudades() {

        String provincia =
                cmbProvincia.getValue();

        if (provincia == null) {
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


    // ==========================================================
    // BARRIOS
    // ==========================================================

    private void cargarBarrios() {

        String provincia =
                cmbProvincia.getValue();

        String ciudad =
                cmbCiudad.getValue();

        if (provincia == null || ciudad == null) {
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


    // ==========================================================
    // ESPECIALIDADES DISPONIBLES
    // ==========================================================

    private void cargarEspecialidades() {

        try {

            cmbEspecialidad.setItems(
                    FXCollections.observableArrayList(
                            especialidadDAO.obtenerTodas()
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


    // ==========================================================
    // CARGAR ESPECIALIDADES DEL PROFESIONAL
    // ==========================================================

    private void cargarEspecialidadesProfesional()
            throws SQLException {

        especialidadesSeleccionadas.clear();
        especialidadesOriginales.clear();
        lstEspecialidades.getItems().clear();

        List<Especialidad> lista =
                empleadoEspecialidadDAO
                        .obtenerEspecialidadesPorEmpleado(
                                idEmpleado
                        );

        for (Especialidad especialidad : lista) {

            if (!especialidad.isActivo()) {
                continue;
            }

            especialidadesSeleccionadas.add(
                    especialidad
            );

            especialidadesOriginales.add(
                    especialidad
            );

            lstEspecialidades.getItems().add(
                    especialidad.getNombre()
            );
        }
    }


    // ==========================================================
    // AGREGAR ESPECIALIDAD
    // ==========================================================

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

        for (Especialidad existente :
                especialidadesSeleccionadas) {

            if (existente.getIdEspecialidad()
                    == especialidad.getIdEspecialidad()) {

                mostrarAdvertencia(
                        "Especialidad",
                        "La especialidad ya está asignada al profesional."
                );

                return;
            }
        }

        especialidadesSeleccionadas.add(
                especialidad
        );

        lstEspecialidades.getItems().add(
                especialidad.getNombre()
        );

        cmbEspecialidad.setValue(null);
    }


    // ==========================================================
    // QUITAR ESPECIALIDAD
    // ==========================================================

    @FXML
    private void quitarEspecialidad() {

        int indice =
                lstEspecialidades
                        .getSelectionModel()
                        .getSelectedIndex();

        if (indice < 0) {

            mostrarAdvertencia(
                    "Especialidad",
                    "Seleccione una especialidad para quitar."
            );

            return;
        }

        especialidadesSeleccionadas.remove(
                indice
        );

        lstEspecialidades.getItems().remove(
                indice
        );
    }


    // ==========================================================
    // DÍAS
    // ==========================================================

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


    // ==========================================================
    // TABLA DISPONIBILIDADES
    // ==========================================================

    private void configurarTablaDisponibilidades() {

        colDia.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getDiaSemana()
                        )
        );

        colHoraDesde.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                formatearHora(
                                        data.getValue().getHoraDesde()
                                )
                        )
        );

        colHoraHasta.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                formatearHora(
                                        data.getValue().getHoraHasta()
                                )
                        )
        );

        colEstado.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().isActivo()
                                        ? "Activo"
                                        : "Inactivo"
                        )
        );
    }


    private String formatearHora(LocalTime hora) {

        if (hora == null) {
            return "";
        }

        return String.format(
                "%02d:%02d",
                hora.getHour(),
                hora.getMinute()
        );
    }


    // ==========================================================
    // CARGAR DISPONIBILIDADES
    // ==========================================================

    private void cargarDisponibilidadesProfesional()
            throws SQLException {

        disponibilidadesSeleccionadas.clear();

        disponibilidadesQuitadas.clear();

        List<Disponibilidad> lista =
                disponibilidadDAO.obtenerPorEmpleado(
                        idEmpleado
                );

        for (Disponibilidad disponibilidad : lista) {

            if (disponibilidad.isActivo()) {

                disponibilidadesSeleccionadas.add(
                        disponibilidad
                );
            }
        }

        actualizarTablaDisponibilidades();
    }


    private void actualizarTablaDisponibilidades() {

        ObservableList<Disponibilidad> datos =
                FXCollections.observableArrayList(
                        disponibilidadesSeleccionadas
                );

        tblDisponibilidades.setItems(datos);
    }


    // ==========================================================
    // HORARIOS SEGÚN HORARIO DE ATENCIÓN
    // ==========================================================

    private void cargarHorariosDelDia() {

        String dia =
                cmbDiaSemana.getValue();

        cmbHoraDesde.getItems().clear();
        cmbHoraHasta.getItems().clear();

        if (dia == null) {
            return;
        }

        try {

            /*
             * MISMA LÓGICA UTILIZADA EN ALTA PROFESIONAL.
             */
            HorarioAtencion horario =
                    horarioAtencionService
                            .obtenerHorarioPorDiaSemana(
                                    dia
                            );

            if (horario == null) {

                mostrarAdvertencia(
                        "Horario de atención",
                        "No existe un horario configurado para "
                                + dia
                                + "."
                );

                return;
            }

            LocalTime apertura =
                    horario.getHoraApertura();

            LocalTime cierre =
                    horario.getHoraCierre();


            // Día cerrado
            if (apertura.equals(cierre)) {

                mostrarAdvertencia(
                        "Horario de atención",
                        "La peluquería no tiene atención los días "
                                + dia
                                + "."
                );

                return;
            }


            List<String> horarios =
                    new ArrayList<>();

            LocalTime hora =
                    apertura;


            while (!hora.isAfter(cierre)) {

                horarios.add(
                        String.format(
                                "%02d:%02d",
                                hora.getHour(),
                                hora.getMinute()
                        )
                );

                hora =
                        hora.plusMinutes(30);
            }


            cmbHoraDesde.setItems(
                    FXCollections.observableArrayList(
                            horarios
                    )
            );

            cmbHoraHasta.setItems(
                    FXCollections.observableArrayList(
                            horarios
                    )
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudo consultar el horario de atención."
            );

            e.printStackTrace();
        }
    }


    // ==========================================================
    // AGREGAR DISPONIBILIDAD
    // ==========================================================

    @FXML
    private void agregarDisponibilidad() {

        String dia =
                cmbDiaSemana.getValue();

        String desde =
                cmbHoraDesde.getValue();

        String hasta =
                cmbHoraHasta.getValue();


        if (dia == null
                || desde == null
                || hasta == null) {

            mostrarAdvertencia(
                    "Disponibilidad",
                    "Complete día, hora desde y hora hasta."
            );

            return;
        }


        LocalTime horaDesde =
                LocalTime.parse(desde);

        LocalTime horaHasta =
                LocalTime.parse(hasta);


        if (!horaDesde.isBefore(horaHasta)) {

            mostrarAdvertencia(
                    "Horario inválido",
                    "La hora desde debe ser anterior a la hora hasta."
            );

            return;
        }


        try {

            HorarioAtencion horario =
                    horarioAtencionService
                            .obtenerHorarioPorDiaSemana(
                                    dia
                            );


            if (horario == null
                    || horario.getHoraApertura()
                    .equals(
                            horario.getHoraCierre()
                    )) {

                mostrarAdvertencia(
                        "Horario de atención",
                        "La peluquería no tiene atención configurada para "
                                + dia
                                + "."
                );

                return;
            }


            LocalTime horaApertura =
                    horario.getHoraApertura();

            LocalTime horaCierre =
                    horario.getHoraCierre();


            if (horaDesde.isBefore(horaApertura)
                    || horaHasta.isAfter(horaCierre)) {

                mostrarAdvertencia(
                        "Horario inválido",
                        "La disponibilidad debe estar comprendida entre "
                                + formatearHora(horaApertura)
                                + " y "
                                + formatearHora(horaCierre)
                                + "."
                );

                return;
            }


            // Evitar duplicados
            for (Disponibilidad d :
                    disponibilidadesSeleccionadas) {

                if (d.getDiaSemana().equals(dia)
                        && d.getHoraDesde().equals(horaDesde)
                        && d.getHoraHasta().equals(horaHasta)) {

                    mostrarAdvertencia(
                            "Disponibilidad duplicada",
                            "Ya existe esa disponibilidad."
                    );

                    return;
                }
            }


            Disponibilidad disponibilidad =
                    new Disponibilidad();

            disponibilidad.setIdEmpleado(
                    idEmpleado
            );

            disponibilidad.setDiaSemana(
                    dia
            );

            disponibilidad.setHoraDesde(
                    horaDesde
            );

            disponibilidad.setHoraHasta(
                    horaHasta
            );

            disponibilidad.setActivo(true);


            disponibilidadesSeleccionadas.add(
                    disponibilidad
            );


            actualizarTablaDisponibilidades();


            limpiarControlesDisponibilidad();

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudo consultar el horario de atención."
            );

            e.printStackTrace();
        }
    }


    // ==========================================================
    // MODIFICAR DISPONIBILIDAD
    // ==========================================================

    @FXML
    private void modificarDisponibilidad() {

        Disponibilidad seleccionada =
                tblDisponibilidades
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {

            mostrarAdvertencia(
                    "Disponibilidad",
                    "Seleccione una disponibilidad."
            );

            return;
        }


        String dia =
                cmbDiaSemana.getValue();

        String desde =
                cmbHoraDesde.getValue();

        String hasta =
                cmbHoraHasta.getValue();


        if (dia == null
                || desde == null
                || hasta == null) {

            mostrarAdvertencia(
                    "Disponibilidad",
                    "Seleccione día, hora desde y hora hasta."
            );

            return;
        }


        LocalTime horaDesde =
                LocalTime.parse(desde);

        LocalTime horaHasta =
                LocalTime.parse(hasta);


        if (!horaDesde.isBefore(horaHasta)) {

            mostrarAdvertencia(
                    "Horario inválido",
                    "La hora desde debe ser anterior a la hora hasta."
            );

            return;
        }


        try {

            HorarioAtencion horario =
                    horarioAtencionService
                            .obtenerHorarioPorDiaSemana(
                                    dia
                            );


            if (horario == null
                    || horario.getHoraApertura()
                    .equals(
                            horario.getHoraCierre()
                    )) {

                mostrarAdvertencia(
                        "Horario de atención",
                        "No existe atención configurada para "
                                + dia
                                + "."
                );

                return;
            }


            if (horaDesde.isBefore(
                    horario.getHoraApertura())
                    || horaHasta.isAfter(
                    horario.getHoraCierre())) {

                mostrarAdvertencia(
                        "Horario inválido",
                        "La disponibilidad debe estar comprendida entre "
                                + formatearHora(
                                horario.getHoraApertura())
                                + " y "
                                + formatearHora(
                                horario.getHoraCierre())
                                + "."
                );

                return;
            }


            // Evitar duplicados
            for (Disponibilidad d :
                    disponibilidadesSeleccionadas) {

                if (d == seleccionada) {
                    continue;
                }

                if (d.getDiaSemana().equals(dia)
                        && d.getHoraDesde().equals(horaDesde)
                        && d.getHoraHasta().equals(horaHasta)) {

                    mostrarAdvertencia(
                            "Disponibilidad duplicada",
                            "Ya existe esa disponibilidad."
                    );

                    return;
                }
            }


            seleccionada.setDiaSemana(dia);
            seleccionada.setHoraDesde(horaDesde);
            seleccionada.setHoraHasta(horaHasta);


            actualizarTablaDisponibilidades();

            limpiarControlesDisponibilidad();

        } catch (SQLException e) {

            mostrarError(
                    "Error",
                    "No se pudo validar el horario de atención."
            );

            e.printStackTrace();
        }
    }


    // ==========================================================
    // SELECCIONAR DISPONIBILIDAD
    // ==========================================================

    @FXML
    private void seleccionarDisponibilidad() {

        Disponibilidad seleccionada =
                tblDisponibilidades
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        cmbDiaSemana.setValue(
                seleccionada.getDiaSemana()
        );

        cmbHoraDesde.setValue(
                formatearHora(
                        seleccionada.getHoraDesde()
                )
        );

        cmbHoraHasta.setValue(
                formatearHora(
                        seleccionada.getHoraHasta()
                )
        );
    }


    // ==========================================================
    // QUITAR DISPONIBILIDAD
    // ==========================================================

    @FXML
    private void quitarDisponibilidad() {

        Disponibilidad seleccionada =
                tblDisponibilidades
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {

            mostrarAdvertencia(
                    "Disponibilidad",
                    "Seleccione una disponibilidad para quitar."
            );

            return;
        }


        if (seleccionada.getIdDisponibilidad() > 0) {

            disponibilidadesQuitadas.add(
                    seleccionada.getIdDisponibilidad()
            );
        }


        disponibilidadesSeleccionadas.remove(
                seleccionada
        );


        actualizarTablaDisponibilidades();

        limpiarControlesDisponibilidad();
    }


    // ==========================================================
    // LIMPIAR CONTROLES DISPONIBILIDAD
    // ==========================================================

    private void limpiarControlesDisponibilidad() {

        cmbDiaSemana.setValue(null);

        cmbHoraDesde.getItems().clear();

        cmbHoraHasta.getItems().clear();

        cmbHoraDesde.setValue(null);

        cmbHoraHasta.setValue(null);
    }


    // ==========================================================
    // GUARDAR CAMBIOS
    // ==========================================================

    @FXML
    private void handleGuardarCambios() {

        if (!validarDatos()) {
            return;
        }


        try {

            Empleado empleado =
                    empleadoDAO.obtenerPorId(
                            idEmpleado
                    );


            if (empleado == null) {

                mostrarError(
                        "Error",
                        "No se encontró el profesional."
                );

                return;
            }


            Persona persona =
                    empleado.getPersona();


            if (persona == null) {

                mostrarError(
                        "Error",
                        "El profesional no tiene una persona asociada."
                );

                return;
            }


            // ==================================================
            // DATOS PERSONALES
            // ==================================================

            persona.setNombre(
                    txtNombre.getText().trim()
            );

            persona.setApellido(
                    txtApellido.getText().trim()
            );

            persona.setTelefono(
                    txtTelefono.getText().trim()
            );

            persona.setEmail(
                    txtEmail.getText().trim()
            );

            persona.setCalle(
                    txtCalle.getText().trim()
            );

            persona.setNumero(
                    txtNumero.getText().trim()
            );


            // ==================================================
            // DATOS PROFESIONALES
            // ==================================================

            empleado.setFechaIngreso(
                    dpFechaIngreso.getValue()
            );

            empleado.setPorcentajeComision(
                    Double.parseDouble(
                            txtPorcentajeComision
                                    .getText()
                                    .trim()
                    )
            );


            // ==================================================
            // ACTUALIZAR DATOS PRINCIPALES
            // ==================================================

            boolean actualizado =
                    empleadoDAO.actualizar(
                            empleado,
                            cmbBarrio.getValue()
                    );


            if (!actualizado) {

                mostrarError(
                        "Error",
                        "No se pudieron actualizar los datos del profesional."
                );

                return;
            }


            // ==================================================
            // SINCRONIZAR ESPECIALIDADES
            // ==================================================

            sincronizarEspecialidades();


            // ==================================================
            // SINCRONIZAR DISPONIBILIDADES
            // ==================================================

            sincronizarDisponibilidades();


            mostrarInformacion(
                    "Datos actualizados",
                    "Los datos del profesional fueron actualizados correctamente."
            );


            cerrar();

        } catch (NumberFormatException e) {

            mostrarError(
                    "Error",
                    "El porcentaje de comisión debe ser numérico."
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error de base de datos",
                    "No se pudieron guardar los cambios.\n\n"
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    // ==========================================================
    // SINCRONIZAR ESPECIALIDADES
    // ==========================================================

    private void sincronizarEspecialidades()
            throws SQLException {

        /*
         * Quitamos las especialidades que estaban originalmente
         * pero que ya no están seleccionadas.
         */

        for (Especialidad original :
                especialidadesOriginales) {

            boolean sigueAsignada = false;

            for (Especialidad actual :
                    especialidadesSeleccionadas) {

                if (actual.getIdEspecialidad()
                        == original.getIdEspecialidad()) {

                    sigueAsignada = true;
                    break;
                }
            }

            if (!sigueAsignada) {

                empleadoEspecialidadDAO
                        .quitarEspecialidad(
                                idEmpleado,
                                original.getIdEspecialidad()
                        );
            }
        }


        /*
         * Agregamos las especialidades nuevas.
         */

        for (Especialidad actual :
                especialidadesSeleccionadas) {

            boolean yaExistia = false;

            for (Especialidad original :
                    especialidadesOriginales) {

                if (actual.getIdEspecialidad()
                        == original.getIdEspecialidad()) {

                    yaExistia = true;
                    break;
                }
            }

            if (!yaExistia) {

                empleadoEspecialidadDAO
                        .asignarEspecialidad(
                                idEmpleado,
                                actual.getIdEspecialidad()
                        );
            }
        }
    }


    // ==========================================================
    // SINCRONIZAR DISPONIBILIDADES
    // ==========================================================

    private void sincronizarDisponibilidades()
            throws SQLException {

        /*
         * Desactivar las disponibilidades que fueron quitadas.
         */

        for (Integer idDisponibilidad :
                disponibilidadesQuitadas) {

            disponibilidadDAO.actualizarEstado(
                    idDisponibilidad,
                    false
            );
        }


        /*
         * Actualizar las existentes e insertar
         * las nuevas.
         */

        for (Disponibilidad disponibilidad :
                disponibilidadesSeleccionadas) {

            if (disponibilidad.getIdDisponibilidad() > 0) {

                disponibilidadDAO.actualizar(
                        disponibilidad
                );

            } else {

                disponibilidad.setIdEmpleado(
                        idEmpleado
                );

                disponibilidadDAO.insertar(
                        disponibilidad
                );
            }
        }
    }


    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    private boolean validarDatos() {

        if (txtNombre.getText() == null
                || txtNombre.getText().trim().isEmpty()) {

            mostrarAdvertencia(
                    "Validación",
                    "Ingrese el nombre."
            );

            return false;
        }


        if (txtApellido.getText() == null
                || txtApellido.getText().trim().isEmpty()) {

            mostrarAdvertencia(
                    "Validación",
                    "Ingrese el apellido."
            );

            return false;
        }


        if (cmbBarrio.getValue() == null
                || cmbBarrio.getValue().trim().isEmpty()) {

            mostrarAdvertencia(
                    "Validación",
                    "Seleccione el barrio."
            );

            return false;
        }


        if (dpFechaIngreso.getValue() == null) {

            mostrarAdvertencia(
                    "Validación",
                    "Seleccione la fecha de ingreso."
            );

            return false;
        }


        String porcentaje =
                txtPorcentajeComision
                        .getText()
                        .trim();


        if (porcentaje.isEmpty()) {

            mostrarAdvertencia(
                    "Validación",
                    "Ingrese el porcentaje de comisión."
            );

            return false;
        }


        try {

            double valor =
                    Double.parseDouble(
                            porcentaje
                    );

            if (valor < 0 || valor > 100) {

                mostrarAdvertencia(
                        "Validación",
                        "El porcentaje de comisión debe estar entre 0 y 100."
                );

                return false;
            }

        } catch (NumberFormatException e) {

            mostrarAdvertencia(
                    "Validación",
                    "El porcentaje de comisión debe ser numérico."
            );

            return false;
        }


        return true;
    }


    // ==========================================================
    // CANCELAR
    // ==========================================================

    @FXML
    private void cancelar() {

        cerrar();
    }


    // ==========================================================
    // CERRAR
    // ==========================================================

    private void cerrar() {

        if (txtNombre.getScene() != null
                && txtNombre.getScene().getWindow() != null) {

            txtNombre.getScene()
                    .getWindow()
                    .hide();
        }
    }


    // ==========================================================
    // MENSAJES
    // ==========================================================

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
}