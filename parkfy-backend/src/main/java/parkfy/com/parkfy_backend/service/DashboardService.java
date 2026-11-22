package parkfy.com.parkfy_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import parkfy.com.parkfy_backend.dto.dashboard.IndicadoresDashboardDto;
import parkfy.com.parkfy_backend.enums.StatusTicket;
import parkfy.com.parkfy_backend.repository.TicketReposirory;
import parkfy.com.parkfy_backend.repository.TipoVeiculoRepository;
import parkfy.com.parkfy_backend.repository.VeiculoRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketReposirory ticketReposirory;
    private final VeiculoRepository veiculoRepository;
    private final TipoVeiculoRepository tipoVeiculoRepository;

    /**
     * Retorna os indicadores do painel calculados diretamente no banco de dados,
     * considerando todos os registros e sem depender da paginação das telas
     * @return dto com os totais operacionais, o faturamento e as placas que estão no pátio
     */
    @Transactional(readOnly = true)
    public IndicadoresDashboardDto getIndicadores() {
        return new IndicadoresDashboardDto(
                ticketReposirory.countByStatus(StatusTicket.ABERTO),
                ticketReposirory.countByStatus(StatusTicket.FECHADO),
                ticketReposirory.somarValorPorStatus(StatusTicket.FECHADO),
                veiculoRepository.count(),
                tipoVeiculoRepository.count(),
                ticketReposirory.findPlacasByStatus(StatusTicket.ABERTO)
        );
    }
}
