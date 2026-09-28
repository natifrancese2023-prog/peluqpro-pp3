package service;

import claseslogicas.HorarioAtencion;
import dao.HorarioAtencionDAO;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class HorarioAtencionService {

    private final HorarioAtencionDAO horarioDAO = new HorarioAtencionDAO();

    public HorarioAtencion obtenerHorarioPorDia(LocalDate fecha) throws SQLException {
        return horarioDAO.obtenerHorarioPorDia(fecha);
    }

    public HorarioAtencion obtenerHorarioPorDiaSemana(String diaSemana)
            throws SQLException {

        DayOfWeek dia = switch (diaSemana) {
            case "Lunes" -> DayOfWeek.MONDAY;
            case "Martes" -> DayOfWeek.TUESDAY;
            case "Miércoles", "Miercoles" -> DayOfWeek.WEDNESDAY;
            case "Jueves" -> DayOfWeek.THURSDAY;
            case "Viernes" -> DayOfWeek.FRIDAY;
            case "Sábado", "Sabado" -> DayOfWeek.SATURDAY;
            case "Domingo" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException(
                    "Día de semana no válido: " + diaSemana
            );
        };

        LocalDate fecha = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(dia));

        return obtenerHorarioPorDia(fecha);
    }
}
