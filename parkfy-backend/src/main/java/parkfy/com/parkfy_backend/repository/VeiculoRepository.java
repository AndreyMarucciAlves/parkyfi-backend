package parkfy.com.parkfy_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import parkfy.com.parkfy_backend.model.Veiculo;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    boolean existsByPlacaIgnoreCase(String placa);
    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);
    Optional<Veiculo> findByPlacaIgnoreCase(String placa);
}
