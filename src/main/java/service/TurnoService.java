package service;

import claseslogicas.*;
import dao.DisponibilidadDAO;
import dao.EmpleadoDAO;
import dao.HorarioAtencionDAO;
import dao.TurnoDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurnoService {

    private static final Logger log = LoggerFactory.getLogger(TurnoService.class);

    private final TurnoDAO turnoDAO = new TurnoDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final HorarioAtencionDAO horarioDAO = new HorarioAtencionDAO();
    private final VisitaService visitaService = new VisitaService();
    private final FacturaService facturaService = new FacturaService();
    private final DisponibilidadDAO disponibilidadDAO = new DisponibilidadDAO();

    private static final int INTERVALO_MINUTOS = 30;
    public List<BloqueDisponible> buscarDisponibilidad(
            LocalDate fecha,
            int duracionTotalMinutos,
            Integer idEstilistaOpcional) throws SQLException {

        List<BloqueDisponible> disponibles = new ArrayList<>();

        // 1. Obtener horario general del salón
        HorarioAtencion horario = horarioDAO.obtenerHorarioPorDia(fecha);

        if (horario == null
                || horario.getHoraApertura().equals(horario.getHoraCierre())) {
            return disponibles;
        }

        LocalTime aperturaSalon = horario.getHoraApertura();
        LocalTime cierreSalon = horario.getHoraCierre();

        // 2. Obtener profesionales
        List<Empleado> estilistas = (idEstilistaOpcional != null)
                ? empleadoDAO.obtenerEstilistasPorId(idEstilistaOpcional)
                : empleadoDAO.obtenerEstilistas();

        // 3. Obtener nombre del día
        String diaSemana = switch (fecha.getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };

        // 4. Procesar cada profesional
        for (Empleado empleado : estilistas) {

            List<Disponibilidad> disponibilidades =
                    disponibilidadDAO.obtenerPorEmpleado(
                            empleado.getIdEmpleado()
                    );

            // Buscar disponibilidad del profesional para ese día
            for (Disponibilidad disponibilidad : disponibilidades) {

                if (!disponibilidad.isActivo()) {
                    continue;
                }

                if (!disponibilidad.getDiaSemana().equalsIgnoreCase(diaSemana)) {
                    continue;
                }

                // 5. Intersección entre horario del salón
                //    y disponibilidad del profesional

                LocalTime inicioDisponible =
                        disponibilidad.getHoraDesde().isAfter(aperturaSalon)
                                ? disponibilidad.getHoraDesde()
                                : aperturaSalon;

                LocalTime finDisponible =
                        disponibilidad.getHoraHasta().isBefore(cierreSalon)
                                ? disponibilidad.getHoraHasta()
                                : cierreSalon;

                // No existe intersección
                if (!inicioDisponible.isBefore(finDisponible)) {
                    continue;
                }

                // 6. Generar bloques cada 30 minutos
                LocalTime horaActual = inicioDisponible;

                while (!horaActual
                        .plusMinutes(duracionTotalMinutos)
                        .isAfter(finDisponible)) {

                    // 7. Verificar que no exista otro turno
                    boolean disponible =
                            turnoDAO.validarDisponibilidad(
                                    empleado.getIdEmpleado(),
                                    fecha,
                                    horaActual,
                                    duracionTotalMinutos
                            );

                    if (disponible) {

                        disponibles.add(
                                new BloqueDisponible(
                                        horaActual,
                                        horaActual.plusMinutes(
                                                duracionTotalMinutos
                                        ),
                                        empleado
                                )
                        );
                    }

                    horaActual =
                            horaActual.plusMinutes(INTERVALO_MINUTOS);
                }
            }
        }

        return disponibles;
    }

    public boolean registrarTurno(Turno turno) throws SQLException {
        turno.setEstadoLogico(EstadoTurno.PENDIENTE);
        log.info("Registrando turno cliente={} empleado={}", turno.getIdCliente(), turno.getIdEmpleado());
        return turnoDAO.insertarTurno(turno);
    }
    // 🔹 Copia y pega esto dentro de TurnoService.java
    public Turno obtenerPorId(int idTurno) throws SQLException {
        return turnoDAO.obtenerPorId(idTurno);
    }
    public void cambiarEstado(Turno turno, EstadoTurno nuevoEstado, String motivo) throws SQLException {
        if (!turno.puedeCambiarA(nuevoEstado)) {
            throw new IllegalStateException(
                    "No se puede cambiar de " + turno.getEstadoTurno().getNombre() + " a " + nuevoEstado.getNombre());
        }

        turnoDAO.actualizarEstado(turno.getIdTurno(), nuevoEstado, motivo);
        turno.setEstadoTurno(nuevoEstado);
        turno.setMotivoLog(motivo);
        log.info("Estado de turno {} cambiado a {} con motivo={}", turno.getIdTurno(), nuevoEstado.getNombre(), motivo);
    }
    public boolean puedeFacturar(Turno turno) {
        log.info("[DEBUG] === Evaluando puedeFacturar ===");
        log.info("[DEBUG] turno null? {}", turno == null);
        if (turno != null) {
            log.info("[DEBUG] idTurno={} estado={} (hash={})",
                    turno.getIdTurno(), turno.getEstadoTurno(),
                    System.identityHashCode(turno.getEstadoTurno()));
            log.info("[DEBUG] comparación == FINALIZADO: {}", turno.getEstadoTurno() == EstadoTurno.FINALIZADO);
        }

        if (turno == null || turno.getEstadoTurno() != EstadoTurno.FINALIZADO) {
            log.info("[DEBUG] CORTA por estado");
            return false;
        }

        Visita visita = visitaService.obtenerVisitaPorTurno(turno.getIdTurno());
        log.info("[DEBUG] visita obtenida: {}", visita);
        if (visita == null) {
            log.info("[DEBUG] CORTA por visita nula");
            return false;
        }

        try {
            Factura factura = facturaService.obtenerPorTurno(turno.getIdTurno());
            log.info("[DEBUG] factura obtenida: {}", factura);
            boolean resultado = factura == null;
            log.info("[DEBUG] RESULTADO FINAL puedeFacturar = {}", resultado);
            return resultado;
        } catch (SQLException e) {
            log.error("[DEBUG] EXCEPCIÓN al buscar factura turno={}", turno.getIdTurno(), e);
            return false;
        }
    }
    public Visita obtenerVisitaDeTurno(int idTurno) {
        return visitaService.obtenerVisitaPorTurno(idTurno);
    }

    public List<Turno> obtenerTurnosPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        return turnoDAO.obtenerTurnosPorPeriodo(desde, hasta);
    }

    public List<Turno> obtenerAgenda(LocalDate fecha, Integer idEmpleado) throws SQLException {
        return turnoDAO.obtenerTurnosFiltrados(fecha, idEmpleado);
    }

    public List<Turno> obtenerTurnosPorCliente(int idCliente) throws SQLException {
        return turnoDAO.obtenerTurnosPorCliente(idCliente);
    }

    // 🔹 Nuevo: obtener todos los turnos
    public List<Turno> obtenerTodos() {
        return turnoDAO.obtenerTodos();
    }

    // 🔹 Opcional: validar existencia de cliente
    public boolean clienteExiste(int idCliente) throws SQLException {
        return turnoDAO.clienteExiste(idCliente);
    }
}