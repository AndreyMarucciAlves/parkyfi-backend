package parkfy.com.parkfy_backend.dto.ticket;

import parkfy.com.parkfy_backend.enums.StatusTicket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponseDto(
   Long id,
   Long veiculoId,
   String placa,
   LocalDateTime dataEntrada,
   LocalDateTime dataSaida,
   BigDecimal valorTotal,
   StatusTicket status
) {}
