package parkfy.com.parkfy_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import parkfy.com.parkfy_backend.dto.pageable.FilterPageable;
import parkfy.com.parkfy_backend.dto.pageable.ResponsePagedCommom;
import parkfy.com.parkfy_backend.dto.tipoveiculo.TipoVeiculoSaveDto;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;
import parkfy.com.parkfy_backend.model.TipoVeiculo;
import parkfy.com.parkfy_backend.repository.TipoVeiculoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoVeiculoService {

    private final TipoVeiculoRepository tipoVeiculoRepository;

    /**
     * Retorna uma Lista de tipos de veículo Paginados
     * @param filterPageable dto com os dados de paginação
     * @return dto com os dados de paginação e a lista de tipos de veículo
     */
    @Transactional(readOnly = true)
    public ResponsePagedCommom<TipoVeiculoSaveDto> findAllPageable(FilterPageable filterPageable) {
        Page<TipoVeiculo> listTiposVeiculoBd = tipoVeiculoRepository.findAll(PageRequest.of(filterPageable.getPage(), filterPageable.getSize(), Sort.by(filterPageable.getDirection(), filterPageable.getOrderBy())));

        List<TipoVeiculoSaveDto> listTipoVeiculoResponse = listTiposVeiculoBd.stream()
                .map(this::toDto)
                .toList();

        return new ResponsePagedCommom<TipoVeiculoSaveDto>(
                listTipoVeiculoResponse,
                listTiposVeiculoBd.getTotalElements(),
                listTiposVeiculoBd.getTotalPages(),
                filterPageable.getSize()
        );
    }

    /**
     * Salva ou atualiza um tipo de veículo no Banco de Dados (Upsert)
     * @param tipoVeiculoSaveDto dto com os dados do tipo de veículo a ser salvo ou atualizado
     * @return id do tipo de veículo salvo ou atualizado
     */
    @Transactional
    public Long save(TipoVeiculoSaveDto tipoVeiculoSaveDto) {
        TipoVeiculo tipoVeiculo;

        if(tipoVeiculoSaveDto.id() != null && tipoVeiculoSaveDto.id() > 0) {
            tipoVeiculo = this.getById(tipoVeiculoSaveDto.id());
        } else {
            tipoVeiculo = new TipoVeiculo();
        }

        tipoVeiculo.setDescricao(tipoVeiculoSaveDto.descricao());
        tipoVeiculo.setValorHora(tipoVeiculoSaveDto.valorHora());

        return tipoVeiculoRepository.save(tipoVeiculo).getId();
    }

    /**
     * Retorna os dados de um tipo de veículo para edição
     * @param id id do tipo de veículo a ser editado
     * @return dto com os dados do tipo de veículo a ser editado
     */
    @Transactional(readOnly = true)
    public TipoVeiculoSaveDto getEdicao(Long id) {
        return this.toDto(this.getById(id));
    }

    /**
     * Deleta um tipo de veículo do Banco de Dados
     * @param id id do tipo de veículo a ser deletado
     */
    @Transactional
    public void delete(Long id) {
        TipoVeiculo tipoVeiculo = this.getById(id);
        tipoVeiculoRepository.delete(tipoVeiculo);
    }

    /**
     * Função Auxiliar para buscar um tipo de veículo pelo id, caso não encontre lança uma exception personalizada
     * @param id id do tipo de veículo a ser buscado
     * @return tipo de veículo encontrado
     */
    public TipoVeiculo getById(Long id) {
        return tipoVeiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de veículo", id));
    }

    public void validarTipoveiculoExistente(Long tipoVeiculoId) {
        if(!tipoVeiculoRepository.existsById(tipoVeiculoId)) {
            throw new ResourceNotFoundException("Tipo de veículo", tipoVeiculoId);
        }
    }

    private TipoVeiculoSaveDto toDto(TipoVeiculo tipoVeiculo) {
        return new TipoVeiculoSaveDto(tipoVeiculo.getId(), tipoVeiculo.getDescricao(), tipoVeiculo.getValorHora());
    }
}
