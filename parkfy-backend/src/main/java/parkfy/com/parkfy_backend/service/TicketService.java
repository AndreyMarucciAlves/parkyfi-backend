package parkfy.com.parkfy_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import parkfy.com.parkfy_backend.model.Veiculo;
import parkfy.com.parkfy_backend.repository.TicketReposirory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketReposirory ticketReposirory;
    private final VeiculoService veiculoService;


    @Transactional(readOnly = true)
    public ResponsePagedCommom<TicketResponseDto> findAllPageable(FilterPageable filterPageable) {
        var listTicketsBd = ticketReposirory.findAll(PageRequest.of(filterPageable.getPage(), filterPageable.getSize(), Sort.by(filterPageable.getDirection(), filterPageable.getOrderBy())));

        var listTicketsResponse = listTicketsBd.stream()
                .map(this::toDto)
                .toList();

        return new ResponsePagedCommom<TicketResponseDto>(
                listTicketsResponse,
                listTicketsBd.getTotalElements(),
                listTicketsBd.getTotalPages(),
                filterPageable.getSize()
        );
    }


    /**
     * Registra a entrada de um veículo no estacionamento, criando um novo ticket associado ao veículo.
     * Se o veículo não existir, ele será criado com base no Dto fornecido
     * @throws BadRequestException se já existir um ticket ativo (sem data de saída) para o veículo
     * @param dto dto com os dados do veículo e do tipo de veículo para criação do ticket
     * @return id do ticket criado
     * @Author: João Szczypior
     */
    @Transactional
    public long registrarEntrada(TicketSaveDto dto) {
        Veiculo veiculo = veiculoService.getVeiculoByPlaca(dto.placa());

        // Verifica se o veículo existe e se há um ticket ativo (sem data de saída) para ele
        // Se o veículo não existir, cria um novo veículo com base no Dto fornecido (apenas informações básicas)
        if(veiculo != null && veiculo.getId() > 0) {
            if(ticketReposirory.existsByVeiculoIdAndDataSaidaIsNullAndStatusEquals(veiculo.getId(), StatusTicket.ABERTO)) {
                throw new BadRequestException("Já existe um ticket ativo para o veículo com a placa informada. Não é possível registrar uma nova entrada.");
            }
        } else {
            Long veiculoId = veiculoService.save(new VeiculoSaveDto(null,dto.placa(), null, null, dto.tipoVeiculoId()));
            veiculo = veiculoService.getById(veiculoId);
        }

        Ticket ticket = new Ticket();
        ticket.setVeiculo(veiculo);
        ticket.setStatus(StatusTicket.ABERTO);
        ticket.setDataEntrada(LocalDateTime.now());
        return ticketReposirory.save(ticket).getId();
    }

    /**
     * Retorna dados informativos de saida de um ticket para serem apresentados em tela no FE e validados Junto ao Cliente
     * @param ticketId id do ticket a ser consultado
     * @return Dto com os dados informativos de saída do ticket(contendo valores financeiros e de tempo)
     * @throws BadRequestException se o ticket estiver fechado
     * @throws BadRequestException se a data de saída calculada for anterior à data de entrada
     * @Author: João Szczypior
     */
    @Transactional(readOnly = true)
    public DadosTickectSaidaDto getDadosSaida(Long ticketId)  {
        Ticket ticket = getById(ticketId);

        if(ticket.getStatus() == StatusTicket.FECHADO) {
            throw new BadRequestException("O ticket informado já está fechado e não pode ser processado para saída.");
        }

        LocalDateTime dataEntrada = ticket.getDataEntrada();
        LocalDateTime dataSaida = LocalDateTime.now();

        // valida se a data de saída não é anterior à data de entrada
        validarDataSaida(ticket, dataSaida);

        // Calcula o valor total com base na diferença de tempo entre a entrada e a saída
        long minutosEstacionado = ChronoUnit.MINUTES.between(dataEntrada, dataSaida);
        BigDecimal valorTarifa = ticket.getVeiculo().getTipoVeiculo().getValorHora();

        BigDecimal valorTotal = this.calcularValorTotal(minutosEstacionado, valorTarifa);

        return new DadosTickectSaidaDto(
                ticket.getId(),
                valorTotal,
                ticket.getVeiculo().getId(),
                ticket.getVeiculo().getPlaca(),
                dataEntrada,
                dataSaida
        );
    }

    /**
     * Registra a saída de um veículo do estacionamento, atualizando o ticket associado com a data de saída/valor total e Status.
     * @param ticketSaidaDto dto com os dados do ticket e a data de saída do veículo
     * @throws BadRequestException se o ticket estiver fechado
     * @Author: João Szczypior
     */
    @Transactional
    public void registrarSaida(TicketSaidaDto ticketSaidaDto) {
        Ticket ticket = this.getById(ticketSaidaDto.ticketId());

        if(ticket.getStatus() == StatusTicket.FECHADO) {
            throw new BadRequestException("O ticket informado já está fechado e não pode ser processado para saída.");
        }

        // valida se a data de saída não é anterior à data de entrada
        validarDataSaida(ticket, ticketSaidaDto.dataHoraSaida());

        long minutosEstacionado = ChronoUnit.MINUTES.between(ticket.getDataEntrada(), ticketSaidaDto.dataHoraSaida());
        BigDecimal valorTarifa = ticket.getVeiculo().getTipoVeiculo().getValorHora();

        BigDecimal valorTotal = this.calcularValorTotal(minutosEstacionado, valorTarifa);

        ticket.setDataSaida(ticketSaidaDto.dataHoraSaida());
        ticket.setStatus(StatusTicket.FECHADO);
        ticket.setValor(valorTotal);
        ticketReposirory.save(ticket);
    }

    /**
     * Valida se a data de saída informada é anterior à data de entrada do ticket.
     * @throws BadRequestException se a data de saída for anterior à data de entrada
     * @param ticket ticket a ser validado
     * @param dataSaida data de saída a ser validada
     * @Author: João Szczypior
     */
    private void validarDataSaida(Ticket ticket, LocalDateTime dataSaida) {
        if (dataSaida.isBefore(ticket.getDataEntrada())) {
            throw new BadRequestException("A data de saída não pode ser anterior à data de entrada.");
        }
    }

    /**
     * Calcula o valor total a ser cobrado com base no tempo de estacionamento e na tarifa por hora.
     * @param minutosEstacionado tempo total em minutos que o veículo ficou estacionado
     * @param valorTarifa valor da tarifa por hora do tipo de veículo
     * @return valor total a ser cobrado, arredondado para duas casas decimais
     * @Author: João Szczypior
     */
    private BigDecimal calcularValorTotal(long minutosEstacionado, BigDecimal valorTarifa) {
        BigDecimal horasEstacionado = BigDecimal.valueOf(minutosEstacionado)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        return valorTarifa.multiply(horasEstacionado)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private TicketResponseDto toDto(Ticket ticket) {
        return new TicketResponseDto(
                ticket.getId(),
                ticket.getVeiculo().getId(),
                ticket.getVeiculo().getPlaca(),
                ticket.getDataEntrada(),
                ticket.getDataSaida(),
                ticket.getValor(),
                ticket.getStatus()
        );
    }


    private Ticket getById(Long ticketId) {
        return ticketReposirory.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
    }
}
