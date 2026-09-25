package hu33;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

class HU33ClientesRiesgoTest {
    @Test
    @Disabled("NOT_IMPLEMENTED: no existe ningún método que calcule 'clientes en riesgo' por meses sin visita. " +
            "Los datos base (visita.fecha_hora) sí existen, falta la lógica de negocio.")
    @DisplayName("HU33 - NOT_IMPLEMENTED: clientes en riesgo (> 3 meses desde la última visita)")
    void clientesEnRiesgo() {
        fail("HU33 no está implementada.");
    }
}
