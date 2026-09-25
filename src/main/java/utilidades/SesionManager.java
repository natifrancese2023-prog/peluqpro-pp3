package utilidades;

import claseslogicas.Usuario;

public class SesionManager {

    private static SesionManager instance;
    private Usuario usuarioLogueado;

    private SesionManager() {}

    public static SesionManager getInstance() {
        if (instance == null) {
            instance = new SesionManager();
        }
        return instance;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    // Antes no existía: cerrarSesion() en el controller sólo cerraba la
    // ventana pero nunca limpiaba este singleton, así que el usuario logueado
    // quedaba "pegado" en memoria para el resto del proceso.
    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }
}