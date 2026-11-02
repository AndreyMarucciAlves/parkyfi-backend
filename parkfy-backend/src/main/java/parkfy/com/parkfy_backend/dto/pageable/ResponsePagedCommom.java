package parkfy.com.parkfy_backend.dto.pageable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponsePagedCommom<T> {
    private List<T> data;
    private Long totalRecords;
    private int totalPages;
    private int pageSize;
}
