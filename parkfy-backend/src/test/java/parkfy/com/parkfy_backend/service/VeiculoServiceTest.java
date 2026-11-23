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
import parkfy.com.parkfy_backend.dto.tipoveiculo.VeiculoSaveDto;
import parkfy.com.parkfy_backend.infra.exception.ResourceAlreadyExistsException;
import parkfy.com.parkfy_backend.infra.exception.ResourceNotFoundException;
import parkfy.com.parkfy_backend.model.TipoVeiculo;
import parkfy.com.parkfy_backend.model.Veiculo;
import parkfy.com.parkfy_backend.repository.VeiculoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoService")
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private TipoVeiculoService tipoVeiculoService;

    @InjectMocks
    private VeiculoService veiculoService;

    private final TipoVeiculo carro = new TipoVeiculo(1L, "Carro", new BigDecimal("10.00"));

    private void simularSaveComId(long id) {
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> {
            Veiculo v = inv.getArgument(0);
            v.setId(id);
            return v;
        });
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("cadastra um veículo novo com os dados e o tipo informados")
        void deveCadastrarVeiculoNovo() {
            when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(false);
            when(tipoVeiculoService.getById(1L)).thenReturn(carro);
            simularSaveComId(10L);

            Long id = veiculoService.save(new VeiculoSaveDto(null, "ABC1D23", "Gol", "Prata", 1L));

            assertThat(id).isEqualTo(10L);
            ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
            verify(veiculoRepository).save(captor.capture());
            Veiculo salvo = captor.getValue();
            assertThat(salvo.getPlaca()).isEqualTo("ABC1D23");
            assertThat(salvo.getModelo()).isEqualTo("Gol");
            assertThat(salvo.getCor()).isEqualTo("Prata");
            assertThat(salvo.getTipoVeiculo()).isSameAs(carro);
            verify(veiculoRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("remove espaços das pontas da placa, mas mantém maiúsculas/minúsculas como digitadas")
        void deveManterPlacaComoDigitada() {
            when(veiculoRepository.existsByPlacaIgnoreCase("abc1D23")).thenReturn(false);
            when(tipoVeiculoService.getById(1L)).thenReturn(carro);
            simularSaveComId(10L);

            veiculoService.save(new VeiculoSaveDto(null, "  abc1D23  ", null, null, 1L));

            ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
            verify(veiculoRepository).save(captor.capture());
            assertThat(captor.getValue().getPlaca()).isEqualTo("abc1D23");
        }

        @Test
        @DisplayName("trata id zero como cadastro novo")
        void deveTratarIdZeroComoNovo() {
            when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(false);
            when(tipoVeiculoService.getById(1L)).thenReturn(carro);
            simularSaveComId(10L);

            veiculoService.save(new VeiculoSaveDto(0L, "ABC1D23", null, null, 1L));

            verify(veiculoRepository, never()).findById(anyLong());
            verify(veiculoRepository, never()).existsByPlacaIgnoreCaseAndIdNot(anyString(), anyLong());
        }

        @Test
        @DisplayName("lança ResourceAlreadyExistsException ao cadastrar placa já existente")
        void naoDeveCadastrarPlacaDuplicada() {
            when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(true);

            assertThatThrownBy(() -> veiculoService.save(new VeiculoSaveDto(null, "ABC1D23", null, null, 1L)))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("ABC1D23");

            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("atualiza um veículo existente, ignorando o próprio registro na validação da placa")
        void deveAtualizarVeiculoExistente() {
            Veiculo existente = new Veiculo(10L, "ABC1D23", "Gol", "Prata", carro);
            TipoVeiculo moto = new TipoVeiculo(2L, "Moto", new BigDecimal("5.00"));
            when(veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("XYZ9A87", 10L)).thenReturn(false);
            when(veiculoRepository.findById(10L)).thenReturn(Optional.of(existente));
            when(tipoVeiculoService.getById(2L)).thenReturn(moto);
            when(veiculoRepository.save(existente)).thenReturn(existente);

            Long id = veiculoService.save(new VeiculoSaveDto(10L, "XYZ9A87", "CG 160", "Preta", 2L));

            assertThat(id).isEqualTo(10L);
            assertThat(existente.getPlaca()).isEqualTo("XYZ9A87");
            assertThat(existente.getModelo()).isEqualTo("CG 160");
            assertThat(existente.getCor()).isEqualTo("Preta");
            assertThat(existente.getTipoVeiculo()).isSameAs(moto);
            verify(veiculoRepository, never()).existsByPlacaIgnoreCase(anyString());
        }

        @Test
        @DisplayName("lança ResourceAlreadyExistsException ao atualizar para placa usada por outro veículo")
        void naoDeveAtualizarParaPlacaDeOutroVeiculo() {
            when(veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("XYZ9A87", 10L)).thenReturn(true);

            assertThatThrownBy(() -> veiculoService.save(new VeiculoSaveDto(10L, "XYZ9A87", null, null, 1L)))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("lança ResourceNotFoundException ao atualizar veículo inexistente")
        void naoDeveAtualizarVeiculoInexistente() {
            when(veiculoRepository.existsByPlacaIgnoreCaseAndIdNot("ABC1D23", 99L)).thenReturn(false);
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> veiculoService.save(new VeiculoSaveDto(99L, "ABC1D23", null, null, 1L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Veículo não encontrado com ID: 99");

            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("lança ResourceNotFoundException quando o tipo de veículo não existe")
        void naoDeveSalvarComTipoInexistente() {
            when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(false);
            when(tipoVeiculoService.getById(99L)).thenThrow(new ResourceNotFoundException("Tipo de veículo", 99L));

            assertThatThrownBy(() -> veiculoService.save(new VeiculoSaveDto(null, "ABC1D23", null, null, 99L)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(veiculoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("consultas")
    class Consultas {

        @Test
        @DisplayName("findAllPageable converte os veículos em DTOs e mantém a paginação")
        void deveListarPaginado() {
            Veiculo veiculo = new Veiculo(10L, "ABC1D23", "Gol", "Prata", carro);
            FilterPageable filtro = new FilterPageable(1, 5, Sort.Direction.ASC, "placa");
            Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "placa"));
            when(veiculoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(veiculo), pageable, 6));

            ResponsePagedCommom<VeiculoSaveDto> resposta = veiculoService.findAllPageable(filtro);

            assertThat(resposta.getData()).containsExactly(new VeiculoSaveDto(10L, "ABC1D23", "Gol", "Prata", 1L));
            assertThat(resposta.getTotalRecords()).isEqualTo(6L);
            assertThat(resposta.getTotalPages()).isEqualTo(2);
            assertThat(resposta.getPageSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("getEdicao retorna o DTO do veículo")
        void deveRetornarDadosParaEdicao() {
            when(veiculoRepository.findById(10L))
                    .thenReturn(Optional.of(new Veiculo(10L, "ABC1D23", "Gol", "Prata", carro)));

            assertThat(veiculoService.getEdicao(10L))
                    .isEqualTo(new VeiculoSaveDto(10L, "ABC1D23", "Gol", "Prata", 1L));
        }

        @Test
        @DisplayName("getEdicao lança ResourceNotFoundException para id inexistente")
        void getEdicaoDeveFalharParaIdInexistente() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> veiculoService.getEdicao(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findByPlaca remove espaços e retorna o DTO quando a placa existe")
        void deveEncontrarPorPlaca() {
            when(veiculoRepository.findByPlacaIgnoreCase("ABC1D23"))
                    .thenReturn(Optional.of(new Veiculo(10L, "ABC1D23", "Gol", "Prata", carro)));

            assertThat(veiculoService.findByPlaca(" ABC1D23 "))
                    .contains(new VeiculoSaveDto(10L, "ABC1D23", "Gol", "Prata", 1L));
        }

        @Test
        @DisplayName("findByPlaca retorna vazio quando a placa não está cadastrada")
        void findByPlacaDeveRetornarVazio() {
            when(veiculoRepository.findByPlacaIgnoreCase("ABC1D23")).thenReturn(Optional.empty());

            assertThat(veiculoService.findByPlaca("ABC1D23")).isEmpty();
        }

        @Test
        @DisplayName("getVeiculoByPlaca retorna null quando a placa não está cadastrada")
        void getVeiculoByPlacaDeveRetornarNull() {
            when(veiculoRepository.findByPlacaIgnoreCase("ABC1D23")).thenReturn(Optional.empty());

            assertThat(veiculoService.getVeiculoByPlaca("ABC1D23")).isNull();
        }

        @Test
        @DisplayName("getVeiculoByPlaca retorna a entidade quando a placa existe")
        void getVeiculoByPlacaDeveRetornarEntidade() {
            Veiculo veiculo = new Veiculo(10L, "ABC1D23", "Gol", "Prata", carro);
            when(veiculoRepository.findByPlacaIgnoreCase("ABC1D23")).thenReturn(Optional.of(veiculo));

            assertThat(veiculoService.getVeiculoByPlaca("ABC1D23 ")).isSameAs(veiculo);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("exclui o veículo encontrado")
        void deveExcluirVeiculo() {
            Veiculo veiculo = new Veiculo(10L, "ABC1D23", "Gol", "Prata", carro);
            when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

            veiculoService.delete(10L);

            verify(veiculoRepository).delete(veiculo);
        }

        @Test
        @DisplayName("lança ResourceNotFoundException e não exclui nada quando o id não existe")
        void naoDeveExcluirVeiculoInexistente() {
            when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> veiculoService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(veiculoRepository, never()).delete(any());
        }
    }
}
