package parkfy.com.parkfy_backend.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record IndicadoresDashboardDto(
        long ticketsAbertos,
        long ticketsFechados,
        BigDecimal faturamento,
        long totalVeiculos,
        long totalTipos,
        List<String> placasNoPatio
) {}
