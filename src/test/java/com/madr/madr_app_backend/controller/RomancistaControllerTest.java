package com.madr.madr_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.exception.GlobalExceptionHandler;
import com.madr.madr_app_backend.exception.RomancistaConflictException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import com.madr.madr_app_backend.service.RomancistaService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RomancistaController.class)
@Import(GlobalExceptionHandler.class)
public class RomancistaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RomancistaService service;

    @Test
    @DisplayName("POST /romancista - Deve retornar 201 Created quando o payload for válido")
    void deveCriarRomancistaComSucesso() throws Exception {
        RomancistRequest request = new RomancistRequest("Machado de Assis");
        RomancistResponse response = new RomancistResponse(1L, "machado de assis");
        when(service.criar(any(RomancistRequest.class))).thenReturn(response);
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/romancista/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("machado de assis"));
    }

    @Test
    @DisplayName("POST /romancista?foo=bar - Deve retornar Location apenas com a URI do recurso criado (sem query params)")
    void deveIgnorarQueryParamsNoHeaderLocationAoCriar() throws Exception {
        RomancistRequest request = new RomancistRequest("Machado de Assis");
        RomancistResponse response = new RomancistResponse(1L, "machado de assis");
        when(service.criar(any(RomancistRequest.class))).thenReturn(response);
        mockMvc.perform(post("/romancista")
                        .queryParam("foo", "bar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/romancista/1"));
    }

    @Test
    @DisplayName("POST /romancista - Deve retornar 400 Bad Request com lista ordenada de erros quando o nome for inválido")
    void deveRetornar400QuandoNomeForInvalido() throws Exception {
        RomancistRequest requestInvalido = new RomancistRequest("");
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields.nome").isArray())
                .andExpect(jsonPath("$.invalidFields.nome.length()").value(2))
                .andExpect(jsonPath("$.invalidFields.nome[0]").value("O nome deve ter entre 2 e 100 caracteres"))
                .andExpect(jsonPath("$.invalidFields.nome[1]").value("O nome é obrigatório"));
    }

    @Test
    @DisplayName("POST /romancista - Deve retornar 400 quando o nome sanitizado ficar menor que 2 caracteres")
    void deveRetornar400QuandoNomeSanitizadoForMuitoCurto() throws Exception {
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  a  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields.nome").isArray())
                .andExpect(jsonPath("$.invalidFields.nome.length()").value(1))
                .andExpect(jsonPath("$.invalidFields.nome[0]").value("O nome deve ter entre 2 e 100 caracteres"));
    }

    @Test
    @DisplayName("POST /romancista - Deve retornar 400 quando o nome tiver apenas pontuação e ficar vazio após sanitizar")
    void deveRetornar400QuandoNomeTiverApenasPontuacao() throws Exception {
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  ???  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields.nome").exists());
    }

    @Test
    @DisplayName("POST /romancista - Deve retornar 409 Conflict quando o autor já existir")
    void deveRetornar409QuandoNomeDuplicado() throws Exception {
        RomancistRequest request = new RomancistRequest("Clarice Lispector");
        when(service.criar(any(RomancistRequest.class)))
                .thenThrow(new RomancistaConflictException("clarice lispector"));
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /romancista/{id} - Deve retornar 200 OK quando o autor existir")
    void deveBuscarPorIdComSucesso() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(new RomancistResponse(1L, "machado de assis"));
        mockMvc.perform(get("/romancista/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("machado de assis"));
    }

    @Test
    @DisplayName("GET /romancista/{id} - Deve retornar 404 Not Found quando o autor não existir")
    void deveRetornar404QuandoBuscarIdInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RomancistaNotFoundException(99L));
        mockMvc.perform(get("/romancista/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Romancista não encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /romancista - Deve retornar 200 OK com a lista de autores")
    void deveListarRomancistas() throws Exception {
        when(service.listar(null)).thenReturn(List.of(
                new RomancistResponse(1L, "machado de assis"),
                new RomancistResponse(2L, "clarice lispector")
        ));
        mockMvc.perform(get("/romancista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("machado de assis"))
                .andExpect(jsonPath("$[1].nome").value("clarice lispector"));
    }

    @Test
    @DisplayName("GET /romancista?nome= - Deve retornar 200 OK com a lista filtrada por nome")
    void deveListarRomancistasFiltrandoPorNome() throws Exception {
        when(service.listar("machado")).thenReturn(List.of(
                new RomancistResponse(1L, "machado de assis")
        ));
        mockMvc.perform(get("/romancista").param("nome", "machado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("machado de assis"));
    }

    @Test
    @DisplayName("PATCH /romancista/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void deveAtualizarComSucesso() throws Exception {
        RomancistRequest request = new RomancistRequest("Machado Atualizado");
        when(service.atualizar(eq(1L), any(RomancistRequest.class)))
                .thenReturn(new RomancistResponse(1L, "machado atualizado"));
        mockMvc.perform(patch("/romancista/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("machado atualizado"));
    }
    @Test
    @DisplayName("PATCH /romancista/{id} - Deve retornar 404 Not Found se tentar atualizar id inexistente")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        RomancistRequest request = new RomancistRequest("Autor Qualquer");
        when(service.atualizar(eq(99L), any(RomancistRequest.class)))
                .thenThrow(new RomancistaNotFoundException(99L));
        mockMvc.perform(patch("/romancista/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /romancista/{id} - Deve retornar 409 Conflict quando o novo nome já estiver em uso")
    void deveRetornar409AoAtualizarComNomeDuplicado() throws Exception {
        RomancistRequest request = new RomancistRequest("Clarice Lispector");
        when(service.atualizar(eq(1L), any(RomancistRequest.class)))
                .thenThrow(new RomancistaConflictException("clarice lispector"));
        mockMvc.perform(patch("/romancista/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE /romancista/{id} - Deve retornar 204 No Content ao excluir")
    void deveExcluirComSucesso() throws Exception {
        mockMvc.perform(delete("/romancista/1"))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("DELETE /romancista/{id} - Deve retornar 404 Not Found se tentar excluir inexistente")
    void deveRetornar404AoExcluirInexistente() throws Exception {
        doThrow(new RomancistaNotFoundException(99L)).when(service).excluir(99L);
        mockMvc.perform(delete("/romancista/99"))
                .andExpect(status().isNotFound());
    }
}
