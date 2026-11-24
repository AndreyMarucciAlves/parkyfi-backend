package parkfy.com.parkfy_backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import parkfy.com.parkfy_backend.dto.pageable.FilterPageable;
import parkfy.com.parkfy_backend.dto.pageable.ResponsePagedCommom;
import parkfy.com.parkfy_backend.dto.ticket.DadosTickectSaidaDto;
import parkfy.com.parkfy_backend.dto.ticket.TicketResponseDto;
import parkfy.com.parkfy_backend.dto.ticket.TicketSaidaDto;
import parkfy.com.parkfy_backend.dto.ticket.TicketSaveDto;
import parkfy.com.parkfy_backend.dto.tipoveiculo.VeiculoSaveDto;
import parkfy.com.parkfy_backend.enums.StatusTicket;
import parkfy.com.parkfy_backend.infra.exception.BadRequestException;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;
import parkfy.com.parkfy_backend.model.Ticket;
import parkfy.com.parkfy_backend.model.TipoVeiculo;
import parkfy.com.parkfy_backend.model.Veiculo;
import parkfy.com.parkfy_backend.repository.TicketReposirory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService")
class TicketServiceTest {

    @Mock
    private TicketReposirory ticketReposirory;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private TipoVeiculoService tipoVeiculoService;

    @InjectMocks
    private TicketService ticketService;

    private static final String PLACA = "ABC1D23";

    private TipoVeiculo criarTipoVeiculo(String valorHora) {
        return new TipoVeiculo(1L, "Carro", new BigDecimal(valorHora));
    }

    private Veiculo criarVeiculo(long id, TipoVeiculo tipoVeiculo) {
        return new Veiculo(id, PLACA, "Gol", "Prata", tipoVeiculo);
    }

    private Ticket criarTicketAberto(Long id, Veiculo veiculo, LocalDateTime dataEntrada) {
        return new Ticket(id, dataEntrada, null, null, StatusTicket.ABERTO, veiculo);
    }

    @Nested
    @DisplayName("findAllPageable")
    class FindAllPageable {

        @Test
        @DisplayName("converte os tickets do banco em DTOs e mantém os dados de paginação")
        void deveRetornarTicketsPaginados() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            LocalDateTime entrada = LocalDateTime.of(2026, 1, 10, 8, 0);
            LocalDateTime saida = LocalDateTime.of(2026, 1, 10, 10, 0);
            Ticket ticket = new Ticket(1L, entrada, saida, new BigDecimal("20.00"), StatusTicket.FECHADO, veiculo);

            FilterPageable filtro = new FilterPageable(0, 10, Sort.Direction.DESC, "dataEntrada");
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataEntrada"));
            when(ticketReposirory.findAll(pageable)).thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));

            ResponsePagedCommom<TicketResponseDto> resposta = ticketService.findAllPageable(filtro, null);

            assertThat(resposta.getData()).containsExactly(new TicketResponseDto(
                    1L, 5L, PLACA, entrada, saida, new BigDecimal("20.00"), StatusTicket.FECHADO));
            assertThat(resposta.getTotalRecords()).isEqualTo(1L);
            assertThat(resposta.getTotalPages()).isEqualTo(1);
            assertThat(resposta.getPageSize()).isEqualTo(10);
            verify(ticketReposirory, never()).findAllByStatus(any(), any());
        }

        @Test
        @DisplayName("filtra pelo status no banco e calcula a paginação só com os tickets daquele status")
        void deveFiltrarTicketsPorStatus() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            LocalDateTime entrada = LocalDateTime.of(2026, 1, 10, 8, 0);
            LocalDateTime saida = LocalDateTime.of(2026, 1, 10, 10, 0);
            Ticket ticket = new Ticket(1L, entrada, saida, new BigDecimal("20.00"), StatusTicket.FECHADO, veiculo);

            FilterPageable filtro = new FilterPageable(1, 5, Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "id"));
            when(ticketReposirory.findAllByStatus(StatusTicket.FECHADO, pageable))
                    .thenReturn(new PageImpl<>(List.of(ticket), pageable, 6));

            ResponsePagedCommom<TicketResponseDto> resposta = ticketService.findAllPageable(filtro, StatusTicket.FECHADO);

            assertThat(resposta.getData()).extracting(TicketResponseDto::status).containsOnly(StatusTicket.FECHADO);
            assertThat(resposta.getTotalRecords()).isEqualTo(6L);
            assertThat(resposta.getTotalPages()).isEqualTo(2);
            verify(ticketReposirory, never()).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("registrarEntrada")
    class RegistrarEntrada {

        @Test
        @DisplayName("cria um ticket ABERTO para um veículo já cadastrado sem ticket ativo")
        void deveRegistrarEntradaDeVeiculoExistente() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            when(veiculoService.getVeiculoByPlaca(PLACA)).thenReturn(veiculo);
            when(ticketReposirory.existsByVeiculoIdAndDataSaidaIsNullAndStatusEquals(5L, StatusTicket.ABERTO))
                    .thenReturn(false);
            when(ticketReposirory.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(100L);
                return t;
            });

            LocalDateTime antes = LocalDateTime.now();
            long ticketId = ticketService.registrarEntrada(new TicketSaveDto(PLACA, 1L));
            LocalDateTime depois = LocalDateTime.now();

            assertThat(ticketId).isEqualTo(100L);

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketReposirory).save(captor.capture());
            Ticket salvo = captor.getValue();
            assertThat(salvo.getVeiculo()).isSameAs(veiculo);
            assertThat(salvo.getStatus()).isEqualTo(StatusTicket.ABERTO);
            assertThat(salvo.getDataEntrada()).isBetween(antes, depois);
            assertThat(salvo.getDataSaida()).isNull();
            assertThat(salvo.getValor()).isNull();

            verify(veiculoService, never()).save(any());
            verify(tipoVeiculoService, never()).validarTipoveiculoExistente(any());
        }

        @Test
        @DisplayName("lança BadRequestException quando o veículo já tem um ticket ativo")
        void naoDevePermitirSegundoTicketAberto() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            when(veiculoService.getVeiculoByPlaca(PLACA)).thenReturn(veiculo);
            when(ticketReposirory.existsByVeiculoIdAndDataSaidaIsNullAndStatusEquals(5L, StatusTicket.ABERTO))
                    .thenReturn(true);

            assertThatThrownBy(() -> ticketService.registrarEntrada(new TicketSaveDto(PLACA, 1L)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Já existe um ticket ativo");

            verify(ticketReposirory, never()).save(any());
        }

        @Test
        @DisplayName("cadastra o veículo automaticamente quando a placa ainda não existe")
        void deveCadastrarVeiculoNovoNaEntrada() {
            Veiculo veiculoNovo = criarVeiculo(7L, criarTipoVeiculo("10.00"));
            when(veiculoService.getVeiculoByPlaca(PLACA)).thenReturn(null);
            when(veiculoService.save(any(VeiculoSaveDto.class))).thenReturn(7L);
            when(veiculoService.getById(7L)).thenReturn(veiculoNovo);
            when(ticketReposirory.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(200L);
                return t;
            });

            long ticketId = ticketService.registrarEntrada(new TicketSaveDto(PLACA, 1L));

            assertThat(ticketId).isEqualTo(200L);
            verify(tipoVeiculoService).validarTipoveiculoExistente(1L);
            verify(veiculoService).save(new VeiculoSaveDto(null, PLACA, null, null, 1L));

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketReposirory).save(captor.capture());
            assertThat(captor.getValue().getVeiculo()).isSameAs(veiculoNovo);
            assertThat(captor.getValue().getStatus()).isEqualTo(StatusTicket.ABERTO);
        }

        @Test
        @DisplayName("não cria veículo nem ticket quando o tipo de veículo informado não existe")
        void naoDeveRegistrarEntradaComTipoInexistente() {
            when(veiculoService.getVeiculoByPlaca(PLACA)).thenReturn(null);
            doThrow(new ResourceNotFoundException("Tipo de veículo", 99L))
                    .when(tipoVeiculoService).validarTipoveiculoExistente(99L);

            assertThatThrownBy(() -> ticketService.registrarEntrada(new TicketSaveDto(PLACA, 99L)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(veiculoService, never()).save(any());
            verify(ticketReposirory, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getDadosSaida")
    class GetDadosSaida {

        @Test
        @DisplayName("calcula a prévia de cobrança proporcional ao tempo estacionado")
        void deveCalcularPreviaDeSaida() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            LocalDateTime entrada = LocalDateTime.now().minusHours(2);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(criarTicketAberto(1L, veiculo, entrada)));

            DadosTickectSaidaDto dados = ticketService.getDadosSaida(1L);

            assertThat(dados.ticketId()).isEqualTo(1L);
            assertThat(dados.veiculoId()).isEqualTo(5L);
            assertThat(dados.placaVeiculo()).isEqualTo(PLACA);
            assertThat(dados.dataHoraEntrada()).isEqualTo(entrada);
            assertThat(dados.dataHoraSaida()).isAfterOrEqualTo(entrada.plusHours(2));
            // 2h a R$ 10,00/h
            assertThat(dados.valorTotal()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("não altera o ticket ao calcular a prévia")
        void naoDeveSalvarNaPrevia() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, LocalDateTime.now().minusMinutes(30));
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            ticketService.getDadosSaida(1L);

            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.ABERTO);
            assertThat(ticket.getDataSaida()).isNull();
            verify(ticketReposirory, never()).save(any());
        }

        @Test
        @DisplayName("lança BadRequestException quando o ticket já está fechado")
        void naoDeveCalcularTicketFechado() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, LocalDateTime.now().minusHours(1));
            ticket.setStatus(StatusTicket.FECHADO);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketService.getDadosSaida(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("já está fechado");
        }

        @Test
        @DisplayName("lança BadRequestException quando a entrada está no futuro")
        void naoDeveCalcularComEntradaNoFuturo() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, LocalDateTime.now().plusHours(1));
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketService.getDadosSaida(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("não pode ser anterior");
        }

        @Test
        @DisplayName("lança ResourceNotFoundException quando o ticket não existe")
        void deveFalharParaTicketInexistente() {
            when(ticketReposirory.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.getDadosSaida(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Ticket não encontrado com ID: 99");
        }
    }

    @Nested
    @DisplayName("registrarSaida")
    class RegistrarSaida {

        private final LocalDateTime entrada = LocalDateTime.of(2026, 1, 10, 8, 0);

        @Test
        @DisplayName("fecha o ticket, grava a data de saída e o valor cobrado")
        void deveRegistrarSaida() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, entrada);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            LocalDateTime saida = entrada.plusMinutes(90);
            ticketService.registrarSaida(new TicketSaidaDto(1L, saida));

            verify(ticketReposirory).save(ticket);
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.FECHADO);
            assertThat(ticket.getDataSaida()).isEqualTo(saida);
            // 1h30 a R$ 10,00/h
            assertThat(ticket.getValor()).isEqualByComparingTo("15.00");
        }

        @Test
        @DisplayName("arredonda o valor para duas casas decimais (HALF_UP)")
        void deveArredondarValor() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("5.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, entrada);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            // 20 min = 0,3333h -> 0,3333 * 5,00 = 1,6665 -> 1,67
            ticketService.registrarSaida(new TicketSaidaDto(1L, entrada.plusMinutes(20)));

            assertThat(ticket.getValor()).isEqualTo(new BigDecimal("1.67"));
        }

        @Test
        @DisplayName("cobra zero quando a saída é no mesmo minuto da entrada")
        void deveCobrarZeroSemTempoEstacionado() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, entrada);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            ticketService.registrarSaida(new TicketSaidaDto(1L, entrada.plusSeconds(30)));

            assertThat(ticket.getValor()).isEqualByComparingTo("0.00");
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.FECHADO);
        }

        @Test
        @DisplayName("lança BadRequestException quando o ticket já está fechado")
        void naoDeveFecharTicketJaFechado() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, entrada);
            ticket.setStatus(StatusTicket.FECHADO);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketService.registrarSaida(new TicketSaidaDto(1L, entrada.plusHours(1))))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("já está fechado");

            verify(ticketReposirory, never()).save(any());
        }

        @Test
        @DisplayName("lança BadRequestException quando a saída é anterior à entrada")
        void naoDevePermitirSaidaAntesDaEntrada() {
            Veiculo veiculo = criarVeiculo(5L, criarTipoVeiculo("10.00"));
            Ticket ticket = criarTicketAberto(1L, veiculo, entrada);
            when(ticketReposirory.findById(1L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketService.registrarSaida(new TicketSaidaDto(1L, entrada.minusMinutes(1))))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("não pode ser anterior");

            verify(ticketReposirory, never()).save(any());
            assertThat(ticket.getStatus()).isEqualTo(StatusTicket.ABERTO);
        }

        @Test
        @DisplayName("lança ResourceNotFoundException quando o ticket não existe")
        void deveFalharParaTicketInexistente() {
            when(ticketReposirory.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.registrarSaida(new TicketSaidaDto(99L, entrada)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(ticketReposirory, never()).save(any());
        }
    }
}
