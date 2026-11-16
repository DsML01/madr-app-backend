package com.madr.madr_app_backend.service;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.exception.RomancistaConflictException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.Random.class)
class RomancistaServiceTest {

    private RomancistaService service;

    @BeforeEach
    void setUp() {
        service = new RomancistaService();
    }

    @Test
    @DisplayName("Deve criar romancista sanitizando o nome e gerando ID sequencial")
    void deveCriarRomancistaSanitizandoNome() {
        RomancistResponse response = service.criar(new RomancistRequest("  Machado    de  Assis?  "));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("machado de assis");
    }

    @Test
    @DisplayName("Deve lançar conflito ao tentar cadastrar romancista duplicado ignorando caixa e espaços")
    void deveLancarConflitoAoCriarDuplicado() {
        service.criar(new RomancistRequest("Clarice Lispector"));

        assertThatThrownBy(() -> service.criar(new RomancistRequest("  CLARICE LISPECTOR  ")))
                .isInstanceOf(RomancistaConflictException.class);
    }

    @Test
    @DisplayName("Deve buscar romancista por ID e lançar exceção quando não existir")
    void deveBuscarPorIdELancarExcecaoQuandoInexistente() {
        RomancistResponse criado = service.criar(new RomancistRequest("Machado de Assis"));

        RomancistResponse encontrado = service.buscarPorId(criado.id());
        assertThat(encontrado.nome()).isEqualTo("machado de assis");

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RomancistaNotFoundException.class);
    }

    @Test
    @DisplayName("Deve listar todos ordenados por ID quando filtro for nulo ou vazio e filtrar por nome parcial sanitizado")
    void deveListarComESemFiltro() {
        service.criar(new RomancistRequest("Machado de Assis"));
        service.criar(new RomancistRequest("Clarice Lispector"));

        assertThat(service.listar(null)).hasSize(2);
        assertThat(service.listar("   ")).hasSize(2);

        List<RomancistResponse> filtrados = service.listar("  MACHADO  ");
        assertThat(filtrados).hasSize(1);
        assertThat(filtrados.getFirst().nome()).isEqualTo("machado de assis");
    }

    @Test
    @DisplayName("Deve garantir ordenação crescente por ID ao listar múltiplos romancistas")
    void deveListarOrdenadoPorId() {
        for (int i = 1; i <= 20; i++) {
            service.criar(new RomancistRequest("Autor " + i));
        }

        List<Long> ids = service.listar(null).stream()
                .map(RomancistResponse::id)
                .toList();

        assertThat(ids).isSorted();
        assertThat(ids).hasSize(20);
    }

    @Test
    @DisplayName("Deve atualizar romancista sanitizando o novo nome e permitir manter o mesmo nome")
    void deveAtualizarEPermitirMesmoNome() {
        RomancistResponse criado = service.criar(new RomancistRequest("Machado de Assis"));

        RomancistResponse mesmoNome = service.atualizar(criado.id(), new RomancistRequest("  MACHADO DE ASSIS  "));
        assertThat(mesmoNome.nome()).isEqualTo("machado de assis");

        RomancistResponse atualizado = service.atualizar(criado.id(), new RomancistRequest("  Joaquim Maria Machado  "));
        assertThat(atualizado.nome()).isEqualTo("joaquim maria machado");
    }

    @Test
    @DisplayName("Deve lançar NotFound ou Conflict ao atualizar romancista")
    void deveLancarErroAoAtualizarInexistenteOuDuplicado() {
        service.criar(new RomancistRequest("Machado de Assis"));
        RomancistResponse segundo = service.criar(new RomancistRequest("Clarice Lispector"));

        assertThatThrownBy(() -> service.atualizar(99L, new RomancistRequest("Novo Nome")))
                .isInstanceOf(RomancistaNotFoundException.class);

        assertThatThrownBy(() -> service.atualizar(segundo.id(), new RomancistRequest("  MACHADO DE ASSIS ")))
                .isInstanceOf(RomancistaConflictException.class);
    }

    @Test
    @DisplayName("Deve excluir romancista existente e lançar NotFound para ID inexistente")
    void deveExcluirELancarExcecaoQuandoInexistente() {
        RomancistResponse criado = service.criar(new RomancistRequest("Jorge Amado"));

        service.excluir(criado.id());

        assertThatThrownBy(() -> service.buscarPorId(criado.id()))
                .isInstanceOf(RomancistaNotFoundException.class);
        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RomancistaNotFoundException.class);
    }
}
