package hu32;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

class HU32RankingMargenTest {
    @Test
    @Disabled("NOT_IMPLEMENTED: no existe columna de costo estimado en servicios ni ningún cálculo de margen; depende de HU31 (tampoco implementada).")
    @DisplayName("HU32 - NOT_IMPLEMENTED: ranking de servicios por margen estimado (Precio - Costo - Comisión)")
    void rankingPorMargenEstimado() {
        fail("HU32 no está implementada.");
    }
}
