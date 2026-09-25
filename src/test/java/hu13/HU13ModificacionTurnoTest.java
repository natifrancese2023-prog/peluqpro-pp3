package hu13;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * HU13 - Modificación de turno (editar fecha, hora, estilista, servicio).
 *
 * ESTADO: NOT_IMPLEMENTED.
 *
 * Verificado contra el código real: ni TurnoDAO ni TurnoService exponen
 * ningún método de actualización de los DATOS de un turno ya creado
 * (fecha/hora/estilista/servicios). Lo único que existe es
 * TurnoService.cambiarEstado(turno, nuevoEstado, motivo), que cambia el
 * ESTADO (Pendiente/Confirmado/Finalizado/Cancelado/Facturado) -- eso es
 * HU17, no esto. No hay ningún controller de "editar turno" (se buscó
 * "modificar"/"editar"+"turno" en toda la carpeta controllers/, sin
 * resultados).
 *
 * Para modificar un turno existente hoy, el único camino es cancelarlo
 * (HU15) y crear uno nuevo (HU11) -- no es lo mismo que "editar in place"
 * que pide el criterio de aceptación.
 */
class HU13ModificacionTurnoTest {

    @Test
    @Disabled("NOT_IMPLEMENTED: no existe ningún método de actualización de datos (fecha/hora/estilista/" +
            "servicio) de un turno ya creado, ni controller de edición. Ver TESTING-REPORT.md.")
    @DisplayName("HU13 - NOT_IMPLEMENTED: editar fecha/hora/estilista/servicio de un turno existente")
    void modificarDatosDeTurnoExistente() {
        fail("HU13 no está implementada: no hay forma de editar los datos de un turno ya creado, solo su estado.");
    }
}
