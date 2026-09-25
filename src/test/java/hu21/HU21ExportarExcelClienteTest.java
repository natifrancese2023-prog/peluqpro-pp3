package hu21;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * HU21 - Exportar reporte por cliente en Excel.
 *
 * ESTADO: BLOCKED (infraestructura de este entorno de testing, no del código).
 *
 * ExportadorExcel.exportarClientesReporte() usa org.apache.poi.xssf (formato
 * .xlsx). El pom.xml del proyecto declara poi/poi-ooxml 5.2.3 (Maven
 * Central). Este entorno de testing no tiene acceso a Maven Central (ver
 * TESTING-REPORT.md) y sólo pudo instalar Apache POI 4.0.1 vía apt de
 * Ubuntu; el paquete poi-ooxml-schemas de esa versión no incluye las clases
 * de esquema OOXML generadas que la propia POI 4.0.1 busca en runtime
 * (NoClassDefFoundError: .../TypeSystemHolder), aun estando todos los jars
 * relevantes en el classpath. Es una incompatibilidad de packaging de
 * Ubuntu, no del código de producción -- en una máquina con Maven Central
 * real (mvn test), este código debería ejecutar sin problema.
 *
 * Nota para cuando se pueda ejecutar: revisando el código fuente de
 * ExportadorExcel.exportarClientesReporte() se confirmó una inspección
 * ESTÁTICA (lectura de código, no ejecución) de que sólo crea UNA hoja
 * ("Reporte de Clientes") con la tabla, sin ningún gráfico ni segunda hoja
 * -- el criterio "tabla y gráfico en hojas separadas" probablemente falle
 * igual una vez que se pueda ejecutar. Queda pendiente de confirmación real.
 */
class HU21ExportarExcelClienteTest {

    @Test
    @Disabled("BLOCKED: incompatibilidad de versiones de Apache POI/xmlbeans en este entorno de testing " +
            "(sin acceso a Maven Central). Ver TESTING-REPORT.md.")
    @DisplayName("HU21 - BLOCKED: el Excel se genera correctamente con tabla y datos del cliente")
    void excelSeGeneraConTabla() {
        fail("BLOCKED: NoClassDefFoundError de Apache POI/xmlbeans en este entorno (POI 4.0.1 de apt, incompleto).");
    }

    @Test
    @Disabled("BLOCKED: mismo motivo. Revisión estática del código (sin ejecutar) sugiere que además fallaría " +
            "por diseño: ExportadorExcel sólo genera una hoja, sin gráfico. Pendiente de confirmar en ejecución real.")
    @DisplayName("HU21 - BLOCKED: tabla y gráfico en hojas separadas")
    void excelIncluyeGraficoEnHojaSeparada() {
        fail("BLOCKED: no se pudo ejecutar Apache POI en este entorno para confirmarlo en la práctica.");
    }
}
