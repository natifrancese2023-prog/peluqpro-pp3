package claseslogicas;


public enum EstadoFactura {
    PENDIENTE(1, "Pendiente"),
    FACTURADA(2, "Facturada"),
    ANULADA(3, "Anulada"),
    PAGADA(4, "Pagada");

    private final int id;
    private final String nombre;

    EstadoFactura(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getIdEstadoFactura() { return id; }
    public String getNombre() { return nombre; }

    public static EstadoFactura fromId(int id) {
        for (EstadoFactura estado : values()) {
            if (estado.getIdEstadoFactura() == id) {
                return estado;
            }
        }
        throw new IllegalArgumentException("EstadoFactura no válido: " + id);
    }


    public boolean puedeTransicionarA(EstadoFactura destino) {
        return switch (this) {
            case PENDIENTE -> destino == FACTURADA || destino == ANULADA;
            case FACTURADA -> destino == PAGADA || destino == ANULADA;
            case PAGADA -> false;   // estado terminal: no se puede anular ni repagar
            case ANULADA -> false;  // estado terminal
        };
    }

    @Override
    public String toString() {
        return nombre;
    }
}