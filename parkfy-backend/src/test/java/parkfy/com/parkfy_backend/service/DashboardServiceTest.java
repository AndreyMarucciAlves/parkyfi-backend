package parkfy.com.parkfy_backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import parkfy.com.parkfy_backend.dto.dashboard.IndicadoresDashboardDto;
import parkfy.com.parkfy_backend.enums.StatusTicket;
import parkfy.com.parkfy_backend.repository.TicketReposirory;
import parkfy.com.parkfy_backend.repository.TipoVeiculoRepository;
import parkfy.com.parkfy_backend.repository.VeiculoRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService")
class DashboardServiceTest {

    @Mock
    private TicketReposirory ticketReposirory;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private TipoVeiculoRepository tipoVeiculoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("monta os indicadores a partir das contagens e somas do banco")
    void deveMontarIndicadores() {
        when(ticketReposirory.countByStatus(StatusTicket.ABERTO)).thenReturn(2L);
        when(ticketReposirory.countByStatus(StatusTicket.FECHADO)).thenReturn(5L);
        when(ticketReposirory.somarValorPorStatus(StatusTicket.FECHADO)).thenReturn(new BigDecimal("87.50"));
        when(veiculoRepository.count()).thenReturn(7L);
        when(tipoVeiculoRepository.count()).thenReturn(3L);
        when(ticketReposirory.findPlacasByStatus(StatusTicket.ABERTO)).thenReturn(List.of("ABC1D23", "XYZ9A87"));

        IndicadoresDashboardDto indicadores = dashboardService.getIndicadores();

        assertThat(indicadores).isEqualTo(new IndicadoresDashboardDto(
                2L, 5L, new BigDecimal("87.50"), 7L, 3L, List.of("ABC1D23", "XYZ9A87")));
    }

    @Test
    @DisplayName("retorna zeros e lista vazia quando não há registros")
    void deveRetornarIndicadoresZeradosSemRegistros() {
        when(ticketReposirory.countByStatus(StatusTicket.ABERTO)).thenReturn(0L);
        when(ticketReposirory.countByStatus(StatusTicket.FECHADO)).thenReturn(0L);
        when(ticketReposirory.somarValorPorStatus(StatusTicket.FECHADO)).thenReturn(BigDecimal.ZERO);
        when(veiculoRepository.count()).thenReturn(0L);
        when(tipoVeiculoRepository.count()).thenReturn(0L);
        when(ticketReposirory.findPlacasByStatus(StatusTicket.ABERTO)).thenReturn(List.of());

        IndicadoresDashboardDto indicadores = dashboardService.getIndicadores();

        assertThat(indicadores.ticketsAbertos()).isZero();
        assertThat(indicadores.ticketsFechados()).isZero();
        assertThat(indicadores.faturamento()).isEqualByComparingTo("0");
        assertThat(indicadores.placasNoPatio()).isEmpty();
    }
}
