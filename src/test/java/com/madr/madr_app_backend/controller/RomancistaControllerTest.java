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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
    @DisplayName("POST /romancistas - Deve retornar 201 Created quando o payload for válido")
    void deveCriarRomancistaComSucesso() throws Exception {
        RomancistRequest request = new RomancistRequest("Machado de Assis");
        RomancistResponse response = new RomancistResponse(1L, "machado de assis");
        when(service.criar(any(RomancistRequest.class))).thenReturn(response);
        mockMvc.perform(post("/romancistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/romancistas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("machado de assis"));
    }

    @Test
    @DisplayName("POST /romancistas - Deve retornar 400 Bad Request com ProblemDetail quando o nome for inválido")
    void deveRetornar400QuandoNomeForInvalido() throws Exception {
        RomancistRequest requestInvalido = new RomancistRequest("   "); // em branco
        mockMvc.perform(post("/romancistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields.nome").exists());
    }

    @Test
    @DisplayName("POST /romancistas - Deve retornar 409 Conflict quando o autor já existir")
    void deveRetornar409QuandoNomeDuplicado() throws Exception {
        RomancistRequest request = new RomancistRequest("Clarice Lispector");
        when(service.criar(any(RomancistRequest.class)))
                .thenThrow(new RomancistaConflictException("clarice lispector"));
        mockMvc.perform(post("/romancistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /romancistas/{id} - Deve retornar 200 OK quando o autor existir")
    void deveBuscarPorIdComSucesso() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(new RomancistResponse(1L, "machado de assis"));
        mockMvc.perform(get("/romancistas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("machado de assis"));
    }

    @Test
    @DisplayName("GET /romancistas/{id} - Deve retornar 404 Not Found quando o autor não existir")
    void deveRetornar404QuandoBuscarIdInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RomancistaNotFoundException(99L));
        mockMvc.perform(get("/romancistas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Romancista não encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /romancistas - Deve retornar 200 OK com a lista de autores")
    void deveListarRomancistas() throws Exception {
        when(service.listar(null)).thenReturn(List.of(
                new RomancistResponse(1L, "machado de assis"),
                new RomancistResponse(2L, "clarice lispector")
        ));
        mockMvc.perform(get("/romancistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("machado de assis"))
                .andExpect(jsonPath("$[1].nome").value("clarice lispector"));
    }

    @Test
    @DisplayName("PUT /romancistas/{id} - Deve retornar 200 OK ao atualizar com sucesso")
    void deveAtualizarComSucesso() throws Exception {
        RomancistRequest request = new RomancistRequest("Machado Atualizado");
        when(service.atualizar(eq(1L), any(RomancistRequest.class)))
                .thenReturn(new RomancistResponse(1L, "machado atualizado"));
        mockMvc.perform(put("/romancistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("machado atualizado"));
    }
    @Test
    @DisplayName("PUT /romancistas/{id} - Deve retornar 404 Not Found se tentar atualizar id inexistente")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        RomancistRequest request = new RomancistRequest("Autor Qualquer");
        when(service.atualizar(eq(99L), any(RomancistRequest.class)))
                .thenThrow(new RomancistaNotFoundException(99L));
        mockMvc.perform(put("/romancistas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /romancistas/{id} - Deve retornar 204 No Content ao excluir")
    void deveExcluirComSucesso() throws Exception {
        mockMvc.perform(delete("/romancistas/1"))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("DELETE /romancistas/{id} - Deve retornar 404 Not Found se tentar excluir inexistente")
    void deveRetornar404AoExcluirInexistente() throws Exception {
        doThrow(new RomancistaNotFoundException(99L)).when(service).excluir(99L);
        mockMvc.perform(delete("/romancistas/99"))
                .andExpect(status().isNotFound());
    }
}
