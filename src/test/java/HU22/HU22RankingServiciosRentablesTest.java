package HU22;

import claseslogicas.RankingServicio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import service.RankingServicioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HU22RankingServiciosRentablesTest {

    private static RankingServicioService rankingService;

    /*
     * Fecha exclusiva utilizada para los datos de prueba de HU22.
     * Debe coincidir con la fecha utilizada en el script SQL.
     */
    private static final LocalDate FECHA_PRUEBA =
            LocalDate.of(2099, 1, 15);

    @BeforeAll
    static void iniciar() {
        rankingService = new RankingServicioService();
    }

    // =========================================================
    // 1. SE PUEDEN CONSULTAR LOS SERVICIOS REALIZADOS
    // =========================================================

    @Test
    @Order(1)
    void consultarServiciosRealizados() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        assertNotNull(
                ranking,
                "El ranking no debe ser null."
        );

        assertFalse(
                ranking.isEmpty(),
                "Debe existir información de servicios realizados."
        );

        assertTrue(
                ranking.stream()
                        .allMatch(r -> r.getCantidadRealizada() > 0),
                "Cada servicio del ranking debe tener una cantidad realizada mayor a cero."
        );
    }

    // =========================================================
    // 2. SE MUESTRA EL PRECIO FINAL DE CADA SERVICIO
    // =========================================================

    @Test
    @Order(2)
    void mostrarPrecioFinalDeCadaServicio() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        RankingServicio corte =
                buscarServicio(ranking, 1);

        RankingServicio coloracion =
                buscarServicio(ranking, 2);

        RankingServicio peinado =
                buscarServicio(ranking, 3);

        assertNotNull(corte, "No se encontró Corte de pelo.");
        assertNotNull(coloracion, "No se encontró Coloración.");
        assertNotNull(peinado, "No se encontró Peinado.");

        assertBigDecimalEquals(
                new BigDecimal("8000"),
                corte.getIngresos(),
                "El precio de Corte de pelo debe ser $8000."
        );

        assertBigDecimalEquals(
                new BigDecimal("25000"),
                coloracion.getIngresos(),
                "El precio de Coloración debe ser $25000."
        );

        assertBigDecimalEquals(
                new BigDecimal("6000"),
                peinado.getIngresos(),
                "El precio de Peinado debe ser $6000."
        );
    }

    // =========================================================
    // 3. CADA SERVICIO TIENE UN COSTO ESTIMADO
    // =========================================================

    @Test
    @Order(3)
    void considerarCostoEstimadoDelServicio() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        RankingServicio corte =
                buscarServicio(ranking, 1);

        RankingServicio coloracion =
                buscarServicio(ranking, 2);

        RankingServicio peinado =
                buscarServicio(ranking, 3);

        assertNotNull(corte);
        assertNotNull(coloracion);
        assertNotNull(peinado);

        assertBigDecimalEquals(
                new BigDecimal("2000"),
                corte.getCostoEstimado(),
                "El costo estimado de Corte de pelo debe ser $2000."
        );

        assertBigDecimalEquals(
                new BigDecimal("7000"),
                coloracion.getCostoEstimado(),
                "El costo estimado de Coloración debe ser $7000."
        );

        assertBigDecimalEquals(
                new BigDecimal("1000"),
                peinado.getCostoEstimado(),
                "El costo estimado de Peinado debe ser $1000."
        );
    }

    // =========================================================
    // 4. SE CONSIDERA LA COMISIÓN DEL PROFESIONAL
    // =========================================================

    @Test
    @Order(4)
    void considerarComisionDelProfesional() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        RankingServicio corte =
                buscarServicio(ranking, 1);

        RankingServicio coloracion =
                buscarServicio(ranking, 2);

        RankingServicio peinado =
                buscarServicio(ranking, 3);

        assertNotNull(corte);
        assertNotNull(coloracion);
        assertNotNull(peinado);

        /*
         * Estela tiene 20% de comisión.
         *
         * Corte:
         * 8000 x 20% = 1600
         *
         * Coloración:
         * 25000 x 20% = 5000
         *
         * Peinado:
         * 6000 x 20% = 1200
         */

        assertBigDecimalEquals(
                new BigDecimal("1600"),
                corte.getComisiones(),
                "La comisión de Corte de pelo debe ser $1600."
        );

        assertBigDecimalEquals(
                new BigDecimal("5000"),
                coloracion.getComisiones(),
                "La comisión de Coloración debe ser $5000."
        );

        assertBigDecimalEquals(
                new BigDecimal("1200"),
                peinado.getComisiones(),
                "La comisión de Peinado debe ser $1200."
        );
    }

    // =========================================================
    // 5. SE CALCULA EL MARGEN ESTIMADO
    // =========================================================

    @Test
    @Order(5)
    void calcularMargenEstimadoDelServicio() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        RankingServicio corte =
                buscarServicio(ranking, 1);

        RankingServicio coloracion =
                buscarServicio(ranking, 2);

        RankingServicio peinado =
                buscarServicio(ranking, 3);

        assertNotNull(corte);
        assertNotNull(coloracion);
        assertNotNull(peinado);

        /*
         * Fórmula:
         *
         * Margen = Precio - Costo - Comisión
         *
         * Corte:
         * 8000 - 2000 - 1600 = 4400
         *
         * Coloración:
         * 25000 - 7000 - 5000 = 13000
         *
         * Peinado:
         * 6000 - 1000 - 1200 = 3800
         */

        assertBigDecimalEquals(
                new BigDecimal("4400"),
                corte.getMargenEstimado(),
                "El margen de Corte de pelo debe ser $4400."
        );

        assertBigDecimalEquals(
                new BigDecimal("13000"),
                coloracion.getMargenEstimado(),
                "El margen de Coloración debe ser $13000."
        );

        assertBigDecimalEquals(
                new BigDecimal("3800"),
                peinado.getMargenEstimado(),
                "El margen de Peinado debe ser $3800."
        );
    }

    // =========================================================
    // 6. LOS SERVICIOS SE ORDENAN POR MARGEN
    // =========================================================

    @Test
    @Order(6)
    void ordenarServiciosPorMargenEstimado() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        assertTrue(
                ranking.size() >= 3,
                "El ranking debe contener los tres servicios de prueba."
        );

        /*
         * El DAO ordena:
         *
         * mayor margen -> menor margen
         */

        for (int i = 0; i < ranking.size() - 1; i++) {

            BigDecimal margenActual =
                    ranking.get(i).getMargenEstimado();

            BigDecimal margenSiguiente =
                    ranking.get(i + 1).getMargenEstimado();

            assertTrue(
                    margenActual.compareTo(margenSiguiente) >= 0,
                    "El ranking debe estar ordenado de mayor a menor margen."
            );
        }
    }

    // =========================================================
    // 7. SE IDENTIFICA EL SERVICIO CON MAYOR MARGEN
    // =========================================================

    @Test
    @Order(7)
    void identificarServicioConMayorMargen() throws Exception {

        List<RankingServicio> ranking =
                rankingService.obtenerRanking(
                        FECHA_PRUEBA,
                        FECHA_PRUEBA
                );

        assertFalse(
                ranking.isEmpty(),
                "El ranking no debe estar vacío."
        );

        RankingServicio mayorMargen =
                ranking.get(0);

        assertEquals(
                2,
                mayorMargen.getIdServicio()
        );

        assertEquals(
                "Coloración",
                mayorMargen.getServicio()
        );

        assertBigDecimalEquals(
                new BigDecimal("13000"),
                mayorMargen.getMargenEstimado(),
                "Coloración debe tener el mayor margen estimado."
        );

        assertEquals(
                1,
                mayorMargen.getPosicion()
        );
    }

    // =========================================================
    // 8. VALIDAR PERÍODO INVÁLIDO
    // =========================================================

    @Test
    @Order(8)
    void rechazarPeriodoInvalido() {

        LocalDate desde =
                LocalDate.of(2099, 1, 16);

        LocalDate hasta =
                LocalDate.of(2099, 1, 15);

        assertThrows(
                IllegalArgumentException.class,
                () -> rankingService.obtenerRanking(
                        desde,
                        hasta
                )
        );
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private RankingServicio buscarServicio(
            List<RankingServicio> ranking,
            int idServicio) {

        return ranking.stream()
                .filter(r -> r.getIdServicio() == idServicio)
                .findFirst()
                .orElse(null);
    }

    /**
     * Compara BigDecimal por valor numérico y no por escala.
     *
     * Ejemplo:
     * 8000.00 y 8000 son numéricamente iguales.
     */
    private void assertBigDecimalEquals(
            BigDecimal esperado,
            BigDecimal actual,
            String mensaje) {

        assertNotNull(
                actual,
                mensaje + " El valor obtenido es null."
        );

        assertEquals(
                0,
                esperado.compareTo(actual),
                mensaje
        );
    }
}
