package parkfy.com.parkfy_backend.dto.ticket;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TicketSaidaDto(
   @NotNull(message = "É obrigatório informar o ticket para registrar a saída do veículo")
   Long ticketId,
   @NotNull(message = "É obrigatório informar a data de saída do veículo")
   LocalDateTime dataHoraSaida
) {}
