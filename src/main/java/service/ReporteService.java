package service;

import claseslogicas.ClienteReporteExtendido;
import claseslogicas.ClienteRiesgo;
import dao.ReporteDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReporteService {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public List<ClienteReporteExtendido> obtenerDatosClientesExtendido() throws SQLException {
        return reporteDAO.obtenerDatosClientesExtendido();
    }

    public BigDecimal obtenerTicketPromedioCliente(int idCliente, LocalDate desde, LocalDate hasta)
            throws SQLException {
        return reporteDAO.obtenerTicketPromedioCliente(idCliente, desde, hasta);
    }
    public List<ClienteRiesgo> obtenerClientesEnRiesgo() throws SQLException {
        return reporteDAO.obtenerClientesEnRiesgo();
    }

    public BigDecimal obtenerTicketPromedio(LocalDate desde, LocalDate hasta) throws SQLException {
        return reporteDAO.obtenerTicketPromedio(desde, hasta);
    }

}
