package claseslogicas;

import dao.ConexionBD;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ExportadorExcel implements ExportadorReporte {



    private static final String[] TABLAS_PELUQPRO = {
            "barrio",
            "ciudad",
            "cliente",
            "detalle_factura",
            "detalle_servicio",
            "documento",
            "empleado",
            "estado",
            "estado_factura",
            "factura",
            "horario_atencion",
            "metodo_pago",
            "persona",
            "provincia",
            "red_social",
            "roles",
            "servicios",
            "tipo_documento",
            "tipos_red_social",
            "turno",
            "turno_servicios",
            "usuarios",
            "visita"
    };

    @Override
    public void exportarClientesReporte(ObservableList<ClienteReporteExtendido> clientes, File destino) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Reporte de Clientes");

            hoja.createRow(0).createCell(0).setCellValue("PeluqPro");
            hoja.createRow(1).createCell(0).setCellValue("Dirección: Av. Central 123, Laguna Larga");
            hoja.createRow(2).createCell(0).setCellValue("Teléfono: 03572-400000");
            hoja.createRow(3).createCell(0).setCellValue("Email: contacto@peluqpro.com");
            hoja.createRow(4).createCell(0).setCellValue("Fecha de generación: " + LocalDate.now());

            Row encabezado = hoja.createRow(6);
            String[] columnas = {"Nombre", "Email", "Visitas", "Gasto total", "Estado último turno"};
            for (int i = 0; i < columnas.length; i++) {
                encabezado.createCell(i).setCellValue(columnas[i]);
            }

            int filaActual = 7;
            for (ClienteReporteExtendido c : clientes) {
                Row fila = hoja.createRow(filaActual++);
                fila.createCell(0).setCellValue(c.getNombreCompleto());
                fila.createCell(1).setCellValue(c.getEmail());
                fila.createCell(2).setCellValue(c.getCantidadVisitas());
                fila.createCell(3).setCellValue(c.getGastoTotal().doubleValue());
                fila.createCell(4).setCellValue(c.getEstadoUltimoTurno());
            }

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                workbook.write(fos);
            }
        }
    }

    @Override
    public void exportarFacturasReporte(ObservableList<FacturaResumen> facturas, File destino) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Reporte de Facturación");

            hoja.createRow(0).createCell(0).setCellValue("PeluqPro");
            hoja.createRow(1).createCell(0).setCellValue("Fecha de generación: " + LocalDate.now());

            Row encabezado = hoja.createRow(3);
            String[] columnas = {
                    "Fecha",
                    "Total facturado",
                    "Efectivo ($)",
                    "Transferencia ($)",
                    "Débito/Crédito ($)"
            };

            for (int i = 0; i < columnas.length; i++) {
                encabezado.createCell(i).setCellValue(columnas[i]);
            }

            int filaActual = 4;

            for (FacturaResumen fr : facturas) {

                Row fila = hoja.createRow(filaActual++);

                fila.createCell(0).setCellValue(
                        fr.getFecha().toString()
                );

                fila.createCell(1).setCellValue(
                        fr.getTotalFacturado().doubleValue()
                );

                fila.createCell(2).setCellValue(
                        fr.getEfectivo().doubleValue()
                );

                fila.createCell(3).setCellValue(
                        fr.getTransferencia().doubleValue()
                );

                fila.createCell(4).setCellValue(
                        fr.getDebito().doubleValue()
                );
            }

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                workbook.write(fos);
            }
        }
    }


    public  void exportarTodo(File destino) throws Exception {
        if (destino == null) {
            throw new IllegalArgumentException("El archivo de destino no puede ser null.");
        }

        try (Connection conn = ConexionBD.getConnection();
             Workbook workbook = new XSSFWorkbook()) {

            DatabaseMetaData metadata = conn.getMetaData();
            Set<String> tablasDisponibles = obtenerTablasPublicas(metadata);

            for (String tabla : TABLAS_PELUQPRO) {
                if (!tablasDisponibles.contains(tabla)) {
                    throw new SQLException("La tabla requerida por HU24 no existe en el esquema public: " + tabla);
                }
                exportarTabla(conn, workbook, tabla);
            }

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                workbook.write(fos);
            }
        }
    }

    private Set<String> obtenerTablasPublicas(DatabaseMetaData metadata) throws SQLException {
        Set<String> tablas = new HashSet<>();

        try (ResultSet rs = metadata.getTables(null, "public", "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME"));
            }
        }

        return tablas;
    }

    private void exportarTabla(Connection conn, Workbook workbook, String nombreTabla) throws SQLException {

        String sql = "SELECT * FROM public." + nombreTabla;

        try (var ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int cantidadColumnas = meta.getColumnCount();

            Sheet hoja = workbook.createSheet(nombreTabla);
            Row encabezado = hoja.createRow(0);

            CellStyle estiloEncabezado = workbook.createCellStyle();
            Font fuenteEncabezado = workbook.createFont();
            fuenteEncabezado.setBold(true);
            estiloEncabezado.setFont(fuenteEncabezado);

            for (int columna = 1; columna <= cantidadColumnas; columna++) {
                Cell celda = encabezado.createCell(columna - 1);
                celda.setCellValue(meta.getColumnName(columna));
                celda.setCellStyle(estiloEncabezado);
            }

            int filaActual = 1;
            while (rs.next()) {
                Row fila = hoja.createRow(filaActual++);

                for (int columna = 1; columna <= cantidadColumnas; columna++) {
                    Object valor = rs.getObject(columna);
                    escribirValor(fila.createCell(columna - 1), valor);
                }
            }

            hoja.createFreezePane(0, 1);


            for (int columna = 0; columna < cantidadColumnas; columna++) {
                int ancho = Math.min(40, Math.max(12, meta.getColumnName(columna + 1).length() + 2));
                hoja.setColumnWidth(columna, ancho * 256);
            }
        }
    }

    private void escribirValor(Cell celda, Object valor) {
        if (valor == null) {
            celda.setBlank();
        } else if (valor instanceof Number) {
            celda.setCellValue(((Number) valor).doubleValue());
        } else if (valor instanceof Boolean) {
            celda.setCellValue((Boolean) valor);
        } else if (valor instanceof Date) {
            celda.setCellValue((Date) valor);
        } else if (valor instanceof Timestamp) {
            celda.setCellValue((Timestamp) valor);
        } else if (valor instanceof Time) {
            celda.setCellValue((Time) valor);
        } else if (valor instanceof byte[]) {
            celda.setCellValue(Arrays.toString((byte[]) valor));
        } else {
            celda.setCellValue(valor.toString());
        }
    }
}
