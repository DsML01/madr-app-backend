package com.madr.madr_app_backend.service;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.exception.RomancistaConflictException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import com.madr.madr_app_backend.mapper.RomancistaMapper;
import com.madr.madr_app_backend.model.Romancista;
import com.madr.madr_app_backend.repository.RomancistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Random.class)
class RomancistaServiceTest {

    @Mock
    private RomancistaRepository repository;

    private final RomancistaMapper mapper = Mappers.getMapper(RomancistaMapper.class);

    private RomancistaService service;

    @BeforeEach
    void setUp() {
        service = new RomancistaService(repository, mapper);
    }

    @Test
    @DisplayName("Deve criar romancista sanitizando o nome e salvando no repositório")
    void deveCriarRomancistaSanitizandoNome() {
        when(repository.existsByNome("machado de assis")).thenReturn(false);
        when(repository.save(any(Romancista.class)))
                .thenAnswer(invocation -> {
                    Romancista salvo = invocation.getArgument(0);
                    return new Romancista(1L, salvo.getNome());
                });

        RomancistResponse response = service.criar(new RomancistRequest("  Machado    de  Assis?  "));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("machado de assis");
        verify(repository).existsByNome("machado de assis");
        verify(repository).save(any(Romancista.class));
    }

    @Test
    @DisplayName("Deve lançar conflito ao tentar cadastrar romancista com nome já existente")
    void deveLancarConflitoAoCriarDuplicado() {
        when(repository.existsByNome("clarice lispector")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new RomancistRequest("  CLARICE LISPECTOR  ")))
                .isInstanceOf(RomancistaConflictException.class);
    }

    @Test
    @DisplayName("Deve buscar romancista por ID e lançar exceção quando não existir")
    void deveBuscarPorIdELancarExcecaoQuandoInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Romancista(1L, "machado de assis")));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RomancistResponse encontrado = service.buscarPorId(1L);
        assertThat(encontrado.nome()).isEqualTo("machado de assis");

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RomancistaNotFoundException.class);
    }

    @Test
    @DisplayName("Deve listar ordenando por ID quando filtro for nulo/vazio e usar filtro sanitizado quando informado")
    void deveListarComESemFiltro() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                new Romancista(1L, "machado de assis"),
                new Romancista(2L, "clarice lispector")
        ));
        when(repository.findByNomeContainingIgnoreCase("machado", Pageable.unpaged(Sort.by("id"))))
                .thenReturn(new PageImpl<>(List.of(new Romancista(1L, "machado de assis"))));

        assertThat(service.listar(null)).hasSize(2);
        assertThat(service.listar("   ")).hasSize(2);

        List<RomancistResponse> filtrados = service.listar("  MACHADO  ");
        assertThat(filtrados).hasSize(1);
        assertThat(filtrados.getFirst().nome()).isEqualTo("machado de assis");
    }

    @Test
    @DisplayName("Deve atualizar romancista sanitizando o novo nome e checando conflito em outros IDs")
    void deveAtualizarRomancista() {
        Romancista existente = new Romancista(1L, "machado de assis");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeAndIdNot("joaquim maria machado", 1L)).thenReturn(false);
        when(repository.save(any(Romancista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RomancistResponse atualizado = service.atualizar(1L, new RomancistRequest("  Joaquim Maria Machado  "));

        assertThat(atualizado.id()).isEqualTo(1L);
        assertThat(atualizado.nome()).isEqualTo("joaquim maria machado");
    }

    @Test
    @DisplayName("Deve lançar NotFound ou Conflict ao atualizar romancista")
    void deveLancarErroAoAtualizarInexistenteOuDuplicado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.findById(2L)).thenReturn(Optional.of(new Romancista(2L, "clarice lispector")));
        when(repository.existsByNomeAndIdNot("machado de assis", 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(99L, new RomancistRequest("Novo Nome")))
                .isInstanceOf(RomancistaNotFoundException.class);

        assertThatThrownBy(() -> service.atualizar(2L, new RomancistRequest("  MACHADO DE ASSIS ")))
                .isInstanceOf(RomancistaConflictException.class);
    }

    @Test
    @DisplayName("Deve excluir romancista existente e lançar NotFound para ID inexistente")
    void deveExcluirELancarExcecaoQuandoInexistente() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.existsById(99L)).thenReturn(false);

        service.excluir(1L);
        verify(repository).deleteById(1L);

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RomancistaNotFoundException.class);
    }
}
