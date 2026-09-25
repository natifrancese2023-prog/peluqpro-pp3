package hu29;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * HU29 - Control de stock crítico.
 * ESTADO: NOT_IMPLEMENTED. Búsqueda exhaustiva ("stock", "producto") en todo
 * el código fuente sin resultados. No hay tabla de productos/stock en el
 * esquema real (peluqueriscema.sql), ni DAO, Service ni controller
 * relacionado. No hay nada que probar.
 */
class HU29StockCriticoTest {
    @Test
    @Disabled("NOT_IMPLEMENTED: no existe ninguna tabla, DAO, service ni controller de stock/productos en todo el proyecto.")
    @DisplayName("HU29 - NOT_IMPLEMENTED: control de stock crítico de productos")
    void controlDeStockCritico() {
        fail("HU29 no está implementada: no hay ningún rastro de gestión de stock/productos en el código ni en el esquema de base.");
    }
}
