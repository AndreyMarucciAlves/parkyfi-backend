package parkfy.com.parkfy_backend.dto.tipoveiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VeiculoSaveDto(
        Long id,
        @NotBlank(message = "A placa do veículo é obrigatória")
        String placa,
        @NotBlank(message = "O modelo do veículo é obrigatório")
        String modelo,
        @NotBlank(message = "A cor do veículo é obrigatória")
        String cor,
        @NotNull(message = "O tipo do veículo é obrigatório")
        Long idTipoVeiculo
) {}
