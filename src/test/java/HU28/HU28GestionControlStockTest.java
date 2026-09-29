package HU28;

import claseslogicas.Producto;
import dao.ConexionBD;
import org.junit.jupiter.api.Test;
import service.ConsumoProductoService;
import service.ProductoStockService;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

class HU28GestionControlStockTest {

    private final ProductoStockService productoService =
            new ProductoStockService();


    // =========================================================
    // TEST 1
    // Consultar stock y stock mínimo
    // =========================================================

    @Test
    void consultarStockYStockMinimo()
            throws Exception {

        Producto producto = new Producto(
                0,
                "Producto HU28 Consulta " +
                        System.currentTimeMillis(),
                "Producto para consulta de stock",
                25,
                10,
                true
        );

        try {

            productoService.registrar(producto);

            Producto obtenido =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertNotNull(
                    obtenido,
                    "El producto debe poder consultarse."
            );

            assertEquals(
                    25,
                    obtenido.getStockActual(),
                    "Debe mostrarse correctamente el stock disponible."
            );

            assertEquals(
                    10,
                    obtenido.getStockMinimo(),
                    "Debe mostrarse correctamente el stock mínimo."
            );

            assertFalse(
                    obtenido.stockCritico(),
                    "Con stock 25 y mínimo 10 el producto no debe estar en stock crítico."
            );

        } finally {

            eliminarProducto(producto.getIdProducto());
        }
    }


    // =========================================================
    // TEST 2
    // Registrar consumo y descontar stock
    // =========================================================

    @Test
    void registrarConsumoDescuentaStock()
            throws Exception {

        Producto producto = new Producto(
                0,
                "Producto HU28 Consumo " +
                        System.currentTimeMillis(),
                "Producto para prueba de consumo",
                20,
                5,
                true
        );

        try {

            productoService.registrar(producto);

            ConsumoProductoService consumoService =
                    new ConsumoProductoService();

            consumoService.registrar(
                    producto.getIdProducto(),
                    7,
                    1
            );

            Producto actualizado =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertNotNull(actualizado);

            assertEquals(
                    13,
                    actualizado.getStockActual(),
                    "El stock debe disminuir en la cantidad consumida."
            );

        } finally {

            eliminarConsumos(producto.getIdProducto());
            eliminarProducto(producto.getIdProducto());
        }
    }


    // =========================================================
    // TEST 3
    // Stock mínimo y alerta de stock crítico
    // =========================================================

    @Test
    void generarAlertaCuandoStockAlcanzaStockMinimo()
            throws Exception {

        Producto producto = new Producto(
                0,
                "Producto HU28 Critico " +
                        System.currentTimeMillis(),
                "Producto para prueba de stock mínimo",
                10,
                5,
                true
        );

        try {

            productoService.registrar(producto);

            ConsumoProductoService consumoService =
                    new ConsumoProductoService();

            consumoService.registrar(
                    producto.getIdProducto(),
                    5,
                    1
            );

            Producto actualizado =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertNotNull(actualizado);

            assertEquals(
                    5,
                    actualizado.getStockActual(),
                    "Después del consumo el stock debe quedar en 5."
            );

            assertEquals(
                    5,
                    actualizado.getStockMinimo(),
                    "El stock mínimo debe mantenerse en 5."
            );

            assertTrue(
                    actualizado.stockCritico(),
                    "Cuando el stock alcanza el mínimo establecido debe generarse la condición de stock crítico."
            );

        } finally {

            eliminarConsumos(producto.getIdProducto());
            eliminarProducto(producto.getIdProducto());
        }
    }


    // =========================================================
    // LIMPIEZA
    // =========================================================

    private void eliminarConsumos(int idProducto) {

        if (idProducto <= 0) {
            return;
        }

        String sql =
                "DELETE FROM consumo_producto_stock " +
                        "WHERE id_producto = ?";

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.executeUpdate();

        } catch (Exception ignored) {
        }
    }


    private void eliminarProducto(int idProducto) {

        if (idProducto <= 0) {
            return;
        }

        String sql =
                "DELETE FROM productos_stock " +
                        "WHERE id_producto = ?";

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.executeUpdate();

        } catch (Exception ignored) {
        }
    }
}