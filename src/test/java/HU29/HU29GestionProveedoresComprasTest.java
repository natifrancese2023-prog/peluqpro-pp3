package HU29;

import claseslogicas.CompraResumen;
import claseslogicas.Producto;
import claseslogicas.Proveedor;
import dao.ConexionBD;
import org.junit.jupiter.api.Test;
import service.CompraStockService;
import service.ProductoStockService;
import service.ProveedorStockService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HU29GestionProveedoresComprasTest {

    private final ProveedorStockService proveedorService =
            new ProveedorStockService();

    private final ProductoStockService productoService =
            new ProductoStockService();

    private final CompraStockService compraService =
            new CompraStockService();


    // =========================================================
    // TEST 1
    // Registrar, consultar y actualizar proveedor
    // =========================================================

    @Test
    void registrarConsultarYActualizarProveedor()
            throws Exception {

        String nombreInicial =
                "Proveedor HU29 " + System.currentTimeMillis();

        String nombreActualizado =
                nombreInicial + " Actualizado";

        Proveedor proveedor = new Proveedor(
                0,
                nombreInicial,
                "3572400000",
                "proveedor.hu29@test.com",
                "Direccion HU29"
        );

        try {

            // Registrar
            proveedorService.registrar(proveedor);

            assertTrue(
                    proveedor.getIdProveedor() > 0,
                    "El proveedor debe registrarse y obtener un identificador."
            );

            // Consultar
            Proveedor obtenido =
                    proveedorService.obtener(
                            proveedor.getIdProveedor()
                    );

            assertNotNull(
                    obtenido,
                    "El proveedor registrado debe poder consultarse."
            );

            assertEquals(
                    nombreInicial,
                    obtenido.getNombre()
            );

            assertEquals(
                    "3572400000",
                    obtenido.getTelefono()
            );

            assertEquals(
                    "proveedor.hu29@test.com",
                    obtenido.getEmail()
            );

            assertEquals(
                    "Direccion HU29",
                    obtenido.getDireccion()
            );

            // Actualizar
            proveedor.setNombre(nombreActualizado);
            proveedor.setTelefono("3572401111");
            proveedor.setEmail("proveedor.actualizado@test.com");
            proveedor.setDireccion("Nueva Direccion HU29");

            proveedorService.actualizar(proveedor);

            Proveedor actualizado =
                    proveedorService.obtener(
                            proveedor.getIdProveedor()
                    );

            assertNotNull(actualizado);

            assertEquals(
                    nombreActualizado,
                    actualizado.getNombre()
            );

            assertEquals(
                    "3572401111",
                    actualizado.getTelefono()
            );

            assertEquals(
                    "proveedor.actualizado@test.com",
                    actualizado.getEmail()
            );

            assertEquals(
                    "Nueva Direccion HU29",
                    actualizado.getDireccion()
            );

        } finally {

            eliminarProveedor(
                    proveedor.getIdProveedor()
            );
        }
    }


    // =========================================================
    // TEST 2
    // Registrar compra y asociarla con proveedor y producto
    // =========================================================

    @Test
    void registrarCompraAsociadaAProveedorYProducto()
            throws Exception {

        Proveedor proveedor = new Proveedor(
                0,
                "Proveedor HU29 Compra " +
                        System.currentTimeMillis(),
                "3572402222",
                "compra.hu29@test.com",
                "Direccion Compra HU29"
        );

        Producto producto = new Producto(
                0,
                "Producto HU29 Compra " +
                        System.currentTimeMillis(),
                "Producto utilizado para prueba de compra",
                10,
                5,
                true
        );

        BigDecimal precioUnitario =
                new BigDecimal("2500.00");

        int cantidad = 4;

        try {

            proveedorService.registrar(proveedor);
            productoService.registrar(producto);

            int stockInicial =
                    producto.getStockActual();

            compraService.registrar(
                    proveedor.getIdProveedor(),
                    producto.getIdProducto(),
                    cantidad,
                    precioUnitario,
                    1
            );

            // Verificar que la compra quede disponible
            // para su consulta mediante el listado.
            List<CompraResumen> compras =
                    compraService.listar();

            CompraResumen compraEncontrada =
                    compras.stream()
                            .filter(c ->
                                    c.getProveedor()
                                            .equals(proveedor.getNombre())
                                            &&
                                            c.getProducto()
                                                    .equals(producto.getNombre())
                            )
                            .findFirst()
                            .orElse(null);

            assertNotNull(
                    compraEncontrada,
                    "La compra registrada debe quedar disponible para su consulta."
            );

            assertEquals(
                    cantidad,
                    compraEncontrada.getCantidad()
            );

            assertEquals(
                    0,
                    compraEncontrada
                            .getPrecioUnitario()
                            .compareTo(precioUnitario)
            );

            BigDecimal totalEsperado =
                    precioUnitario.multiply(
                            BigDecimal.valueOf(cantidad)
                    );

            assertEquals(
                    0,
                    compraEncontrada
                            .getTotal()
                            .compareTo(totalEsperado)
            );

            assertEquals(
                    proveedor.getNombre(),
                    compraEncontrada.getProveedor()
            );

            assertEquals(
                    producto.getNombre(),
                    compraEncontrada.getProducto()
            );

            // La compra también actualiza el stock.
            Producto productoActualizado =
                    productoService.obtener(
                            producto.getIdProducto()
                    );

            assertEquals(
                    stockInicial + cantidad,
                    productoActualizado.getStockActual(),
                    "Al registrar una compra debe incrementarse el stock del producto."
            );

        } finally {

            eliminarComprasDelProveedor(
                    proveedor.getIdProveedor()
            );

            eliminarProveedor(
                    proveedor.getIdProveedor()
            );

            eliminarProducto(
                    producto.getIdProducto()
            );
        }
    }


    // =========================================================
    // TEST 3
    // Generar listado completo de compras
    // =========================================================

    @Test
    void generarListadoDeCompras()
            throws Exception {

        Proveedor proveedor = new Proveedor(
                0,
                "Proveedor HU29 Listado " +
                        System.currentTimeMillis(),
                "3572403333",
                "listado.hu29@test.com",
                "Direccion Listado HU29"
        );

        Producto producto = new Producto(
                0,
                "Producto HU29 Listado " +
                        System.currentTimeMillis(),
                "Producto para listado de compras",
                5,
                2,
                true
        );

        try {

            proveedorService.registrar(proveedor);
            productoService.registrar(producto);

            compraService.registrar(
                    proveedor.getIdProveedor(),
                    producto.getIdProducto(),
                    3,
                    new BigDecimal("1800.00"),
                    1
            );

            List<CompraResumen> listado =
                    compraService.listar();

            assertNotNull(
                    listado,
                    "El sistema debe generar un listado de compras."
            );

            assertFalse(
                    listado.isEmpty(),
                    "El listado de compras no debe estar vacío después de registrar una compra."
            );

            CompraResumen compra =
                    listado.stream()
                            .filter(c ->
                                    c.getProveedor()
                                            .equals(proveedor.getNombre())
                                            &&
                                            c.getProducto()
                                                    .equals(producto.getNombre())
                            )
                            .findFirst()
                            .orElse(null);

            assertNotNull(
                    compra,
                    "La compra registrada debe aparecer en el listado."
            );

            assertTrue(
                    compra.getIdCompra() > 0,
                    "La compra debe tener identificador."
            );

            assertEquals(
                    proveedor.getNombre(),
                    compra.getProveedor()
            );

            assertEquals(
                    producto.getNombre(),
                    compra.getProducto()
            );

            assertEquals(
                    3,
                    compra.getCantidad()
            );

            assertEquals(
                    0,
                    compra.getPrecioUnitario()
                            .compareTo(
                                    new BigDecimal("1800.00")
                            )
            );

            assertEquals(
                    0,
                    compra.getTotal()
                            .compareTo(
                                    new BigDecimal("5400.00")
                            )
            );

            assertNotNull(
                    compra.getFecha(),
                    "La compra debe registrar la fecha."
            );

            assertNotNull(
                    compra.getUsuario(),
                    "La compra debe registrar el usuario que la realizó."
            );

        } finally {

            eliminarComprasDelProveedor(
                    proveedor.getIdProveedor()
            );

            eliminarProveedor(
                    proveedor.getIdProveedor()
            );

            eliminarProducto(
                    producto.getIdProducto()
            );
        }
    }


    // =========================================================
    // LIMPIEZA DE DATOS DE PRUEBA
    // =========================================================

    private void eliminarComprasDelProveedor(
            int idProveedor) {

        if (idProveedor <= 0) {
            return;
        }

        String sqlDetalle =
                """
                DELETE FROM detalle_compra_stock
                WHERE id_compra IN (
                    SELECT id_compra
                    FROM compras_stock
                    WHERE id_proveedor = ?
                )
                """;

        String sqlCompra =
                """
                DELETE FROM compras_stock
                WHERE id_proveedor = ?
                """;

        try (Connection conn =
                     ConexionBD.getConnection()) {

            try (PreparedStatement ps =
                         conn.prepareStatement(sqlDetalle)) {

                ps.setInt(1, idProveedor);
                ps.executeUpdate();
            }

            try (PreparedStatement ps =
                         conn.prepareStatement(sqlCompra)) {

                ps.setInt(1, idProveedor);
                ps.executeUpdate();
            }

        } catch (Exception ignored) {
        }
    }


    private void eliminarProveedor(
            int idProveedor) {

        if (idProveedor <= 0) {
            return;
        }

        String sql =
                """
                DELETE FROM proveedores_stock
                WHERE id_proveedor = ?
                """;

        try (Connection conn =
                     ConexionBD.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            ps.executeUpdate();

        } catch (Exception ignored) {
        }
    }


    private void eliminarProducto(
            int idProducto) {

        if (idProducto <= 0) {
            return;
        }

        String sql =
                """
                DELETE FROM productos_stock
                WHERE id_producto = ?
                """;

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