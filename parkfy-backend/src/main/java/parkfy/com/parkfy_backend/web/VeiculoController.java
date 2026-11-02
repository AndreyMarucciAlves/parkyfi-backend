package parkfy.com.parkfy_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import parkfy.com.parkfy_backend.dto.pageable.FilterPageable;
import parkfy.com.parkfy_backend.dto.pageable.ResponsePagedCommom;
import parkfy.com.parkfy_backend.dto.tipoveiculo.VeiculoSaveDto;
import parkfy.com.parkfy_backend.service.VeiculoService;

@RestController
@RequestMapping("/veiculo")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @GetMapping
    public ResponseEntity<ResponsePagedCommom<VeiculoSaveDto>> findAllPageable(@Valid FilterPageable filterPageable){
        return ResponseEntity.ok(veiculoService.findAllPageable(filterPageable));
    }

    @PostMapping("/save")
    public ResponseEntity<Long> save(@Valid @RequestBody VeiculoSaveDto veiculoSaveDto){
        Long id = veiculoService.save(veiculoSaveDto);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/edicao/{idVeiculo}")
    public ResponseEntity<VeiculoSaveDto> getEdicao(@PathVariable Long idVeiculo){
        return ResponseEntity.ok(veiculoService.getEdicao(idVeiculo));
    }

    @DeleteMapping("/{idVeiculo}")
    public ResponseEntity<Void> deleteVeiculo(@PathVariable Long idVeiculo){
        veiculoService.delete(idVeiculo);
        return  ResponseEntity.ok().build();
    }
}
