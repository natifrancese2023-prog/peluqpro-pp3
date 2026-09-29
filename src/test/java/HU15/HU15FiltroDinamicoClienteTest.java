package HU15;

import claseslogicas.ClienteReporteExtendido;
import controllers.ReporteClienteController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HU15FiltroDinamicoClienteTest {

    @BeforeAll
    static void iniciarJavaFX() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // JavaFX ya estaba iniciado.
            latch.countDown();
        }

        assertTrue(
                latch.await(5, TimeUnit.SECONDS),
                "No se pudo inicializar JavaFX."
        );
    }

    @Test
    void filtrosDisponiblesSonTodosFrecuentesYMayorGasto()
            throws Exception {

        ejecutarEnJavaFX(() -> {

            ReporteClienteController controller =
                    crearController();

            ComboBox<String> filtro =
                    obtenerCampo(controller, "cbFiltro");

            invocarMetodo(
                    controller,
                    "configurarFiltro"
            );

            assertEquals(
                    List.of("Todos", "Frecuentes", "Mayor gasto"),
                    filtro.getItems(),
                    "Deben estar disponibles los filtros Todos, Frecuentes y Mayor gasto."
            );
        });
    }

    @Test
    void filtroTodosMuestraTodosLosClientes()
            throws Exception {

        ejecutarEnJavaFX(() -> {

            ReporteClienteController controller =
                    crearController();

            List<ClienteReporteExtendido> datos =
                    datosDePrueba();

            setearCampo(
                    controller,
                    "datosClientes",
                    datos
            );

            ComboBox<String> filtro =
                    obtenerCampo(controller, "cbFiltro");

            filtro.getSelectionModel().select("Todos");

            invocarMetodo(
                    controller,
                    "actualizarVisualizacion"
            );

            TableView<ClienteReporteExtendido> tabla =
                    obtenerCampo(controller, "tablaClientes");

            assertEquals(
                    datos.size(),
                    tabla.getItems().size(),
                    "El filtro Todos debe mostrar todos los clientes."
            );
        });
    }

    @Test
    void filtroFrecuentesMuestraLosCincoClientesConMasVisitas()
            throws Exception {

        ejecutarEnJavaFX(() -> {

            ReporteClienteController controller =
                    crearController();

            setearCampo(
                    controller,
                    "datosClientes",
                    datosDePrueba()
            );

            ComboBox<String> filtro =
                    obtenerCampo(controller, "cbFiltro");

            filtro.getSelectionModel().select("Frecuentes");

            invocarMetodo(
                    controller,
                    "actualizarVisualizacion"
            );

            TableView<ClienteReporteExtendido> tabla =
                    obtenerCampo(controller, "tablaClientes");

            assertEquals(
                    5,
                    tabla.getItems().size(),
                    "El filtro Frecuentes debe mostrar como máximo cinco clientes."
            );

            for (int i = 0; i < tabla.getItems().size() - 1; i++) {

                int visitasActual =
                        tabla.getItems()
                                .get(i)
                                .getCantidadVisitas();

                int visitasSiguiente =
                        tabla.getItems()
                                .get(i + 1)
                                .getCantidadVisitas();

                assertTrue(
                        visitasActual >= visitasSiguiente,
                        "Los clientes deben quedar ordenados de mayor a menor cantidad de visitas."
                );
            }
        });
    }

    @Test
    void filtroMayorGastoMuestraLosCincoClientesConMayorGasto()
            throws Exception {

        ejecutarEnJavaFX(() -> {

            ReporteClienteController controller =
                    crearController();

            setearCampo(
                    controller,
                    "datosClientes",
                    datosDePrueba()
            );

            ComboBox<String> filtro =
                    obtenerCampo(controller, "cbFiltro");

            filtro.getSelectionModel().select("Mayor gasto");

            invocarMetodo(
                    controller,
                    "actualizarVisualizacion"
            );

            TableView<ClienteReporteExtendido> tabla =
                    obtenerCampo(controller, "tablaClientes");

            assertEquals(
                    5,
                    tabla.getItems().size(),
                    "El filtro Mayor gasto debe mostrar como máximo cinco clientes."
            );

            for (int i = 0; i < tabla.getItems().size() - 1; i++) {

                BigDecimal gastoActual =
                        tabla.getItems()
                                .get(i)
                                .getGastoTotal();

                BigDecimal gastoSiguiente =
                        tabla.getItems()
                                .get(i + 1)
                                .getGastoTotal();

                assertTrue(
                        gastoActual.compareTo(gastoSiguiente) >= 0,
                        "Los clientes deben quedar ordenados de mayor a menor gasto."
                );
            }
        });
    }

    @Test
    void filtroSeleccionadoPermaneceIdentificado()
            throws Exception {

        ejecutarEnJavaFX(() -> {

            ReporteClienteController controller =
                    crearController();

            setearCampo(
                    controller,
                    "datosClientes",
                    datosDePrueba()
            );

            ComboBox<String> filtro =
                    obtenerCampo(controller, "cbFiltro");

            filtro.getSelectionModel().select("Frecuentes");

            invocarMetodo(
                    controller,
                    "actualizarVisualizacion"
            );

            assertEquals(
                    "Frecuentes",
                    filtro.getValue(),
                    "El filtro seleccionado debe permanecer identificado después de actualizar el reporte."
            );
        });
    }

    private ReporteClienteController crearController() {

        ReporteClienteController controller =
                new ReporteClienteController();

        ComboBox<String> cbFiltro =
                new ComboBox<>();

        TableView<ClienteReporteExtendido> tablaClientes =
                new TableView<>();

        BarChart<String, Number> graficoBarras =
                new BarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                );

        PieChart graficoTorta =
                new PieChart();

        BarChart<String, Number> graficoHistograma =
                new BarChart<>(
                        new CategoryAxis(),
                        new NumberAxis()
                );

        setearCampo(
                controller,
                "cbFiltro",
                cbFiltro
        );

        setearCampo(
                controller,
                "tablaClientes",
                tablaClientes
        );

        setearCampo(
                controller,
                "graficoBarras",
                graficoBarras
        );

        setearCampo(
                controller,
                "graficoTorta",
                graficoTorta
        );

        setearCampo(
                controller,
                "graficoHistograma",
                graficoHistograma
        );

        return controller;
    }

    private List<ClienteReporteExtendido> datosDePrueba() {

        return List.of(

                cliente(
                        1,
                        "Cliente 1",
                        2,
                        "10000"
                ),

                cliente(
                        2,
                        "Cliente 2",
                        10,
                        "50000"
                ),

                cliente(
                        3,
                        "Cliente 3",
                        5,
                        "30000"
                ),

                cliente(
                        4,
                        "Cliente 4",
                        8,
                        "70000"
                ),

                cliente(
                        5,
                        "Cliente 5",
                        1,
                        "5000"
                ),

                cliente(
                        6,
                        "Cliente 6",
                        15,
                        "90000"
                ),

                cliente(
                        7,
                        "Cliente 7",
                        3,
                        "20000"
                )
        );
    }

    private ClienteReporteExtendido cliente(
            int id,
            String nombre,
            int visitas,
            String gasto) {

        return new ClienteReporteExtendido(
                id,
                id,
                nombre,
                "3510000000",
                "cliente" + id + "@test.com",
                "Calle Test 123",
                LocalDate.of(2026, 1, 1),
                visitas,
                new BigDecimal(gasto),
                "Finalizado",
                null
        );
    }

    private void ejecutarEnJavaFX(
            Runnable accion) throws Exception {

        CountDownLatch latch =
                new CountDownLatch(1);

        final Throwable[] error =
                new Throwable[1];

        Platform.runLater(() -> {

            try {
                accion.run();
            } catch (Throwable e) {
                error[0] = e;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(
                latch.await(10, TimeUnit.SECONDS),
                "La prueba JavaFX no finalizó."
        );

        if (error[0] != null) {
            if (error[0] instanceof Exception) {
                throw (Exception) error[0];
            }

            if (error[0] instanceof Error) {
                throw (Error) error[0];
            }

            throw new RuntimeException(error[0]);
        }
    }

    private static void setearCampo(
            Object objeto,
            String nombre,
            Object valor) {

        try {

            Field campo =
                    objeto.getClass()
                            .getDeclaredField(nombre);

            campo.setAccessible(true);
            campo.set(objeto, valor);

        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo acceder al campo: " + nombre,
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T obtenerCampo(
            Object objeto,
            String nombre) {

        try {

            Field campo =
                    objeto.getClass()
                            .getDeclaredField(nombre);

            campo.setAccessible(true);

            return (T) campo.get(objeto);

        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo obtener el campo: " + nombre,
                    e
            );
        }
    }

    private static void invocarMetodo(
            Object objeto,
            String nombre) {

        try {

            Method metodo =
                    objeto.getClass()
                            .getDeclaredMethod(nombre);

            metodo.setAccessible(true);
            metodo.invoke(objeto);

        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo ejecutar el método: " + nombre,
                    e
            );
        }
    }
}