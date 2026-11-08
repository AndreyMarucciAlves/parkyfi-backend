package parkfy.com.parkfy_backend.dto.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosTickectSaidaDto(
   Long ticketId,
   BigDecimal valorTotal,
   Long veiculoId,
   String placaVeiculo,
   LocalDateTime dataHoraEntrada,
   LocalDateTime dataHoraSaida
) {}
