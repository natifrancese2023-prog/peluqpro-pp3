package HU20;

import claseslogicas.ProductividadProfesional;
import dao.ProductividadProfesionalDAO;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HU20ProductividadProfesionalTest {

    private final ProductividadProfesionalDAO productividadDAO =
            new ProductividadProfesionalDAO();

    private final LocalDate desde =
            LocalDate.of(2026, 1, 1);

    private final LocalDate hasta =
            LocalDate.of(2026, 12, 31);


    /**
     * 1. Verifica que se pueda consultar la productividad
     * seleccionando un período.
     */
    @Test
    void consultarProductividadPorPeriodo()
            throws SQLException {

        List<ProductividadProfesional> resultados =
                productividadDAO.obtenerPorPeriodo(
                        desde,
                        hasta
                );

        assertNotNull(
                resultados,
                "La consulta de productividad no debe devolver null."
        );

        /*
         * La consulta debe devolver una fila por profesional
         * estilista, incluso cuando no tenga servicios.
         */
        assertFalse(
                resultados.isEmpty(),
                "Debe existir al menos un profesional para mostrar en el período consultado."
        );
    }


    /**
     * 2. Verifica que se muestre la cantidad
     * de servicios realizados por profesional.
     */
    @Test
    void mostrarCantidadServiciosPorProfesional()
            throws SQLException {

        List<ProductividadProfesional> resultados =
                productividadDAO.obtenerPorPeriodo(
                        desde,
                        hasta
                );

        assertFalse(
                resultados.isEmpty()
        );

        for (ProductividadProfesional resultado :
                resultados) {

            assertTrue(
                    resultado.getIdProfesional() > 0,
                    "Cada resultado debe identificar al profesional."
            );

            assertNotNull(
                    resultado.getNombreProfesional()
            );

            assertTrue(
                    resultado.getCantidadServicios() >= 0,
                    "La cantidad de servicios no puede ser negativa."
            );
        }

        /*
         * Verificamos que al menos un profesional tenga
         * servicios realizados en el período.
         */
        assertTrue(
                resultados.stream()
                        .anyMatch(
                                p ->
                                        p.getCantidadServicios() > 0
                        ),
                "Debe existir al menos un profesional con servicios realizados en el período."
        );
    }


    /**
     * 3. Verifica que se calcule el tiempo promedio
     * de duración de los servicios realizados.
     */
    @Test
    void mostrarTiempoPromedioDuracionServicios()
            throws SQLException {

        List<ProductividadProfesional> resultados =
                productividadDAO.obtenerPorPeriodo(
                        desde,
                        hasta
                );

        ProductividadProfesional profesional =
                resultados.stream()
                        .filter(
                                p ->
                                        p.getCantidadServicios() > 0
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "No existe un profesional con servicios en el período."
                                        )
                        );

        assertNotNull(
                profesional.getPromedioDuracionMinutos(),
                "El profesional con servicios debe tener promedio de duración."
        );

        assertTrue(
                profesional.getPromedioDuracionMinutos() > 0,
                "El promedio de duración debe ser mayor que cero."
        );
    }


    /**
     * 4. Verifica que la información esté diferenciada
     * por profesional.
     */
    @Test
    void resultadosSePresentanDiferenciadosPorProfesional()
            throws SQLException {

        List<ProductividadProfesional> resultados =
                productividadDAO.obtenerPorPeriodo(
                        desde,
                        hasta
                );

        assertFalse(
                resultados.isEmpty()
        );

        Map<Integer, ProductividadProfesional> porProfesional =
                new HashMap<>();

        for (ProductividadProfesional resultado :
                resultados) {

            ProductividadProfesional anterior =
                    porProfesional.put(
                            resultado.getIdProfesional(),
                            resultado
                    );

            assertNull(
                    anterior,
                    "No debe existir más de un resultado para el mismo profesional."
            );
        }

        assertEquals(
                resultados.size(),
                porProfesional.size(),
                "Cada profesional debe aparecer una sola vez."
        );
    }


    /**
     * 5. Verifica que los resultados correspondan
     * exclusivamente al período seleccionado.
     *
     * Se compara el resultado de la consulta con un cálculo
     * independiente realizado directamente sobre las visitas,
     * detalles de servicio y duración de los servicios.
     */
    @Test
    void resultadosCorrespondenAlPeriodoSeleccionado()
            throws SQLException {

        List<ProductividadProfesional> resultados =
                productividadDAO.obtenerPorPeriodo(
                        desde,
                        hasta
                );

        Map<Integer, DatosEsperados> esperados =
                obtenerDatosEsperadosDesdeBD(
                        desde,
                        hasta
                );

        for (ProductividadProfesional resultado :
                resultados) {

            DatosEsperados esperado =
                    esperados.get(
                            resultado.getIdProfesional()
                    );

            assertNotNull(
                    esperado,
                    "El profesional informado por el reporte debe corresponder a los datos del período."
            );

            assertEquals(
                    esperado.cantidadServicios(),
                    resultado.getCantidadServicios(),
                    "La cantidad de servicios no corresponde al período seleccionado."
            );

            if (esperado.cantidadServicios() > 0) {

                assertNotNull(
                        resultado.getPromedioDuracionMinutos()
                );

                assertEquals(
                        esperado.promedioDuracion(),
                        resultado.getPromedioDuracionMinutos(),
                        0.01,
                        "El promedio de duración no corresponde al período seleccionado."
                );

            } else {

                assertNull(
                        resultado.getPromedioDuracionMinutos(),
                        "Un profesional sin servicios en el período no debe tener promedio de duración."
                );
            }
        }
    }


    /**
     * Prueba complementaria negativa:
     * Hasta no puede ser anterior a Desde.
     */
    @Test
    void rechazarPeriodoConHastaAnteriorADesde() {

        LocalDate desdeInvalida =
                LocalDate.of(2026, 12, 31);

        LocalDate hastaInvalida =
                LocalDate.of(2026, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        productividadDAO.obtenerPorPeriodo(
                                desdeInvalida,
                                hastaInvalida
                        )
        );
    }


    /**
     * Obtiene los valores esperados directamente de la BD
     * para el período seleccionado.
     */
    private Map<Integer, DatosEsperados>
    obtenerDatosEsperadosDesdeBD(
            LocalDate desde,
            LocalDate hasta)
            throws SQLException {

        Map<Integer, DatosEsperados> resultado =
                new HashMap<>();

        String sql =
                "SELECT " +
                        "    e.id_empleado, " +
                        "    COUNT(ds.id_servicio) AS cantidad, " +
                        "    AVG(s.duracion_minutos) AS promedio " +
                        "FROM empleado e " +
                        "JOIN persona p " +
                        "    ON p.id_persona = e.id_persona " +
                        "JOIN roles r " +
                        "    ON r.id_rol = e.id_rol " +
                        "LEFT JOIN visita v " +
                        "    ON v.id_estilista = e.id_empleado " +
                        "    AND v.fecha_hora >= ? " +
                        "    AND v.fecha_hora < ? " +
                        "LEFT JOIN detalle_servicio ds " +
                        "    ON ds.id_visita = v.id_visita " +
                        "LEFT JOIN servicios s " +
                        "    ON s.id_servicio = ds.id_servicio " +
                        "WHERE r.es_estilista = true " +
                        "GROUP BY e.id_empleado " +
                        "ORDER BY e.id_empleado";

        try (
                Connection conn =
                        dao.ConexionBD.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setTimestamp(
                    1,
                    java.sql.Timestamp.valueOf(
                            desde.atStartOfDay()
                    )
            );

            ps.setTimestamp(
                    2,
                    java.sql.Timestamp.valueOf(
                            hasta.plusDays(1)
                                    .atStartOfDay()
                    )
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    long cantidad =
                            rs.getLong("cantidad");

                    java.math.BigDecimal promedioDecimal =
                            rs.getBigDecimal("promedio");

                    Double promedio =
                            promedioDecimal != null
                                    ? promedioDecimal.doubleValue()
                                    : null;

                    resultado.put(
                            rs.getInt("id_empleado"),
                            new DatosEsperados(
                                    cantidad,
                                    promedio
                            )
                    );
                }
            }
        }

        return resultado;
    }


    private record DatosEsperados(
            long cantidadServicios,
            Double promedioDuracion
    ) {
    }
}
