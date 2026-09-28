package controllers;

import claseslogicas.CompraResumen;
import claseslogicas.Producto;
import claseslogicas.Proveedor;
import claseslogicas.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.CompraStockService;
import service.ConsumoProductoService;
import service.ProductoStockService;
import service.ProveedorStockService;
import utilidades.AlertaUtil;
import utilidades.SesionManager;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class GestionStockController {
    @FXML private TextField txtProductoNombre, txtProductoDescripcion, txtStockInicial, txtStockMinimo;
    @FXML private CheckBox chkProductoActivo;
    @FXML private TableView<Producto> tvProductos;
    @FXML private TableColumn<Producto,String> colProdNombre,colProdDescripcion,colProdEstado,colProdAlerta;
    @FXML private TableColumn<Producto,Number> colProdStock,colProdMinimo;
    @FXML private ComboBox<Producto> cbConsumoProducto;
    @FXML private TextField txtConsumoCantidad;
    @FXML private Label lblStockConsumo;

    @FXML private TextField txtProveedorNombre,txtProveedorTelefono,txtProveedorEmail,txtProveedorDireccion;
    @FXML private TableView<Proveedor> tvProveedores;
    @FXML private TableColumn<Proveedor,String> colProvNombre,colProvTelefono,colProvEmail,colProvDireccion;

    @FXML private ComboBox<Proveedor> cbCompraProveedor;
    @FXML private ComboBox<Producto> cbCompraProducto;
    @FXML private TextField txtCompraCantidad,txtCompraPrecio;
    @FXML private TableView<CompraResumen> tvCompras;
    @FXML private TableColumn<CompraResumen,Number> colCompraId,colCompraCantidad;
    @FXML private TableColumn<CompraResumen,String> colCompraProveedor,colCompraProducto,colCompraUsuario,colCompraFecha;
    @FXML private TableColumn<CompraResumen,String> colCompraPrecio,colCompraTotal;

    private final ProductoStockService productoService=new ProductoStockService();
    private final ProveedorStockService proveedorService=new ProveedorStockService();
    private final CompraStockService compraService=new CompraStockService();
    private final ConsumoProductoService consumoService=new ConsumoProductoService();
    private Producto productoEditando;
    private Proveedor proveedorEditando;

    @FXML private Button btnProductoGuardar,btnProductoEstado,btnProveedorGuardar;

    @FXML
    private void initialize(){
        configurarTablas();
        chkProductoActivo.setSelected(true);
        tvProductos.getSelectionModel().selectedItemProperty().addListener((obs,old,v)->seleccionarProducto(v));
        tvProveedores.getSelectionModel().selectedItemProperty().addListener((obs,old,v)->seleccionarProveedor(v));
        cbConsumoProducto.valueProperty().addListener((obs,old,v)->actualizarStockConsumo());
        cargarTodo();
    }

    private void configurarTablas(){
        colProdNombre.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNombre()));
        colProdDescripcion.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getDescripcion()==null?"":c.getValue().getDescripcion()));
        colProdStock.setCellValueFactory(c->new javafx.beans.property.SimpleIntegerProperty(c.getValue().getStockActual()));
        colProdMinimo.setCellValueFactory(c->new javafx.beans.property.SimpleIntegerProperty(c.getValue().getStockMinimo()));
        colProdEstado.setCellValueFactory(c->new SimpleStringProperty(c.getValue().isActivo()?"Activo":"Inactivo"));
        colProdAlerta.setCellValueFactory(c->new SimpleStringProperty(c.getValue().stockCritico()?"STOCK MÍNIMO":"OK"));

        colProvNombre.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNombre()));
        colProvTelefono.setCellValueFactory(c->new SimpleStringProperty(n(c.getValue().getTelefono())));
        colProvEmail.setCellValueFactory(c->new SimpleStringProperty(n(c.getValue().getEmail())));
        colProvDireccion.setCellValueFactory(c->new SimpleStringProperty(n(c.getValue().getDireccion())));

        colCompraId.setCellValueFactory(c->new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdCompra()));
        colCompraProveedor.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getProveedor()));
        colCompraProducto.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getProducto()));
        colCompraCantidad.setCellValueFactory(c->new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCantidad()));
        colCompraPrecio.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getPrecioUnitario().toString()));
        colCompraTotal.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getTotal().toString()));
        colCompraFecha.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colCompraUsuario.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getUsuario()));
    }

    private String n(String s){return s==null?"":s;}

    private void cargarTodo(){
        try{
            var productos=productoService.listar();
            var proveedores=proveedorService.listar();
            tvProductos.setItems(FXCollections.observableArrayList(productos));
            cbConsumoProducto.setItems(FXCollections.observableArrayList(productos.stream().filter(Producto::isActivo).toList()));
            cbCompraProducto.setItems(FXCollections.observableArrayList(productos.stream().filter(Producto::isActivo).toList()));
            tvProveedores.setItems(FXCollections.observableArrayList(proveedores));
            cbCompraProveedor.setItems(FXCollections.observableArrayList(proveedores));
            tvCompras.setItems(FXCollections.observableArrayList(compraService.listar()));
        }catch(Exception e){mostrarError("No se pudo cargar la gestión de productos/stock: "+e.getMessage());}
    }

    private void seleccionarProducto(Producto p){
        productoEditando=p;
        if(p==null)return;
        txtProductoNombre.setText(p.getNombre());txtProductoDescripcion.setText(n(p.getDescripcion()));
        txtStockInicial.setText(String.valueOf(p.getStockActual()));txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        chkProductoActivo.setSelected(p.isActivo());
        btnProductoGuardar.setText("Actualizar producto"); btnProductoEstado.setText(p.isActivo()?"Dar de baja":"Activar");
    }
    private void seleccionarProveedor(Proveedor p){
        proveedorEditando=p;
        if(p==null)return;
        txtProveedorNombre.setText(p.getNombre());txtProveedorTelefono.setText(n(p.getTelefono()));txtProveedorEmail.setText(n(p.getEmail()));txtProveedorDireccion.setText(n(p.getDireccion()));
        btnProveedorGuardar.setText("Actualizar proveedor");
    }

    @FXML private void guardarProducto(){
        try{
            int stock=Integer.parseInt(txtStockInicial.getText().trim());
            int minimo=Integer.parseInt(txtStockMinimo.getText().trim());
            if(productoEditando==null){
                Producto p=new Producto(0,txtProductoNombre.getText().trim(),txtProductoDescripcion.getText().trim(),stock,minimo,chkProductoActivo.isSelected());
                productoService.registrar(p);
            }else{
                // El stock no se modifica desde actualizar producto: cambia solamente mediante compra/consumo.
                productoEditando.setNombre(txtProductoNombre.getText().trim());productoEditando.setDescripcion(txtProductoDescripcion.getText().trim());productoEditando.setStockMinimo(minimo);
                productoService.actualizar(productoEditando);
            }
            limpiarProducto();cargarTodo();mostrarInfo("Producto guardado correctamente.");
        }catch(NumberFormatException e){mostrarError("Stock y stock mínimo deben ser números enteros.");}
        catch(Exception e){mostrarError(e.getMessage());}
    }

    @FXML private void cambiarEstadoProducto(){
        if(productoEditando==null){mostrarError("Seleccione un producto.");return;}
        try{productoService.actualizarEstado(productoEditando.getIdProducto(),!productoEditando.isActivo());limpiarProducto();cargarTodo();mostrarInfo("Estado del producto actualizado.");}
        catch(Exception e){mostrarError(e.getMessage());}
    }

    @FXML private void nuevoProducto(){limpiarProducto();}
    private void limpiarProducto(){productoEditando=null;txtProductoNombre.clear();txtProductoDescripcion.clear();txtStockInicial.setText("0");txtStockMinimo.setText("0");chkProductoActivo.setSelected(true);btnProductoGuardar.setText("Registrar producto");btnProductoEstado.setText("Dar de baja");tvProductos.getSelectionModel().clearSelection();}

    @FXML private void actualizarStockConsumo(){
        Producto p=cbConsumoProducto.getValue();lblStockConsumo.setText(p==null?"Stock actual: -":"Stock actual: "+p.getStockActual());
    }

    @FXML private void registrarConsumo(){
        try{
            Producto p=cbConsumoProducto.getValue();int cantidad=Integer.parseInt(txtConsumoCantidad.getText().trim());Usuario u=usuarioActual();
            if(p==null)throw new IllegalArgumentException("Seleccione un producto.");
            consumoService.registrar(p.getIdProducto(),cantidad,u.getId());
            txtConsumoCantidad.clear();cargarTodo();mostrarInfo("Consumo registrado. El stock fue descontado.");
        }catch(NumberFormatException e){mostrarError("La cantidad debe ser un número entero.");}catch(Exception e){mostrarError(e.getMessage());}
    }

    @FXML private void guardarProveedor(){
        try{
            if(proveedorEditando==null)proveedorService.registrar(new Proveedor(0,txtProveedorNombre.getText().trim(),txtProveedorTelefono.getText().trim(),txtProveedorEmail.getText().trim(),txtProveedorDireccion.getText().trim()));
            else{proveedorEditando.setNombre(txtProveedorNombre.getText().trim());proveedorEditando.setTelefono(txtProveedorTelefono.getText().trim());proveedorEditando.setEmail(txtProveedorEmail.getText().trim());proveedorEditando.setDireccion(txtProveedorDireccion.getText().trim());proveedorService.actualizar(proveedorEditando);}
            limpiarProveedor();cargarTodo();mostrarInfo("Proveedor guardado correctamente.");
        }catch(Exception e){mostrarError(e.getMessage());}
    }
    @FXML private void nuevoProveedor(){limpiarProveedor();}
    private void limpiarProveedor(){proveedorEditando=null;txtProveedorNombre.clear();txtProveedorTelefono.clear();txtProveedorEmail.clear();txtProveedorDireccion.clear();btnProveedorGuardar.setText("Registrar proveedor");tvProveedores.getSelectionModel().clearSelection();}

    @FXML private void registrarCompra(){
        try{
            Proveedor prov=cbCompraProveedor.getValue();Producto prod=cbCompraProducto.getValue();int cantidad=Integer.parseInt(txtCompraCantidad.getText().trim());BigDecimal precio=new BigDecimal(txtCompraPrecio.getText().trim());
            if(prov==null)throw new IllegalArgumentException("Seleccione un proveedor.");if(prod==null)throw new IllegalArgumentException("Seleccione un producto.");
            compraService.registrar(prov.getIdProveedor(),prod.getIdProducto(),cantidad,precio,usuarioActual().getId());
            txtCompraCantidad.clear();txtCompraPrecio.clear();cargarTodo();mostrarInfo("Compra registrada. El stock fue incrementado.");
        }catch(NumberFormatException e){mostrarError("Cantidad y precio deben tener un formato válido.");}catch(Exception e){mostrarError(e.getMessage());}
    }

    @FXML private void refrescar(){cargarTodo();}
    private Usuario usuarioActual(){Usuario u=SesionManager.getInstance().getUsuarioLogueado();if(u==null)throw new IllegalStateException("No hay un usuario con sesión iniciada.");return u;}
    private void mostrarInfo(String m){AlertaUtil.mostrarAlerta(Alert.AlertType.INFORMATION,"Operación realizada",null,m);}
    private void mostrarError(String m){AlertaUtil.mostrarAlerta(Alert.AlertType.ERROR,"Error",null,m==null?"Se produjo un error.":m);}
}
