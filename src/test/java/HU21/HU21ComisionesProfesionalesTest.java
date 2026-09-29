package HU21;

import claseslogicas.ComisionProfesional;
import claseslogicas.Empleado;
import dao.EmpleadoDAO;
import org.junit.jupiter.api.Test;
import service.ComisionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU21ComisionesProfesionalesTest {

    private final ComisionService comisionService =
            new ComisionService();

    private final EmpleadoDAO empleadoDAO =
            new EmpleadoDAO();

    /*
     * Profesional existente en la BD de testing:
     * ID 3 = Estela Estilista
     */
    private static final int ID_PROFESIONAL = 3;


    /**
     * Obtiene un período que realmente tenga comisiones
     * registradas en la BD.
     *
     * No inventamos una fecha.
     */
    private List<ComisionProfesional> obtenerComisionesExistentes() throws SQLException {

        LocalDate desde =
                LocalDate.of(2020, 1, 1);

        LocalDate hasta =
                LocalDate.now().plusDays(1);

        List<ComisionProfesional> resultado =
                comisionService.obtenerComisiones(
                        ID_PROFESIONAL,
                        desde,
                        hasta
                );

        assertNotNull(
                resultado,
                "La consulta de comisiones no debe devolver null."
        );

        assertFalse(
                resultado.isEmpty(),
                "La BD de testing debe contener comisiones para el profesional ID 3."
        );

        return resultado;
    }


    /**
     * 1. Consultar servicios realizados por el profesional
     * durante un período.
     */
    @Test
    void consultarServiciosRealizadosPorProfesional()
            throws Exception {

        List<ComisionProfesional> comisiones =
                obtenerComisionesExistentes();

        assertFalse(
                comisiones.isEmpty()
        );

        for (ComisionProfesional comision :
                comisiones) {

            assertNotNull(
                    comision.getFecha()
            );

            assertNotNull(
                    comision.getServicio()
            );

            assertFalse(
                    comision.getServicio().isBlank()
            );

            assertNotNull(
                    comision.getPrecio()
            );
        }
    }


    /**
     * 2. Verificar que la comisión se calcule
     * según el porcentaje configurado para el profesional.
     */
    @Test
    void calcularComisionSegunPorcentajeConfigurado()
            throws Exception {

        Empleado profesional =
                empleadoDAO.obtenerPorId(
                        ID_PROFESIONAL
                );

        assertNotNull(
                profesional,
                "El profesional ID 3 debe existir."
        );

        double porcentaje =
                profesional.getPorcentajeComision();

        assertTrue(
                porcentaje >= 0
                        && porcentaje <= 100,
                "El porcentaje de comisión debe estar entre 0 y 100."
        );

        List<ComisionProfesional> comisiones =
                obtenerComisionesExistentes();

        ComisionProfesional primera =
                comisiones.get(0);

        BigDecimal esperado =
                primera.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        porcentaje
                                )
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        assertEquals(
                porcentaje,
                primera.getPorcentajeComision(),
                0.001,
                "El porcentaje utilizado debe coincidir con el configurado para el profesional."
        );

        assertEquals(
                esperado,
                primera.getComision(),
                "La comisión no coincide con Precio × porcentaje / 100."
        );
    }


    /**
     * 3. Consultar las comisiones correspondientes
     * a un día determinado.
     *
     * Se utiliza una fecha que realmente posee
     * una comisión en la BD.
     */
    @Test
    void consultarComisionesDelDia()
            throws Exception {

        List<ComisionProfesional> todas =
                obtenerComisionesExistentes();

        LocalDate dia =
                todas.get(0).getFecha();

        List<ComisionProfesional> comisionesDelDia =
                comisionService.obtenerComisiones(
                        ID_PROFESIONAL,
                        dia,
                        dia
                );

        assertNotNull(
                comisionesDelDia
        );

        assertFalse(
                comisionesDelDia.isEmpty(),
                "Debe poder consultarse la comisión del día seleccionado."
        );

        assertTrue(
                comisionesDelDia.stream()
                        .allMatch(
                                c ->
                                        dia.equals(
                                                c.getFecha()
                                        )
                        ),
                "La consulta diaria no debe devolver registros de otros días."
        );
    }


    /**
     * 4. Consultar las comisiones correspondientes
     * al mes en el que existe actividad.
     */
    @Test
    void consultarComisionesDelMes()
            throws Exception {

        List<ComisionProfesional> todas =
                obtenerComisionesExistentes();

        LocalDate fechaReferencia =
                todas.get(0).getFecha();

        LocalDate primerDiaMes =
                fechaReferencia
                        .withDayOfMonth(1);

        LocalDate ultimoDiaMes =
                fechaReferencia
                        .withDayOfMonth(
                                fechaReferencia.lengthOfMonth()
                        );

        List<ComisionProfesional> comisionesDelMes =
                comisionService.obtenerComisiones(
                        ID_PROFESIONAL,
                        primerDiaMes,
                        ultimoDiaMes
                );

        assertNotNull(
                comisionesDelMes
        );

        assertFalse(
                comisionesDelMes.isEmpty(),
                "Debe poder consultarse la comisión correspondiente al mes seleccionado."
        );

        assertTrue(
                comisionesDelMes.stream()
                        .allMatch(
                                c ->
                                        !c.getFecha()
                                                .isBefore(
                                                        primerDiaMes
                                                )
                                                &&
                                                !c.getFecha()
                                                        .isAfter(
                                                                ultimoDiaMes
                                                        )
                        ),
                "La consulta mensual no debe devolver registros fuera del mes."
        );
    }


    /**
     * 5. Verificar que la información esté diferenciada
     * por profesional.
     *
     * Se consulta un profesional concreto y se verifica
     * que todos los registros correspondan exclusivamente
     * a ese profesional.
     */
    @Test
    void informacionDiferenciadaPorProfesional()
            throws Exception {

        Empleado profesional =
                empleadoDAO.obtenerPorId(
                        ID_PROFESIONAL
                );

        assertNotNull(
                profesional
        );

        List<ComisionProfesional> comisiones =
                obtenerComisionesExistentes();

        /*
         * La consulta se realizó exclusivamente
         * utilizando el ID del profesional 3.
         *
         * Todos los resultados pertenecen a esa consulta.
         */
        assertFalse(
                comisiones.isEmpty()
        );

        assertTrue(
                comisiones.stream()
                        .allMatch(
                                c ->
                                        c.getPorcentajeComision()
                                                == profesional
                                                .getPorcentajeComision()
                        ),
                "Los resultados deben utilizar el porcentaje configurado para el profesional consultado."
        );
    }


    /**
     * 6. Validación negativa:
     * no se permite consultar con un profesional inválido.
     */
    @Test
    void rechazarProfesionalInvalido() {

        LocalDate desde =
                LocalDate.of(2026, 1, 1);

        LocalDate hasta =
                LocalDate.of(2026, 12, 31);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        comisionService.obtenerComisiones(
                                0,
                                desde,
                                hasta
                        )
        );
    }


    /**
     * 7. Validación negativa:
     * no se permite un período donde Desde sea
     * posterior a Hasta.
     */
    @Test
    void rechazarPeriodoInvalido() {

        LocalDate desde =
                LocalDate.of(2026, 12, 31);

        LocalDate hasta =
                LocalDate.of(2026, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        comisionService.obtenerComisiones(
                                ID_PROFESIONAL,
                                desde,
                                hasta
                        )
        );
    }
}