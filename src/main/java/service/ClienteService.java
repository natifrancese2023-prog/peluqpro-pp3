package service;

import claseslogicas.Cliente;
import dao.ClienteDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import claseslogicas.ClienteReporteExtendido;
import dao.ReporteDAO;

public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ReporteDAO reporteDAO = new ReporteDAO();
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern TELEFONO_REGEX = Pattern.compile("^[0-9]{7,15}$");
    private static final Pattern DOCUMENTO_REGEX = Pattern.compile("^[0-9]{6,12}$");
    private static final Pattern NOMBRE_REGEX =
            Pattern.compile("^[\\p{L}]+(?:[\\s'-][\\p{L}]+)*$");

    public String validarEmail(String email) {
        return (email != null && EMAIL_REGEX.matcher(email).matches())
                ? null
                : "Ingrese un email válido (ejemplo: usuario@dominio.com).";
    }

    public String validarTelefono(String telefono) {
        return (telefono != null && TELEFONO_REGEX.matcher(telefono).matches())
                ? null
                : "El teléfono debe contener solo dígitos y tener entre 7 y 15 caracteres.";
    }

    public String validarDocumento(String numeroDocumento) {
        return (numeroDocumento != null && DOCUMENTO_REGEX.matcher(numeroDocumento).matches())
                ? null
                : "El documento debe contener solo dígitos y tener entre 6 y 12 caracteres.";
    }


    public ResultadoAlta registrarCliente(Cliente cliente) throws SQLException {
        // 🔥 CAMBIO: Usamos 'consultarPorDocumentoGeneral' para que busque sin importar si está activo o inactivo
        Cliente existente = clienteDAO.consultarPorDocumentoCompletoGeneral(
                cliente.getNombreTipoDocumento(),
                cliente.getNumeroDocumento()
        );

        if (existente != null) {
            if (existente.isActivo()) {
                return ResultadoAlta.DUPLICADO;
            } else {
                return ResultadoAlta.DUPLICADO_INACTIVO; // Ahora sí va a retornar esto de forma correcta 🎉
            }
        }


        boolean insertado = clienteDAO.insertar(cliente);
        return insertado ? ResultadoAlta.OK : ResultadoAlta.ERROR_INSERCION;
    }


    public boolean reactivarYActualizarCliente(Cliente datosNuevos, int idClienteExistente) throws SQLException {

        boolean reactivado = clienteDAO.reactivarCliente(idClienteExistente);

        if (reactivado) {

            datosNuevos.setIdCliente(idClienteExistente);
            datosNuevos.setActivo(true);


            return clienteDAO.actualizar(datosNuevos);
        }
        return false;
    }

    public enum ResultadoAlta {
        OK,
        DUPLICADO,
        DUPLICADO_INACTIVO,
        ERROR_INSERCION
    }

    public boolean actualizarCliente(Cliente cliente) throws SQLException {
        return clienteDAO.actualizar(cliente);
    }

    public boolean eliminarCliente(Cliente cliente) throws SQLException {
        return clienteDAO.eliminar(cliente);
    }
    public boolean reactivarCliente(int idCliente) throws SQLException {
        return clienteDAO.reactivarCliente(idCliente);
    }

    public Cliente buscarPorDocumento(String tipoDoc, String nroDoc) throws SQLException {
        return clienteDAO.consultarPorDocumentoCompleto(tipoDoc, nroDoc);
    }

    public Cliente buscarPorDocumentoGeneral(String tipoDoc, String nroDoc) throws SQLException {
        return clienteDAO.consultarPorDocumentoCompletoGeneral(tipoDoc, nroDoc);
    }

    public Cliente obtenerPorId(int idCliente) throws SQLException {
        return clienteDAO.obtenerPorId(idCliente);
    }

    public java.util.List<Cliente> obtenerTodos() {
        return clienteDAO.obtenerTodos();
    }

    public int contarVisitasPorIdCliente(int idCliente) {
        return clienteDAO.contarVisitasPorIdCliente(idCliente);
    }

    public List<Cliente> obtenerPorEstado(boolean activo) throws SQLException {
        return clienteDAO.obtenerPorEstado(activo);
    }
    public List<ClienteReporteExtendido> obtenerDatosClientesExtendido() throws SQLException {
        return reporteDAO.obtenerDatosClientesExtendido();
    }



public boolean validarNombre(String nombre) {
    return nombre != null
            && !nombre.trim().isEmpty()
            && NOMBRE_REGEX.matcher(nombre.trim()).matches();
}}
