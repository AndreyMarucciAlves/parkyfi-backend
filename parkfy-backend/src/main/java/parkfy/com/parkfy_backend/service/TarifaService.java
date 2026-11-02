package parkfy.com.parkfy_backend.service;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import parkfy.com.parkfy_backend.dto.pageable.FilterPageable;
import parkfy.com.parkfy_backend.dto.pageable.ResponsePagedCommom;
import parkfy.com.parkfy_backend.dto.tarifa.TarifaSaveDto;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;
import parkfy.com.parkfy_backend.model.Tarifa;
import parkfy.com.parkfy_backend.repository.TarifaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaService {


    private final TarifaRepository tarifaRepository;


    /**
     * Retorna uma Lista de tarifas Paginadas
     * @param filterPageable dto com os dados de paginação
     * @return dto com os dados de paginação e a lista de tarifas
     * @Author: João Szczypior
     */
    @Transactional(readOnly = true)
    public ResponsePagedCommom<TarifaSaveDto> findAllPageable( FilterPageable filterPageable) {
        Page<Tarifa> listTarifasBd = tarifaRepository.findAll(PageRequest.of(filterPageable.getPage(), filterPageable.getSize(), Sort.by(filterPageable.getDirection(), filterPageable.getOrderBy())));

        List<TarifaSaveDto> listTarifaResponse = listTarifasBd.stream()
                .map(tarifa -> new TarifaSaveDto(tarifa.getId(), tarifa.getValorHora()))
                .toList();

        return new ResponsePagedCommom<TarifaSaveDto>(
                listTarifaResponse,
                listTarifasBd.getTotalElements(),
                listTarifasBd.getTotalPages(),
                filterPageable.getSize()
        );
    }

    /**
     * Salva ou atuliza uma tarifa no Banco de Dados(Upseart)
     * @param tarivaSaveDto dto com os dados da tarifa a ser salva ou atualizada
     * @return id da tarifa salva ou atualizada
     * @Author: João Szczypior
     */
    @Transactional
    public Long save(TarifaSaveDto tarivaSaveDto) {
        Tarifa tarifa;

        if(tarivaSaveDto.id() != null && tarivaSaveDto.id() > 0) {
            tarifa = this.getById(tarivaSaveDto.id());
        } else {
            tarifa = new Tarifa();
        }

        tarifa.setValorHora(tarivaSaveDto.valorHora());

        return tarifaRepository.save(tarifa).getId();
    }

    /**
     * Retorna os dados de uma tarifa para edição
     * @param id id da tarifa a ser editada
     * @return dto com os dados da tarifa a ser editada
     * @Author: João Szczypior
     */
    @Transactional(readOnly = true)
    public TarifaSaveDto getEdicao(Long id) {
        Tarifa tarifa = this.getById(id);

        return new TarifaSaveDto(tarifa.getId(), tarifa.getValorHora());
    }

    /**
     * Deleta uma tarifa do Banco de Dados
     * @param id id da tarifa a ser deletada
     * @Author: João Szczypior
     */
    @Transactional
    public void delete(Long id) {
        Tarifa tarifa = this.getById(id);
        tarifaRepository.delete(tarifa);
    }

    /**
     * Função Auxiliar para buscar uma tarifa pelo id, caso não encontre lança uma exception personalizada
     * @param id id da tarifa a ser buscada
     * @return tarifa encontrada
     * @Author: João Szczypior
     */
    private Tarifa getById(Long id) {
        return tarifaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa", id));
    }
}
