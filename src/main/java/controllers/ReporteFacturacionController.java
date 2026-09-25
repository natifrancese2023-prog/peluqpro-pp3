package controllers;

import dao.ReporteFacturacionDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import claseslogicas.FacturaResumen;
import claseslogicas.ExportadorExcel;
import claseslogicas.ExportadorPDF;
import utilidades.AlertaUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

public class ReporteFacturacionController {

    @FXML private DatePicker fechaInicio;
    @FXML private DatePicker fechaFin;

    @FXML private LineChart<String, Number> graficoFacturacion;
    @FXML private StackedBarChart<String, Number> graficoMetodosPago;
    @FXML private PieChart graficoTortaMetodos;

    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;

    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblTotalFacturas;
    @FXML private Label lblTotalPagadas;

    @FXML private TableView<FacturaResumenModificada> tablaResumen;
    @FXML private TableColumn<FacturaResumenModificada, String> colFecha;
    @FXML private TableColumn<FacturaResumenModificada, String> colTotal;
    @FXML private TableColumn<FacturaResumenModificada, String> colCantidad;
    @FXML private TableColumn<FacturaResumenModificada, String> colPagadas;

    @FXML private TableColumn<FacturaResumenModificada, String> colEfectivo;
    @FXML private TableColumn<FacturaResumenModificada, String> colTransferencia;
    @FXML private TableColumn<FacturaResumenModificada, String> colDebito;

    private final ReporteFacturacionDAO dao = new ReporteFacturacionDAO();

    private final List<FacturaResumen> resumenParaExportar = new ArrayList<>();

    @FXML
    public void initialize() {



        graficoFacturacion.setTitle("Evolución diaria de facturación ($)");
        graficoMetodosPago.setTitle("Cantidad de facturas por día");
        graficoTortaMetodos.setTitle("Distribución de métodos de pago - Facturas pagadas");

        graficoFacturacion.setAnimated(false);
        graficoMetodosPago.setAnimated(false);


        graficoFacturacion.setLegendVisible(false);


        graficoMetodosPago.setLegendVisible(true);
        graficoMetodosPago.setLegendSide(Side.BOTTOM);


        graficoTortaMetodos.setLegendVisible(true);
        graficoTortaMetodos.setLegendSide(Side.RIGHT);

        graficoFacturacion.setCreateSymbols(true);

        graficoFacturacion.setVisible(false);
        graficoMetodosPago.setVisible(false);
        graficoTortaMetodos.setVisible(false);
        tablaResumen.setVisible(false);



        colFecha.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getFecha().toString()
                )
        );

        colTotal.setCellValueFactory(data -> {
            BigDecimal total = data.getValue().getTotalFacturado();

            return new SimpleStringProperty(
                    NumberFormat.getCurrencyInstance().format(total)
            );
        });

        colCantidad.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().getCantidadFacturas())
                )
        );

        colPagadas.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().getCantidadPagadas())
                )
        );

        colEfectivo.setCellValueFactory(
                data -> new SimpleStringProperty(
                        formatearMoneda(data.getValue().getEfectivo())
                )
        );

        colTransferencia.setCellValueFactory(
                data -> new SimpleStringProperty(
                        formatearMoneda(data.getValue().getTransferencia())
                )
        );

        colDebito.setCellValueFactory(
                data -> new SimpleStringProperty(
                        formatearMoneda(data.getValue().getDebito())
                )
        );
    }
    private String formatearMoneda(BigDecimal valor) {

        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return NumberFormat
                .getCurrencyInstance()
                .format(valor);
    }

    @FXML
    public void generarReporte() {

        LocalDate inicio = fechaInicio.getValue();
        LocalDate fin = fechaFin.getValue();

        if (inicio == null || fin == null) {
            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Fechas inválidas",
                    null,
                    "Debés seleccionar ambas fechas."
            );
            return;
        }

        if (fin.isBefore(inicio)) {
            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Rango inválido",
                    null,
                    "La fecha fin no puede ser anterior a la fecha inicio."
            );
            return;
        }

        Map<LocalDate, BigDecimal> facturacion =
                dao.obtenerFacturacionPorDia(inicio, fin);

        Map<LocalDate, List<String>> datosPorDia =
                dao.obtenerMetodosPorFacturaPorDia(inicio, fin);

        Map<LocalDate, Map<String, BigDecimal>> importesPorMetodo =
                dao.obtenerImportesPorMetodoPagoPorDia(inicio, fin);

        cargarGraficoFacturacion(facturacion);

        cargarDatosReporte(
                facturacion,
                datosPorDia,
                importesPorMetodo
        );

        graficoFacturacion.setVisible(true);
        graficoMetodosPago.setVisible(true);
        graficoTortaMetodos.setVisible(true);
        tablaResumen.setVisible(true);
    }


    private void cargarGraficoFacturacion(
            Map<LocalDate, BigDecimal> datos) {

        graficoFacturacion.getData().clear();

        XYChart.Series<String, Number> serie =
                new XYChart.Series<>();

        serie.setName("Total facturado ($)");

        datos.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {

                    BigDecimal valor =
                            entry.getValue() != null
                                    ? entry.getValue()
                                    .setScale(2, RoundingMode.HALF_UP)
                                    : BigDecimal.ZERO;

                    serie.getData().add(
                            new XYChart.Data<>(
                                    entry.getKey().toString(),
                                    valor.doubleValue()
                            )
                    );
                });

        graficoFacturacion.getData().add(serie);
    }



    private void cargarDatosReporte(
            Map<LocalDate, BigDecimal> facturacion,
            Map<LocalDate, List<String>> datosPorDia,
            Map<LocalDate, Map<String, BigDecimal>> importesPorMetodo) {

        graficoMetodosPago.getData().clear();
        tablaResumen.getItems().clear();
        resumenParaExportar.clear();
        graficoTortaMetodos.getData().clear();

        /*
         * LinkedHashMap para mantener el orden de las series.
         *
         * IMPORTANTE:
         * Las fechas también se procesan siempre mediante la lista
         * fechasOrdenadas.
         */
        Map<String, Map<LocalDate, Integer>> conteoPorCategoria =
                new LinkedHashMap<>();

        /*
         * Las categorías se mantienen en un orden fijo.
         */
        conteoPorCategoria.put(
                "Facturada",
                new LinkedHashMap<>()
        );

        conteoPorCategoria.put(
                "Anulada",
                new LinkedHashMap<>()
        );

        conteoPorCategoria.put(
                "Efectivo",
                new LinkedHashMap<>()
        );

        conteoPorCategoria.put(
                "Transferencia",
                new LinkedHashMap<>()
        );

        conteoPorCategoria.put(
                "Débito",
                new LinkedHashMap<>()
        );

        /*
         * Métodos de pago reales de las facturas pagadas.
         */
        Map<String, Integer> acumuladoMetodosPago =
                new LinkedHashMap<>();

        acumuladoMetodosPago.put("Efectivo", 0);
        acumuladoMetodosPago.put("Transferencia", 0);
        acumuladoMetodosPago.put("Débito", 0);

        BigDecimal totalMontoPeriodo = BigDecimal.ZERO;
        int totalFacturasPeriodo = 0;
        int totalPagadasPeriodo = 0;

        /*
         * IMPORTANTE:
         * Las fechas se ordenan explícitamente.
         */
        List<LocalDate> fechasOrdenadas =
                new ArrayList<>(facturacion.keySet());

        Collections.sort(fechasOrdenadas);

        for (LocalDate fecha : fechasOrdenadas) {

            BigDecimal montoDia =
                    facturacion.getOrDefault(
                            fecha,
                            BigDecimal.ZERO
                    );
            Map<String, BigDecimal> importesDia =
                    importesPorMetodo.getOrDefault(
                            fecha,
                            Collections.emptyMap()
                    );

            BigDecimal efectivo =
                    importesDia.getOrDefault(
                            "Efectivo",
                            BigDecimal.ZERO
                    );

            BigDecimal transferencia =
                    importesDia.getOrDefault(
                            "Transferencia",
                            BigDecimal.ZERO
                    );

            BigDecimal debito =
                    importesDia.getOrDefault(
                            "Débito",
                            BigDecimal.ZERO
                    );

            totalMontoPeriodo =
                    totalMontoPeriodo.add(montoDia);

            List<String> registros =
                    datosPorDia.getOrDefault(
                            fecha,
                            Collections.emptyList()
                    );

            int cantidadFacturasDia = registros.size();
            int cantidadPagadasDia = 0;

            /*
             * Para la tabla del día.
             */
            Map<String, Integer> conteoDia =
                    new LinkedHashMap<>();

            /*
             * Procesamos cada registro recibido del DAO.
             */
            for (String registro : registros) {

                if (registro == null ||
                        registro.trim().isEmpty()) {
                    continue;
                }

                String valor =
                        registro.trim();

                String normalizado =
                        valor.toLowerCase(Locale.ROOT);

                String categoria;

                boolean esPagada = false;



                if (normalizado.contains("anulada")) {

                    categoria = "Anulada";

                } else if (normalizado.contains("facturada")) {

                    categoria = "Facturada";



                } else if (normalizado.contains("efectivo")) {

                    categoria = "Efectivo";
                    esPagada = true;

                } else if (normalizado.contains("transferencia")) {

                    categoria = "Transferencia";
                    esPagada = true;

                } else if (
                        normalizado.contains("debito") ||
                                normalizado.contains("débito") ||
                                normalizado.contains("credito") ||
                                normalizado.contains("crédito") ||
                                normalizado.contains("tarjeta")) {

                    categoria = "Débito";
                    esPagada = true;

                } else {

                    /*
                     * Si el DAO devuelve simplemente "Pagada",
                     * la consideramos pagada pero NO inventamos
                     * un método de pago.
                     *
                     * Para el gráfico de barras la representamos
                     * como "Pagada".
                     */
                    categoria = "Pagada";
                    esPagada = true;

                    if (!conteoPorCategoria.containsKey("Pagada")) {
                        conteoPorCategoria.put(
                                "Pagada",
                                new LinkedHashMap<>()
                        );
                    }
                }



                conteoDia.put(
                        categoria,
                        conteoDia.getOrDefault(categoria, 0) + 1
                );



                Map<LocalDate, Integer> serie =
                        conteoPorCategoria.get(categoria);

                if (serie == null) {

                    serie = new LinkedHashMap<>();

                    conteoPorCategoria.put(
                            categoria,
                            serie
                    );
                }

                serie.put(
                        fecha,
                        serie.getOrDefault(fecha, 0) + 1
                );



                if (esPagada) {

                    cantidadPagadasDia++;

                    /*
                     * Solo acumulamos métodos de pago reales.
                     */
                    if (categoria.equals("Efectivo") ||
                            categoria.equals("Transferencia") ||
                            categoria.equals("Débito")) {

                        acumuladoMetodosPago.put(
                                categoria,
                                acumuladoMetodosPago.getOrDefault(
                                        categoria,
                                        0
                                ) + 1
                        );
                    }
                }
            }

            totalFacturasPeriodo += cantidadFacturasDia;
            totalPagadasPeriodo += cantidadPagadasDia;



            StringBuilder resumen =
                    new StringBuilder();

            conteoDia.forEach((categoria, cantidad) -> {

                if (resumen.length() > 0) {
                    resumen.append("  ");
                }

                resumen.append(categoria)
                        .append(" (")
                        .append(cantidad)
                        .append(")");
            });


            tablaResumen.getItems().add(
                    new FacturaResumenModificada(
                            fecha,
                            montoDia,
                            cantidadFacturasDia,
                            cantidadPagadasDia,
                            efectivo,
                            transferencia,
                            debito
                    )
            );



            resumenParaExportar.add(
                    new FacturaResumen(
                            fecha,
                            montoDia,
                            efectivo,
                            transferencia,
                            debito,
                            cantidadFacturasDia
                    )
            );
        }


        for (Map.Entry<String, Map<LocalDate, Integer>> entrada
                : conteoPorCategoria.entrySet()) {

            String categoria = entrada.getKey();

            Map<LocalDate, Integer> valores =
                    entrada.getValue();

            if (valores.isEmpty()) {
                continue;
            }

            XYChart.Series<String, Number> serie =
                    new XYChart.Series<>();

            serie.setName(categoria);

            /*
             * MUY IMPORTANTE:
             * usamos fechasOrdenadas y NO map.forEach()
             * para garantizar orden cronológico.
             */
            for (LocalDate fecha : fechasOrdenadas) {

                int cantidad =
                        valores.getOrDefault(
                                fecha,
                                0
                        );

                if (cantidad > 0) {

                    serie.getData().add(
                            new XYChart.Data<>(
                                    fecha.toString(),
                                    cantidad
                            )
                    );
                }
            }

            if (!serie.getData().isEmpty()) {
                graficoMetodosPago.getData().add(serie);
            }
        }


        for (Map.Entry<String, Integer> entrada
                : acumuladoMetodosPago.entrySet()) {

            if (entrada.getValue() > 0) {

                graficoTortaMetodos.getData().add(
                        new PieChart.Data(
                                entrada.getKey()
                                        + " ("
                                        + entrada.getValue()
                                        + ")",
                                entrada.getValue()
                        )
                );
            }
        }


        lblTotalPeriodo.setText(
                "Total Facturado Período: "
                        + NumberFormat
                        .getCurrencyInstance()
                        .format(totalMontoPeriodo)
        );

        lblTotalFacturas.setText(
                "Total Facturas: "
                        + totalFacturasPeriodo
        );

        lblTotalPagadas.setText(
                "Total Pagadas: "
                        + totalPagadasPeriodo
        );
    }

    @FXML
    private void exportarReporte(ActionEvent event) {

        Window ventana =
                btnExportar.getScene().getWindow();

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Guardar Reporte como Imagen"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imagen PNG (*.png)",
                        "*.png"
                )
        );

        fileChooser.setInitialFileName(
                "Reporte_Facturacion_"
                        + LocalDate.now()
                        + ".png"
        );

        File archivoDestino =
                fileChooser.showSaveDialog(ventana);

        if (archivoDestino == null) {
            return;
        }

        try {

            VBox contenedorGraficos =
                    (VBox) graficoFacturacion.getParent();

            WritableImage snapshot =
                    contenedorGraficos.snapshot(
                            new SnapshotParameters(),
                            null
                    );

            BufferedImage bufferedImage =
                    SwingFXUtils.fromFXImage(
                            snapshot,
                            null
                    );

            ImageIO.write(
                    bufferedImage,
                    "png",
                    archivoDestino
            );

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Exportación Exitosa",
                    null,
                    "El reporte gráfico se ha guardado correctamente en:\n"
                            + archivoDestino.getAbsolutePath()
            );

        } catch (java.io.IOException e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al exportar",
                    "No se pudo guardar la imagen",
                    "Ocurrió un error interno: "
                            + e.getMessage()
            );
        }
    }


    @FXML
    private void exportarReportePDF(ActionEvent event) {

        if (resumenParaExportar.isEmpty()) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Sin datos",
                    null,
                    "Generá el reporte primero."
            );

            return;
        }

        Window ventana =
                btnExportar.getScene().getWindow();

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Guardar reporte de facturación (PDF)"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivo PDF",
                        "*.pdf"
                )
        );

        fileChooser.setInitialFileName(
                "Reporte_Facturacion_"
                        + LocalDate.now()
                        + ".pdf"
        );

        File destino =
                fileChooser.showSaveDialog(ventana);

        if (destino == null) {
            return;
        }

        try {

            new ExportadorPDF()
                    .exportarFacturasReporte(
                            FXCollections.observableArrayList(
                                    resumenParaExportar
                            ),
                            destino
                    );

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Exportación exitosa",
                    null,
                    "El reporte de facturación fue exportado correctamente a PDF."
            );

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de exportación",
                    null,
                    "No se pudo generar el archivo PDF."
            );
        }
    }

    @FXML
    private void exportarReporteExcel(ActionEvent event) {

        if (resumenParaExportar.isEmpty()) {

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Sin datos",
                    null,
                    "Generá el reporte primero."
            );

            return;
        }

        Window ventana =
                btnExportar.getScene().getWindow();

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Guardar reporte de facturación (Excel)"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivo Excel (*.xlsx)",
                        "*.xlsx"
                )
        );

        fileChooser.setInitialFileName(
                "Reporte_Facturacion_"
                        + LocalDate.now()
                        + ".xlsx"
        );

        File destino =
                fileChooser.showSaveDialog(ventana);

        if (destino == null) {
            return;
        }

        try {

            new ExportadorExcel()
                    .exportarFacturasReporte(
                            FXCollections.observableArrayList(
                                    resumenParaExportar
                            ),
                            destino
                    );

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Exportación exitosa",
                    null,
                    "El reporte de facturación fue exportado correctamente a Excel (.xlsx)."
            );

        } catch (Exception e) {

            e.printStackTrace();

            AlertaUtil.mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de exportación",
                    null,
                    "No se pudo generar el archivo Excel."
            );
        }
    }


    public static class FacturaResumenModificada {

        private final LocalDate fecha;
        private final BigDecimal totalFacturado;
        private final int cantidadFacturas;
        private final int cantidadPagadas;

        private final BigDecimal efectivo;
        private final BigDecimal transferencia;
        private final BigDecimal debito;

        public FacturaResumenModificada(
                LocalDate fecha,
                BigDecimal totalFacturado,
                int cantidadFacturas,
                int cantidadPagadas,
                BigDecimal efectivo,
                BigDecimal transferencia,
                BigDecimal debito) {

            this.fecha = fecha;
            this.totalFacturado = totalFacturado;
            this.cantidadFacturas = cantidadFacturas;
            this.cantidadPagadas = cantidadPagadas;
            this.efectivo = efectivo;
            this.transferencia = transferencia;
            this.debito = debito;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public BigDecimal getTotalFacturado() {
            return totalFacturado;
        }

        public int getCantidadFacturas() {
            return cantidadFacturas;
        }

        public int getCantidadPagadas() {
            return cantidadPagadas;
        }

        public BigDecimal getEfectivo() {
            return efectivo;
        }

        public BigDecimal getTransferencia() {
            return transferencia;
        }

        public BigDecimal getDebito() {
            return debito;
        }
    }
}

