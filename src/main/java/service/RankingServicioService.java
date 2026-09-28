package service;

import claseslogicas.DetalleRankingServicio;
import claseslogicas.RankingServicio;
import dao.RankingServicioDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RankingServicioService {

    private final RankingServicioDAO rankingServicioDAO =
            new RankingServicioDAO();

    public List<RankingServicio> obtenerRanking(LocalDate desde, LocalDate hasta)
            throws SQLException {

        validarRango(desde, hasta);
        return rankingServicioDAO.obtenerRanking(desde, hasta);
    }

    public List<DetalleRankingServicio> obtenerDetalleServicio(
            int idServicio, LocalDate desde, LocalDate hasta) throws SQLException {

        if (idServicio <= 0) {
            throw new IllegalArgumentException("El servicio seleccionado no es válido.");
        }

        validarRango(desde, hasta);
        return rankingServicioDAO.obtenerDetalleServicio(idServicio, desde, hasta);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las dos fechas.");
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }
}
