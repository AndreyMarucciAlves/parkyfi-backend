package parkfy.com.parkfy_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import parkfy.com.parkfy_backend.enums.StatusTicket;
import parkfy.com.parkfy_backend.model.Ticket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TicketReposirory extends JpaRepository<Ticket, Long> {
    // Verifica se existe um ticket ativo (sem data de saída) para o veículo com o ID fornecido e status "ABERTO"
    boolean existsByVeiculoIdAndDataSaidaIsNullAndStatusEquals(Long veiculoId, StatusTicket status);

    long countByStatus(StatusTicket status);

    Page<Ticket> findAllByStatus(StatusTicket status, Pageable pageable);

    @Query("select coalesce(sum(t.valor), 0) from Ticket t where t.status = :status")
    BigDecimal somarValorPorStatus(@Param("status") StatusTicket status);

    @Query("select v.placa from Ticket t join t.veiculo v where t.status = :status")
    List<String> findPlacasByStatus(@Param("status") StatusTicket status);
}
