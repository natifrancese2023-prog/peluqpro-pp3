package support;

import dao.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Infraestructura común para todos los tests de HU.
 *
 * IMPORTANTE: usa dao.ConexionBD.getConnection() -- la MISMA clase de conexión
 * que usa la aplicación real -- así los tests ejercitan el código de
 * producción tal cual, contra la base de testing (peluqueria_test), nunca
 * contra la base real. Las variables de entorno PELUQPRO_DB_URL/_USER/_PASSWORD
 * deben apuntar a esa base de testing al correr los tests (ver
 * TESTING-REPORT.md / pom.xml, sección Runner de surefire).
 *
 * limpiarDatosTransaccionales() deja la base en el mismo estado conocido
 * (datos maestros de seed_test_data.sql intactos, todo lo transaccional
 * vacío) antes de cada test, para que cada test sea independiente y
 * reproducible sin importar el orden de ejecución.
 */
public final class TestDbSupport {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 1_000_000);

    private TestDbSupport() {}

    public static Connection conectar() throws SQLException {
        return ConexionBD.getConnection();
    }

    /** Reinicia todas las tablas transaccionales a un estado vacío y conocido. */
    public static void limpiarDatosTransaccionales() {
        try (Connection conn = conectar(); Statement st = conn.createStatement()) {

            st.execute("TRUNCATE TABLE detalle_factura, factura, detalle_servicio, turno_servicios, "
                    + "visita, turno RESTART IDENTITY CASCADE");

            // Eliminar usuarios asociados a empleados de test
            st.execute("""
    DELETE FROM usuarios
    WHERE id_empleado_fk IN (
        SELECT id_empleado
        FROM empleado
        WHERE id_persona >= 100
    )
    """);

            // Clientes/personas/documentos de test dinámicos (id >= 100).
            st.execute("DELETE FROM red_social WHERE id_cliente >= 100");
            st.execute("DELETE FROM cliente WHERE id_cliente >= 100");

            // Primero empleado, luego persona
            st.execute("DELETE FROM empleado WHERE id_persona >= 100");
            st.execute("DELETE FROM persona WHERE id_persona >= 100");
            st.execute("DELETE FROM documento WHERE id_documento >= 100");

        } catch (SQLException e) {
            throw new RuntimeException("No se pudo limpiar la base de testing. "
                    + "¿Está corriendo Postgres y existe peluqueria_test? " + e.getMessage(), e);
        }
    }
    /** Número de documento único por test, para no chocar entre corridas (siempre >= 900000000). */
    public static String documentoUnico() {
        return "9" + (900_000_000L + SEQ.incrementAndGet());
    }

    public static long siguienteId() {
        return SEQ.incrementAndGet();
    }
}
