package parkfy.com.parkfy_backend.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketSaveDto(
        @NotBlank(message = "A placa do veículo é obrigatória para criação de Ticket")
        String placa,
        @NotNull(message = "O tipo do veículo é obrigatório para criação de Ticket")
        Long tipoVeiculoId
) {}
