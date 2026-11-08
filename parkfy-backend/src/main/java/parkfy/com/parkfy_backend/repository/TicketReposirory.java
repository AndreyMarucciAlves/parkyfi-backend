package parkfy.com.parkfy_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import parkfy.com.parkfy_backend.enums.StatusTicket;
import parkfy.com.parkfy_backend.model.Ticket;

import java.util.Optional;

public interface TicketReposirory extends JpaRepository<Ticket, Long> {
    // Verifica se existe um ticket ativo (sem data de saída) para o veículo com o ID fornecido e status "ABERTO"
    boolean existsByVeiculoIdAndDataSaidaIsNullAndStatusEquals(Long veiculoId, StatusTicket status);
}
