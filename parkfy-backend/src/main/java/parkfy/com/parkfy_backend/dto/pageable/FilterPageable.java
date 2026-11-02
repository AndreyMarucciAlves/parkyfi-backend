package parkfy.com.parkfy_backend.dto.pageable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort.Direction;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FilterPageable{
    @NotNull
    private Integer page;
    @NotNull
    private Integer size;
    @NotNull
    private Direction direction;
    @NotBlank
    private String orderBy;
}
