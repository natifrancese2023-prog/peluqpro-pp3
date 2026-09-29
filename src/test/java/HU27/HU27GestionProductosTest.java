package HU27;

import claseslogicas.Producto;
import dao.ConexionBD;
import org.junit.jupiter.api.Test;
import service.ConsumoProductoService;
import service.ProductoStockService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU27GestionProductosTest {

    private final ProductoStockService productoService =
            new ProductoStockService();


    // =========================================================
    // TEST 1
    // Registrar, consultar y listar producto
    // =========================================================

    @Test
    void registrarConsultarYListarProducto()
            throws Exception {

        Producto producto = new Producto(
                0,
                "Producto HU27 " +
                        System.currentTimeMillis(),
                "Producto creado para prueba HU27",
                20,
                5,
                true
        );

        try {

            productoService.registrar(producto);

            assertTrue(
                    producto.getIdProducto() > 0,
                    "El producto debe registrarse y obtener un identificador."
            );

            Producto obtenido =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertNotNull(
                    obtenido,
                    "El producto registrado debe poder consultarse."
            );

            assertEquals(
                    producto.getNombre(),
                    obtenido.getNombre()
            );

            assertEquals(
                    20,
                    obtenido.getStockActual()
            );

            assertEquals(
                    5,
                    obtenido.getStockMinimo()
            );

            List<Producto> productos =
                    productoService.listar();

            assertTrue(
                    productos.stream()
                            .anyMatch(p ->
                                    p.getIdProducto()
                                            == producto.getIdProducto()
                            ),
                    "El producto registrado debe aparecer en el listado."
            );

        } finally {

            eliminarProducto(producto.getIdProducto());
        }
    }


    // =========================================================
    // TEST 2
    // Actualizar información y estado
    // =========================================================

    @Test
    void actualizarInformacionYEstadoDelProducto()
            throws Exception {

        Producto producto = new Producto(
                0,
                "Producto HU27 Actualizacion " +
                        System.currentTimeMillis(),
                "Descripcion inicial",
                30,
                10,
                true
        );

        try {

            productoService.registrar(producto);

            producto.setNombre(
                    "Producto HU27 Actualizado"
            );

            producto.setDescripcion(
                    "Descripcion actualizada"
            );

            producto.setStockMinimo(15);

            productoService.actualizar(producto);

            Producto actualizado =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertNotNull(actualizado);

            assertEquals(
                    "Producto HU27 Actualizado",
                    actualizado.getNombre()
            );

            assertEquals(
                    "Descripcion actualizada",
                    actualizado.getDescripcion()
            );

            assertEquals(
                    15,
                    actualizado.getStockMinimo()
            );

            productoService.actualizarEstado(
                    producto.getIdProducto(),
                    false
            );

            Producto inactivo =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertFalse(
                    inactivo.isActivo(),
                    "El producto debe quedar inactivo."
            );

        } finally {

            eliminarProducto(producto.getIdProducto());
        }
    }


    // =========================================================
    // TEST 3
    // Producto utilizado en control de stock
    // =========================================================

    @Test
    void productoPuedeUtilizarseEnControlDeStock()
            throws Exception {

        Producto producto = new Producto(
                0,
                "Producto HU27 Stock " +
                        System.currentTimeMillis(),
                "Producto utilizado para probar stock",
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
                    5,
                    1
            );

            Producto actualizado =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertNotNull(actualizado);

            assertEquals(
                    15,
                    actualizado.getStockActual(),
                    "El producto debe poder utilizarse en el control de stock y reflejar su consumo."
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