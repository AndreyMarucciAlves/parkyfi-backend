package parkfy.com.parkfy_backend.dto.tarifa;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TarifaSaveDto(
   Long id,
   @NotNull(message = "O valor da tarifa é obrigatório")
   BigDecimal valorHora
) {}
