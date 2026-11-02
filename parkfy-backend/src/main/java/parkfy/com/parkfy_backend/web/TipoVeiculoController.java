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
import parkfy.com.parkfy_backend.dto.tipoveiculo.TipoVeiculoSaveDto;
import parkfy.com.parkfy_backend.service.TipoVeiculoService;

@RestController
@RequestMapping("/tipo-veiculo")
@RequiredArgsConstructor
public class TipoVeiculoController {

    private final TipoVeiculoService tipoVeiculoService;

    @GetMapping
    public ResponseEntity<ResponsePagedCommom<TipoVeiculoSaveDto>> findAllPageable(@Valid FilterPageable filterPageable) {
        return ResponseEntity.ok(tipoVeiculoService.findAllPageable(filterPageable));
    }

    @PostMapping("/save")
    public ResponseEntity<Long> save(@Valid @RequestBody TipoVeiculoSaveDto tipoVeiculoSaveDto) {
        Long id = tipoVeiculoService.save(tipoVeiculoSaveDto);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/edicao/{idTipoVeiculo}")
    public ResponseEntity<TipoVeiculoSaveDto> getEdicao(@PathVariable Long idTipoVeiculo) {
        return ResponseEntity.ok(tipoVeiculoService.getEdicao(idTipoVeiculo));
    }

    @DeleteMapping(path = "{idTipoVeiculo}")
    public ResponseEntity<Void> deleteTipoVeiculo(@PathVariable Long idTipoVeiculo) {
        tipoVeiculoService.delete(idTipoVeiculo);
        return ResponseEntity.ok().build();
    }
}
