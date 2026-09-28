package service;

import claseslogicas.ComisionProfesional;
import dao.ComisionDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ComisionService {

    private final ComisionDAO comisionDAO = new ComisionDAO();

    public List<ComisionProfesional> obtenerComisiones(
            int idEmpleado,
            LocalDate desde,
            LocalDate hasta) throws SQLException {

        if (idEmpleado <= 0) {
            throw new IllegalArgumentException(
                    "El profesional no tiene un identificador válido."
            );
        }

        if (desde == null || hasta == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el período de consulta."
            );
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
        }

        return comisionDAO.obtenerComisiones(
                idEmpleado,
                desde,
                hasta
        );
    }
}