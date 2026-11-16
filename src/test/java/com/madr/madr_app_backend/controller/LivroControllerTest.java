package com.madr.madr_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madr.madr_app_backend.dto.LivroPatchRequest;
import com.madr.madr_app_backend.dto.LivroRequest;
import com.madr.madr_app_backend.dto.LivroResponse;
import com.madr.madr_app_backend.exception.GlobalExceptionHandler;
import com.madr.madr_app_backend.exception.LivroConflictException;
import com.madr.madr_app_backend.exception.LivroNotFoundException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import com.madr.madr_app_backend.service.LivroService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LivroController.class)
@Import(GlobalExceptionHandler.class)
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LivroService service;

    @Test
    @DisplayName("POST /livro - Deve retornar 201 Created quando o payload for válido")
    void deveCriarLivroComSucesso() throws Exception {
        LivroRequest request = new LivroRequest("Dom Casmurro", 1899, 1L);
        LivroResponse response = new LivroResponse(1L, "dom casmurro", 1899, 1L);

        when(service.criar(any(LivroRequest.class))).thenReturn(response);

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/livro/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("dom casmurro"))
                .andExpect(jsonPath("$.ano").value(1899))
                .andExpect(jsonPath("$.romancistaId").value(1));
    }

    @Test
    @DisplayName("POST /livro?foo=bar - Deve retornar Location apenas com a URI do recurso criado")
    void deveIgnorarQueryParamsNoHeaderLocationAoCriar() throws Exception {
        LivroRequest request = new LivroRequest("Dom Casmurro", 1899, 1L);
        LivroResponse response = new LivroResponse(1L, "dom casmurro", 1899, 1L);

        when(service.criar(any(LivroRequest.class))).thenReturn(response);

        mockMvc.perform(post("/livro")
                        .queryParam("foo", "bar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/livro/1"));
    }

    @Test
    @DisplayName("POST /livro - Deve retornar 400 quando título sanitizado for inválido")
    void deveRetornar400QuandoTituloSanitizadoForInvalido() throws Exception {
        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "  a  ", "ano": 1899, "romancistaId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.invalidFields.titulo").isArray())
                .andExpect(jsonPath("$.invalidFields.titulo[0]").value("O título deve ter entre 2 e 255 caracteres"));
    }

    @Test
    @DisplayName("POST /livro - Deve retornar 404 quando romancista não existir")
    void deveRetornar404QuandoRomancistaNaoExistir() throws Exception {
        LivroRequest request = new LivroRequest("Dom Casmurro", 1899, 99L);
        when(service.criar(any(LivroRequest.class))).thenThrow(new RomancistaNotFoundException(99L));

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Romancista não encontrado"));
    }

    @Test
    @DisplayName("POST /livro - Deve retornar 409 quando título já existir")
    void deveRetornar409QuandoTituloDuplicado() throws Exception {
        LivroRequest request = new LivroRequest("Dom Casmurro", 1899, 1L);
        when(service.criar(any(LivroRequest.class))).thenThrow(new LivroConflictException("dom casmurro"));

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"));
    }

    @Test
    @DisplayName("GET /livro/{id} - Deve retornar 200 OK quando existir e 404 quando não existir")
    void deveBuscarPorIdERetornar404QuandoInexistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(new LivroResponse(1L, "dom casmurro", 1899, 1L));
        when(service.buscarPorId(99L)).thenThrow(new LivroNotFoundException(99L));

        mockMvc.perform(get("/livro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("dom casmurro"));

        mockMvc.perform(get("/livro/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Livro não encontrado"));
    }

    @Test
    @DisplayName("GET /livro - Deve retornar página filtrada por título e ano")
    void deveListarLivrosPaginado() throws Exception {
        LivroResponse livro = new LivroResponse(1L, "dom casmurro", 1899, 1L);
        when(service.listar(eq("casmurro"), eq(1899), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(livro), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/livro")
                        .param("titulo", "casmurro")
                        .param("ano", "1899"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("dom casmurro"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("PATCH /livro/{id} - Deve atualizar parcialmente e tratar 400, 404 e 409")
    void deveAtualizarLivroETratarErros() throws Exception {
        LivroPatchRequest patch = new LivroPatchRequest("Dom Casmurro Edição Especial", null, null);
        LivroResponse response = new LivroResponse(1L, "dom casmurro edição especial", 1899, 1L);

        when(service.atualizar(eq(1L), any(LivroPatchRequest.class))).thenReturn(response);
        when(service.atualizar(eq(2L), any(LivroPatchRequest.class)))
                .thenThrow(new LivroConflictException("dom casmurro edição especial"));
        when(service.atualizar(eq(99L), any(LivroPatchRequest.class)))
                .thenThrow(new LivroNotFoundException(99L));

        mockMvc.perform(patch("/livro/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("dom casmurro edição especial"));

        mockMvc.perform(patch("/livro/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "  a  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidFields.titulo").isArray());

        mockMvc.perform(patch("/livro/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/livro/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /livro/{id} - Deve retornar 204 No Content ao excluir e 404 quando não existir")
    void deveExcluirLivroERetornar404QuandoInexistente() throws Exception {
        doNothing().when(service).excluir(1L);
        doThrow(new LivroNotFoundException(99L)).when(service).excluir(99L);

        mockMvc.perform(delete("/livro/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/livro/99"))
                .andExpect(status().isNotFound());
    }
}
