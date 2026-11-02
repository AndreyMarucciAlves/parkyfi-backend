package parkfy.com.parkfy_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parkfy.com.parkfy_backend.dto.pageable.FilterPageable;
import parkfy.com.parkfy_backend.dto.pageable.ResponsePagedCommom;
import parkfy.com.parkfy_backend.dto.tarifa.TarifaSaveDto;
import parkfy.com.parkfy_backend.service.TarifaService;

@RestController
@RequestMapping("/tarifa")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService tarifaService;

    @GetMapping
    public ResponseEntity<ResponsePagedCommom<TarifaSaveDto>> findAllPageable(@Valid FilterPageable filterPageable) {
        return ResponseEntity.ok(tarifaService.findAllPageable(filterPageable));
    }

    @PostMapping("/save")
    public ResponseEntity<Long> save(@Valid @RequestBody TarifaSaveDto tarivaSaveDto) {
        Long id = tarifaService.save(tarivaSaveDto);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/edicao/{idTarifa}")
    public ResponseEntity<TarifaSaveDto> getEdicao(@PathVariable Long idTarifa) {
        return ResponseEntity.ok(tarifaService.getEdicao(idTarifa));
    }

    @DeleteMapping(path = "{idTarifa}")
    public ResponseEntity<Void> deleteTarifa(@PathVariable Long idTarifa) {
        tarifaService.delete(idTarifa);
        return ResponseEntity.ok().build();
    }
}
