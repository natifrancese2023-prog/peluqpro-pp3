package dao;

import claseslogicas.Empleado;
import claseslogicas.Persona;
import claseslogicas.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private final ConexionBD conexionBD = new ConexionBD();
    private final DocumentoDAO documentoDAO = new DocumentoDAO();
    private final PersonaDAO personaDAO = new PersonaDAO();


    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        Empleado emp = new Empleado();
        emp.setIdEmpleado(rs.getInt("id_empleado"));
        emp.setPorcentajeComision(rs.getDouble("porcentaje_comision"));
        emp.setActivo(rs.getBoolean("activo"));
        emp.setIdPersona(rs.getInt("id_persona"));

        Persona p = new Persona();

        p.setIdPersona(rs.getInt("id_persona"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setCalle(rs.getString("calle"));
        p.setNumero(rs.getString("numero"));
        p.setNumeroDocumento(rs.getString("numero_documento"));
        p.setNombreTipoDocumento(rs.getString("tipo_documento"));
        p.setNombreBarrio(rs.getString("nombre_barrio"));
        p.setNombreCiudad(rs.getString("nombre_ciudad"));
        p.setNombreProvincia(rs.getString("nombre_provincia"));


        emp.setPersona(p);


        Date sqlDate = rs.getDate("fecha_ingreso");
        if (sqlDate != null) {
            emp.setFechaIngreso(sqlDate.toLocalDate());
        }

        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombre(rs.getString("nombre_rol"));
        rol.setEsEstilista(rs.getBoolean("es_estilista"));
        emp.setRol(rol);

        return emp;
    }

    public List<Empleado> obtenerEstilistas() throws SQLException {

        String sql =
                "SELECT e.id_empleado, " +
                        "e.fecha_ingreso, " +
                        "e.porcentaje_comision, " +

                        "p.id_persona, " +
                        "p.nombre, " +
                        "p.apellido, " +
                        "p.telefono, " +
                        "p.email, " +
                        "p.calle, " +
                        "p.numero, " +
                        "p.activo, " +

                        "d.numero_documento, " +
                        "td.tipo_documento, " +

                        "b.nombre_barrio, " +
                        "ci.nombre_ciudad, " +
                        "pr.nombre_provincia, " +

                        "r.id_rol, " +
                        "r.nombre_rol, " +
                        "r.es_estilista " +

                        "FROM empleado e " +

                        "JOIN persona p " +
                        "ON e.id_persona = p.id_persona " +

                        "JOIN documento d " +
                        "ON p.id_documento = d.id_documento " +

                        "JOIN tipo_documento td " +
                        "ON d.id_tipo_documento = td.id_tipo_documento " +

                        "JOIN barrio b " +
                        "ON p.id_barrio = b.id_barrio " +

                        "JOIN ciudad ci " +
                        "ON b.id_ciudad = ci.id_ciudad " +

                        "JOIN provincia pr " +
                        "ON ci.id_provincia = pr.id_provincia " +

                        "JOIN roles r " +
                        "ON e.id_rol = r.id_rol " +

                        "WHERE r.es_estilista = true";

        List<Empleado> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpleado(rs));
            }
        }

        return lista;
    }
    public Empleado obtenerPorId(int idEmpleado) throws SQLException {

        String sql =
                "SELECT e.id_empleado, " +
                        "e.fecha_ingreso, " +
                        "e.porcentaje_comision, " +

                        "p.id_persona, " +
                        "p.nombre, " +
                        "p.apellido, " +
                        "p.telefono, " +
                        "p.email, " +
                        "p.calle, " +
                        "p.numero, " +
                        "p.activo, " +

                        "d.numero_documento, " +
                        "td.tipo_documento, " +

                        "b.nombre_barrio, " +
                        "ci.nombre_ciudad, " +
                        "pr.nombre_provincia, " +

                        "r.id_rol, " +
                        "r.nombre_rol, " +
                        "r.es_estilista " +

                        "FROM empleado e " +

                        "JOIN persona p " +
                        "ON e.id_persona = p.id_persona " +

                        "JOIN documento d " +
                        "ON p.id_documento = d.id_documento " +

                        "JOIN tipo_documento td " +
                        "ON d.id_tipo_documento = td.id_tipo_documento " +

                        "JOIN barrio b " +
                        "ON p.id_barrio = b.id_barrio " +

                        "JOIN ciudad ci " +
                        "ON b.id_ciudad = ci.id_ciudad " +

                        "JOIN provincia pr " +
                        "ON ci.id_provincia = pr.id_provincia " +

                        "JOIN roles r " +
                        "ON e.id_rol = r.id_rol " +

                        "WHERE e.id_empleado = ? " +
                        "AND r.es_estilista = true";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }
        }

        return null;
    }

    public List<String> obtenerTiposDocumento() throws SQLException {
        return documentoDAO.obtenerTiposDocumento();
    }
    public List<Empleado> obtenerEstilistasPorId(int idEstilista) throws SQLException {

        String sql =
                "SELECT e.id_empleado, " +
                        "e.fecha_ingreso, " +
                        "e.porcentaje_comision, " +

                        "p.id_persona, " +
                        "p.nombre, " +
                        "p.apellido, " +
                        "p.telefono, " +
                        "p.email, " +
                        "p.calle, " +
                        "p.numero, " +
                        "p.activo, " +

                        "d.numero_documento, " +
                        "td.tipo_documento, " +

                        "b.nombre_barrio, " +
                        "ci.nombre_ciudad, " +
                        "pr.nombre_provincia, " +

                        "r.id_rol, " +
                        "r.nombre_rol, " +
                        "r.es_estilista " +

                        "FROM empleado e " +

                        "JOIN persona p " +
                        "ON e.id_persona = p.id_persona " +

                        "JOIN documento d " +
                        "ON p.id_documento = d.id_documento " +

                        "JOIN tipo_documento td " +
                        "ON d.id_tipo_documento = td.id_tipo_documento " +

                        "JOIN barrio b " +
                        "ON p.id_barrio = b.id_barrio " +

                        "JOIN ciudad ci " +
                        "ON b.id_ciudad = ci.id_ciudad " +

                        "JOIN provincia pr " +
                        "ON ci.id_provincia = pr.id_provincia " +

                        "JOIN roles r " +
                        "ON e.id_rol = r.id_rol " +

                        "WHERE e.id_empleado = ? " +
                        "AND r.es_estilista = true";

        List<Empleado> lista = new ArrayList<>();

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstilista);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearEmpleado(rs));
                }
            }
        }

        return lista;
    }


    public Empleado obtenerPorIdPersona(int idPersona) {

        Empleado empleado = null;

        String sql = "SELECT id_empleado, id_persona FROM empleado WHERE id_persona = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    empleado = new Empleado();
                    empleado.setIdEmpleado(rs.getInt("id_empleado"));

                    Persona p = new Persona();
                    p.setIdPersona(rs.getInt("id_persona"));

                    empleado.setPersona(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }

        return empleado;
    }

    public boolean insertar(Empleado empleado,
                            String tipoDocumento,
                            String numeroDocumento,
                            String nombreBarrio) throws SQLException {

        Connection conn = null;

        if (empleado == null) {
            return false;
        }

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // PASO 1: Obtener tipo de documento
            int idTipoDocumento =
                    documentoDAO.obtenerIdTipoDocumento(conn, tipoDocumento);

            // PASO 2: Obtener barrio
            int idBarrio =
                    personaDAO.obtenerIdBarrio(conn, nombreBarrio);

            if (idTipoDocumento == -1 || idBarrio == -1) {
                conn.rollback();
                throw new SQLException(
                        "Error: No se encontró Tipo Documento o Barrio."
                );
            }

            // PASO 3: Validar duplicidad de documento
            String sqlDocumento =
                    "SELECT p.id_persona " +
                            "FROM persona p " +
                            "JOIN documento d ON p.id_documento = d.id_documento " +
                            "WHERE UPPER(d.numero_documento) = ? " +
                            "AND d.id_tipo_documento = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlDocumento)) {

                ps.setString(1, numeroDocumento.trim().toUpperCase());
                ps.setInt(2, idTipoDocumento);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        conn.rollback();
                        throw new SQLException(
                                "Error: Ya existe una persona con ese documento."
                        );
                    }
                }
            }

            // PASO 4: Insertar documento
            int idDocumento = documentoDAO.insertar(
                    conn,
                    numeroDocumento,
                    idTipoDocumento
            );

            if (idDocumento == -1) {
                conn.rollback();
                return false;
            }

            // PASO 5: Insertar persona
            int idPersona = -1;

            String sqlPersona =
                    "INSERT INTO persona " +
                            "(nombre, apellido, telefono, email, calle, numero, id_documento, id_barrio) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psPersona = conn.prepareStatement(
                    sqlPersona,
                    Statement.RETURN_GENERATED_KEYS)) {

                psPersona.setString(1, empleado.getNombre());
                psPersona.setString(2, empleado.getApellido());
                psPersona.setString(3, empleado.getTelefono());
                psPersona.setString(4, empleado.getEmail());
                psPersona.setString(5, empleado.getCalle());
                psPersona.setString(6, empleado.getNumero());
                psPersona.setInt(7, idDocumento);
                psPersona.setInt(8, idBarrio);

                if (psPersona.executeUpdate() > 0) {

                    try (ResultSet rs = psPersona.getGeneratedKeys()) {

                        if (rs.next()) {
                            idPersona = rs.getInt(1);
                        }
                    }

                } else {
                    conn.rollback();
                    return false;
                }
            }

            if (idPersona == -1) {
                conn.rollback();
                return false;
            }

            // PASO 6: Obtener el rol correspondiente a un estilista
            int idRolEstilista = -1;

            String sqlRol =
                    "SELECT id_rol " +
                            "FROM roles " +
                            "WHERE es_estilista = true " +
                            "ORDER BY id_rol " +
                            "LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sqlRol);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    idRolEstilista = rs.getInt("id_rol");
                }
            }

            if (idRolEstilista == -1) {
                conn.rollback();
                throw new SQLException(
                        "Error: No existe un rol configurado como estilista."
                );
            }

            // PASO 7: Insertar empleado
            String sqlEmpleado =
                    "INSERT INTO empleado " +
                            "(id_persona, id_rol, fecha_ingreso, porcentaje_comision) " +
                            "VALUES (?, ?, ?, ?)";

            int idEmpleado = -1;

            try (PreparedStatement ps = conn.prepareStatement(
                    sqlEmpleado,
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, idPersona);
                ps.setInt(2, idRolEstilista);

                if (empleado.getFechaIngreso() != null) {
                    ps.setDate(
                            3,
                            Date.valueOf(empleado.getFechaIngreso())
                    );
                } else {
                    ps.setDate(
                            3,
                            new Date(System.currentTimeMillis())
                    );
                }

                ps.setDouble(
                        4,
                        empleado.getPorcentajeComision()
                );

                if (ps.executeUpdate() > 0) {

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {
                            idEmpleado = rs.getInt(1);
                        }
                    }

                } else {
                    conn.rollback();
                    return false;
                }
            }

            if (idEmpleado == -1) {
                conn.rollback();
                return false;
            }

            // PASO 8: Actualizar objeto con los IDs generados
            empleado.setIdEmpleado(idEmpleado);
            empleado.setIdPersona(idPersona);
            empleado.setActivo(true);

            conn.commit();

            return true;

        } catch (SQLException e) {

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println(
                        "Rollback fallido: " + ex.getMessage()
                );
            }

            System.err.println(
                    "❌ ERROR DE TRANSACCIÓN: " +
                            "No se pudo insertar el profesional. Detalle: " +
                            e.getMessage()
            );

            throw e;

        } finally {

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean actualizar(Empleado empleado, String nombreBarrio)
            throws SQLException {

        Connection conn = null;

        if (empleado == null || empleado.getIdEmpleado() <= 0
                || empleado.getIdPersona() <= 0) {
            return false;
        }

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // PASO 1: Obtener barrio
            int idBarrio = personaDAO.obtenerIdBarrio(conn, nombreBarrio);

            if (idBarrio == -1) {
                conn.rollback();
                throw new SQLException(
                        "Error de Modificación: Barrio no encontrado."
                );
            }

            // PASO 2: Actualizar datos de Persona
            String sqlPersona =
                    "UPDATE persona SET " +
                            "nombre = ?, " +
                            "apellido = ?, " +
                            "telefono = ?, " +
                            "email = ?, " +
                            "calle = ?, " +
                            "numero = ?, " +
                            "id_barrio = ? " +
                            "WHERE id_persona = ?";
            Persona persona = empleado.getPersona();

            if (persona == null) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlPersona)) {

                ps.setString(1, persona.getNombre());
                ps.setString(2, persona.getApellido());
                ps.setString(3, persona.getTelefono());
                ps.setString(4, persona.getEmail());
                ps.setString(5, persona.getCalle());
                ps.setString(6, persona.getNumero());
                ps.setInt(7, idBarrio);
                ps.setInt(8, empleado.getIdPersona());

                ps.executeUpdate();
            }
            // PASO 3: Actualizar datos propios del empleado
            String sqlEmpleado =
                    "UPDATE empleado SET " +
                            "fecha_ingreso = ?, " +
                            "porcentaje_comision = ? " +
                            "WHERE id_empleado = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlEmpleado)) {

                if (empleado.getFechaIngreso() != null) {
                    ps.setDate(
                            1,
                            Date.valueOf(empleado.getFechaIngreso())
                    );
                } else {
                    ps.setDate(1, null);
                }

                ps.setDouble(
                        2,
                        empleado.getPorcentajeComision()
                );

                ps.setInt(
                        3,
                        empleado.getIdEmpleado()
                );

                ps.executeUpdate();
            }

            conn.commit();

            return true;

        } catch (SQLException e) {

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println(
                        "Rollback fallido: " + ex.getMessage()
                );
            }

            System.err.println(
                    "❌ ERROR DE TRANSACCIÓN: " +
                            "No se pudo actualizar el profesional. Detalle: " +
                            e.getMessage()
            );

            throw e;

        } finally {

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean actualizarEstado(int idEmpleado, boolean activo)
            throws SQLException {

        if (idEmpleado <= 0) {
            return false;
        }

        Connection conn = null;

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            // Obtener la persona asociada al empleado
            int idPersona = -1;

            String sqlPersonaId =
                    "SELECT id_persona " +
                            "FROM empleado " +
                            "WHERE id_empleado = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlPersonaId)) {

                ps.setInt(1, idEmpleado);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        idPersona = rs.getInt("id_persona");
                    }
                }
            }

            if (idPersona == -1) {
                conn.rollback();
                return false;
            }

            // Actualizar estado de la persona
            String sqlPersona =
                    "UPDATE persona " +
                            "SET activo = ? " +
                            "WHERE id_persona = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlPersona)) {

                ps.setBoolean(1, activo);
                ps.setInt(2, idPersona);

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();

            return true;

        } catch (SQLException e) {

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println(
                        "Rollback fallido: " + ex.getMessage()
                );
            }

            System.err.println(
                    "❌ ERROR DE TRANSACCIÓN: " +
                            "No se pudo actualizar el estado del profesional. Detalle: " +
                            e.getMessage()
            );

            throw e;

        } finally {

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public Empleado consultarPorDocumentoCompleto(
            String tipoDocumento,
            String numeroDocumento) throws SQLException {

        if (tipoDocumento == null
                || numeroDocumento == null
                || tipoDocumento.isEmpty()
                || numeroDocumento.isEmpty()) {
            return null;
        }

        String sql =
                "SELECT e.id_empleado, " +
                        "e.fecha_ingreso, " +
                        "e.porcentaje_comision, " +

                        "p.id_persona, " +
                        "p.nombre, " +
                        "p.apellido, " +
                        "p.telefono, " +
                        "p.email, " +
                        "p.calle, " +
                        "p.numero, " +
                        "p.activo, " +

                        "d.numero_documento, " +
                        "td.tipo_documento, " +

                        "b.nombre_barrio, " +
                        "ci.nombre_ciudad, " +
                        "pr.nombre_provincia, " +

                        "r.id_rol, " +
                        "r.nombre_rol, " +
                        "r.es_estilista " +

                        "FROM empleado e " +

                        "JOIN persona p " +
                        "ON e.id_persona = p.id_persona " +

                        "JOIN documento d " +
                        "ON p.id_documento = d.id_documento " +

                        "JOIN tipo_documento td " +
                        "ON d.id_tipo_documento = td.id_tipo_documento " +

                        "JOIN barrio b " +
                        "ON p.id_barrio = b.id_barrio " +

                        "JOIN ciudad ci " +
                        "ON b.id_ciudad = ci.id_ciudad " +

                        "JOIN provincia pr " +
                        "ON ci.id_provincia = pr.id_provincia " +

                        "JOIN roles r " +
                        "ON e.id_rol = r.id_rol " +

                        "WHERE UPPER(td.tipo_documento) = ? " +
                        "AND d.numero_documento = ? " +
                        "AND r.es_estilista = true";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoDocumento.trim().toUpperCase());
            ps.setString(2, numeroDocumento.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "❌ Error al consultar Profesional por Documento: "
                            + e.getMessage()
            );

            throw e;
        }

        return null;
    }

}