package parkfy.com.parkfy_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import parkfy.com.parkfy_backend.model.Tarifa;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

}
