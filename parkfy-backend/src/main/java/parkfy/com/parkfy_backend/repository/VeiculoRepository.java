package parkfy.com.parkfy_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import parkfy.com.parkfy_backend.model.Veiculo;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
}
