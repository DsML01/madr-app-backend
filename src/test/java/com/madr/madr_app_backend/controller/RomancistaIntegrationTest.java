package com.madr.madr_app_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madr.madr_app_backend.repository.LivroRepository;
import com.madr.madr_app_backend.repository.RomancistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.Random.class)
class RomancistaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private RomancistaRepository romancistaRepository;

    @BeforeEach
    void limparBanco() {
        livroRepository.deleteAllInBatch();
        romancistaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("POST /romancista - Deve cadastrar e depois retornar 409 ao repetir nome com outra caixa e espaços")
    void deveRetornar409AoCadastrarDuplicadoComOutraCaixaEEspacos() throws Exception {
        MvcResult criado = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Machado de Assis"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("machado de assis"))
                .andReturn();

        Long id = extractId(criado);

        mockMvc.perform(get("/romancista/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  MACHADO   de  ASSIS?  "}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /romancista - Deve validar tamanho após sanitização e devolver array ordenado em invalidFields")
    void deveValidarTamanhoAposSanitizacaoEOrdenarErros() throws Exception {
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  a  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidFields.nome.length()").value(1))
                .andExpect(jsonPath("$.invalidFields.nome[0]").value("O nome deve ter entre 2 e 100 caracteres"));

        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidFields.nome.length()").value(2))
                .andExpect(jsonPath("$.invalidFields.nome[0]").value("O nome deve ter entre 2 e 100 caracteres"))
                .andExpect(jsonPath("$.invalidFields.nome[1]").value("O nome é obrigatório"));
    }

    @Test
    @DisplayName("Fluxo completo: criar -> buscar por id -> listar com filtro -> atualizar -> excluir -> confirmar 404")
    void deveExecutarCicloCompletoSemMocks() throws Exception {
        MvcResult criado = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  Clarice Lispector  "}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = extractId(criado);

        mockMvc.perform(get("/romancista/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("clarice lispector"));

        mockMvc.perform(get("/romancista").param("nome", "  CLARICE "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("clarice lispector"));

        mockMvc.perform(patch("/romancista/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Clarice Atualizada"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("clarice atualizada"));

        mockMvc.perform(delete("/romancista/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/romancista/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /romancista/{id} - Deve retornar 409 ao tentar atualizar para nome já usado por outro romancista")
    void deveRetornar409NoPatchParaNomeDeOutroRomancista() throws Exception {
        MvcResult primeiro = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Machado de Assis"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult segundo = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Jorge Amado"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Long id1 = extractId(primeiro);
        Long id2 = extractId(segundo);

        mockMvc.perform(patch("/romancista/{id}", id2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  MACHADO DE ASSIS  "}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/romancista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[1].id").value(id2));
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
