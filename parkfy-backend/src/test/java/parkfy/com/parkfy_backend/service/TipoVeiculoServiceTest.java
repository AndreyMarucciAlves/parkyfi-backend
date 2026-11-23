package parkfy.com.parkfy_backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import parkfy.com.parkfy_backend.dto.pageable.FilterPageable;
import parkfy.com.parkfy_backend.dto.pageable.ResponsePagedCommom;
import parkfy.com.parkfy_backend.dto.tipoveiculo.TipoVeiculoSaveDto;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;
import parkfy.com.parkfy_backend.model.TipoVeiculo;
import parkfy.com.parkfy_backend.repository.TipoVeiculoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TipoVeiculoService")
class TipoVeiculoServiceTest {

    @Mock
    private TipoVeiculoRepository tipoVeiculoRepository;

    @InjectMocks
    private TipoVeiculoService tipoVeiculoService;

    @Nested
    @DisplayName("consultas")
    class Consultas {

        @Test
        @DisplayName("findAllPageable converte os tipos em DTOs e mantém a paginação")
        void deveListarPaginado() {
            TipoVeiculo carro = new TipoVeiculo(1L, "Carro", new BigDecimal("10.00"));
            FilterPageable filtro = new FilterPageable(0, 10, Sort.Direction.ASC, "descricao");
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "descricao"));
            when(tipoVeiculoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(carro), pageable, 1));

            ResponsePagedCommom<TipoVeiculoSaveDto> resposta = tipoVeiculoService.findAllPageable(filtro);

            assertThat(resposta.getData())
                    .containsExactly(new TipoVeiculoSaveDto(1L, "Carro", new BigDecimal("10.00")));
            assertThat(resposta.getTotalRecords()).isEqualTo(1L);
            assertThat(resposta.getTotalPages()).isEqualTo(1);
            assertThat(resposta.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("findOpcoes busca todos os tipos ordenados por descrição crescente")
        void deveListarOpcoesOrdenadas() {
            Sort ordenacao = Sort.by(Sort.Direction.ASC, "descricao");
            when(tipoVeiculoRepository.findAll(ordenacao)).thenReturn(List.of(
                    new TipoVeiculo(1L, "Carro", new BigDecimal("10.00")),
                    new TipoVeiculo(2L, "Moto", new BigDecimal("5.00"))
            ));

            List<TipoVeiculoSaveDto> opcoes = tipoVeiculoService.findOpcoes();

            assertThat(opcoes).extracting(TipoVeiculoSaveDto::descricao).containsExactly("Carro", "Moto");
        }

        @Test
        @DisplayName("getEdicao retorna o DTO do tipo de veículo")
        void deveRetornarDadosParaEdicao() {
            when(tipoVeiculoRepository.findById(1L))
                    .thenReturn(Optional.of(new TipoVeiculo(1L, "Carro", new BigDecimal("10.00"))));

            assertThat(tipoVeiculoService.getEdicao(1L))
                    .isEqualTo(new TipoVeiculoSaveDto(1L, "Carro", new BigDecimal("10.00")));
        }

        @Test
        @DisplayName("getById lança ResourceNotFoundException para id inexistente")
        void getByIdDeveFalharParaIdInexistente() {
            when(tipoVeiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tipoVeiculoService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Tipo de veículo não encontrado com ID: 99");
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("cadastra um tipo novo quando o id é nulo")
        void deveCadastrarTipoNovo() {
            when(tipoVeiculoRepository.save(any(TipoVeiculo.class))).thenAnswer(inv -> {
                TipoVeiculo t = inv.getArgument(0);
                t.setId(3L);
                return t;
            });

            Long id = tipoVeiculoService.save(new TipoVeiculoSaveDto(null, "Caminhonete", new BigDecimal("15.00")));

            assertThat(id).isEqualTo(3L);
            ArgumentCaptor<TipoVeiculo> captor = ArgumentCaptor.forClass(TipoVeiculo.class);
            verify(tipoVeiculoRepository).save(captor.capture());
            assertThat(captor.getValue().getDescricao()).isEqualTo("Caminhonete");
            assertThat(captor.getValue().getValorHora()).isEqualByComparingTo("15.00");
            verify(tipoVeiculoRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("atualiza a descrição e o valor de um tipo existente")
        void deveAtualizarTipoExistente() {
            TipoVeiculo existente = new TipoVeiculo(1L, "Carro", new BigDecimal("10.00"));
            when(tipoVeiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(tipoVeiculoRepository.save(existente)).thenReturn(existente);

            Long id = tipoVeiculoService.save(new TipoVeiculoSaveDto(1L, "Carro grande", new BigDecimal("12.50")));

            assertThat(id).isEqualTo(1L);
            assertThat(existente.getDescricao()).isEqualTo("Carro grande");
            assertThat(existente.getValorHora()).isEqualByComparingTo("12.50");
        }

        @Test
        @DisplayName("lança ResourceNotFoundException ao atualizar tipo inexistente")
        void naoDeveAtualizarTipoInexistente() {
            when(tipoVeiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tipoVeiculoService.save(
                    new TipoVeiculoSaveDto(99L, "Carro", new BigDecimal("10.00"))))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(tipoVeiculoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("exclui o tipo de veículo encontrado")
        void deveExcluirTipo() {
            TipoVeiculo carro = new TipoVeiculo(1L, "Carro", new BigDecimal("10.00"));
            when(tipoVeiculoRepository.findById(1L)).thenReturn(Optional.of(carro));

            tipoVeiculoService.delete(1L);

            verify(tipoVeiculoRepository).delete(carro);
        }

        @Test
        @DisplayName("lança ResourceNotFoundException e não exclui nada quando o id não existe")
        void naoDeveExcluirTipoInexistente() {
            when(tipoVeiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tipoVeiculoService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(tipoVeiculoRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("validarTipoveiculoExistente")
    class ValidarTipoExistente {

        @Test
        @DisplayName("não lança exceção quando o tipo existe")
        void deveAceitarTipoExistente() {
            when(tipoVeiculoRepository.existsById(1L)).thenReturn(true);

            assertThatCode(() -> tipoVeiculoService.validarTipoveiculoExistente(1L)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lança ResourceNotFoundException quando o tipo não existe")
        void deveRejeitarTipoInexistente() {
            when(tipoVeiculoRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> tipoVeiculoService.validarTipoveiculoExistente(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
