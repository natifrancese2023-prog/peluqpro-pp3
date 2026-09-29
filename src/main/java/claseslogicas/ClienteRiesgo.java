package claseslogicas;

import java.time.LocalDateTime;

/** Datos necesarios para identificar clientes que llevan más de tres meses sin visitar el salón. */
public class ClienteRiesgo {
    private final int idCliente;
    private final String nombreCompleto;
    private final String telefono;
    private final String email;
    private final LocalDateTime ultimaVisita;

    public ClienteRiesgo(int idCliente, String nombreCompleto, String telefono,
                         String email, LocalDateTime ultimaVisita) {
        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.email = email;
        this.ultimaVisita = ultimaVisita;
    }

    public int getIdCliente() { return idCliente; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDateTime getUltimaVisita() { return ultimaVisita; }
}
