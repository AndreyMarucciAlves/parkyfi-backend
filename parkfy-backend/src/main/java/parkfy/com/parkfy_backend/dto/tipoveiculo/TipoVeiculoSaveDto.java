package parkfy.com.parkfy_backend.dto.tipoveiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TipoVeiculoSaveDto(
   Long id,
   @NotBlank(message = "A descrição do tipo de veículo é obrigatória")
   String descricao,
   @NotNull(message = "O valor da hora do tipo de veículo é obrigatório")
   BigDecimal valorHora
) {}
