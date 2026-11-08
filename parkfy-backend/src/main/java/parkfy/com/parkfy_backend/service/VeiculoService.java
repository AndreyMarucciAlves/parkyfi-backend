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
import parkfy.com.parkfy_backend.dto.tipoveiculo.VeiculoSaveDto;
import parkfy.com.parkfy_backend.infra.exception.ResourceAlreadyExistsException;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;
import parkfy.com.parkfy_backend.model.TipoVeiculo;
import parkfy.com.parkfy_backend.model.Veiculo;
import parkfy.com.parkfy_backend.repository.VeiculoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final TipoVeiculoService tipoVeiculoService; //Necessário para fazer a associação das entidades

    /**
     * Retorna uma Lista de tipos de veículo Paginados
     * @param filterPageable dto com os dados de paginação
     * @return dto com os dados de paginação e a lista de tipos de veículo
     * @author: Andrey Marucci
     */
    @Transactional(readOnly = true)
    public ResponsePagedCommom<VeiculoSaveDto> findAllPageable(FilterPageable filterPageable) {
        Page<Veiculo> listVeiculoBd = veiculoRepository.findAll(PageRequest.of(filterPageable.getPage(), filterPageable.getSize(), Sort.by(filterPageable.getDirection(), filterPageable.getOrderBy())));

        List<VeiculoSaveDto> listVeiculoResponse = listVeiculoBd.stream()
                .map(this::toDto)
                .toList();

        return new ResponsePagedCommom<VeiculoSaveDto>(
                listVeiculoResponse,
                listVeiculoBd.getTotalElements(),
                listVeiculoBd.getTotalPages(),
                filterPageable.getSize()
        );
    }

    /**
     * Salva ou atualiza um tipo de veículo no Banco de Dados (Upsert)
     * @param veiculoSaveDto dto com os dados do tipo de veículo a ser salvo ou atualizado
     * @return id do tipo de veículo salvo ou atualizado
     * @author: Andrey Marucci
     */
    @Transactional
    public Long save(VeiculoSaveDto veiculoSaveDto){
        this.validarPlacaUnica(veiculoSaveDto.placa(), veiculoSaveDto.id());

        Veiculo veiculo;

        if(veiculoSaveDto.id() != null && veiculoSaveDto.id() > 0) {
            veiculo = this.getById(veiculoSaveDto.id());
        } else {

            veiculo = new Veiculo();
        }

        veiculo.setPlaca(veiculoSaveDto.placa());
        veiculo.setCor(veiculoSaveDto.cor());
        veiculo.setModelo(veiculoSaveDto.modelo());

        TipoVeiculo tipoVeiculo = tipoVeiculoService.getById(veiculoSaveDto.idTipoVeiculo());
        veiculo.setTipoVeiculo(tipoVeiculo);

        return veiculoRepository.save(veiculo).getId();
    }

    /**
     * Retorna os dados de um veículo para edição
     * @param id id do tipo de veículo a ser editado
     * @return dto com os dados do tipo de veículo a ser editado
     * @author: Andrey Marucci
     */
    @Transactional(readOnly = true)
    public VeiculoSaveDto getEdicao(Long id) {
        return this.toDto(this.getById(id));
    }

    /**
     * Deleta um veículo no banco de dados
     * @param id id do veículo a ser deletado. Caso não encontre nada, uma exception personalizada avisa o usuário
     * que não encontrou o veículo com o id informado.
     */
    @Transactional
    public void delete(Long id){
        Veiculo veiculo = this.getById(id);
        veiculoRepository.delete(veiculo);
    }

    /**
     * Função Auxiliar para buscar um veículo pelo id, caso não encontre lança uma exception personalizada
     * @param id id do tipo de veículo a ser buscado
     * @return tipo de veículo encontrado
     * @author: Andrey Marucci
     */
    public Veiculo getById(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo", id));
    }

    /**
     * Função auxiliar para validar uma placa repetida não será criada
     * @param placa número de placa a ser validado
     * @param id em casos de update, busca por id e placa
     * @author: Andrey Marucci
     */
    private void validarPlacaUnica(String placa, Long id) {
        boolean placaEmUso = (id == null || id <= 0)
                ? veiculoRepository.existsByPlaca(placa)
                : veiculoRepository.existsByPlacaAndIdNot(placa, id);

        if (placaEmUso) {
            throw new ResourceAlreadyExistsException("Veículo", "placa", placa);
        }
    }

    // Função auxiliar para buscar veiculo pelo número da placa (Unique value no banco de dados)
    public Veiculo getVeiculoByPlaca(String placa) {
        return veiculoRepository.findByPlacaIgnoreCase(placa)
                .orElse( null);
    }

    private VeiculoSaveDto toDto(Veiculo veiculo) {
        return new VeiculoSaveDto(veiculo.getId(), veiculo.getPlaca(), veiculo.getModelo(), veiculo.getCor(), veiculo.getTipoVeiculo().getId());
    }
}
