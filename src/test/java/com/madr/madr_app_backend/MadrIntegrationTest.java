package com.madr.madr_app_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madr.madr_app_backend.dto.LivroPatchRequest;
import com.madr.madr_app_backend.dto.LivroRequest;
import com.madr.madr_app_backend.dto.RomancistRequest;
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
class MadrIntegrationTest {

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
    @DisplayName("Fluxo completo: criar romancista -> criar livro -> buscar livro com filtro parcial e ano")
    void deveCriarRomancistaCriarLivroEBuscarComFiltro() throws Exception {
        MvcResult romancistaResult = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RomancistRequest("  Machado de Assis  "))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("machado de assis"))
                .andReturn();

        Long romancistaId = extractId(romancistaResult);

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroRequest("  Dom Casmurro?  ", 1899, romancistaId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("dom casmurro"))
                .andExpect(jsonPath("$.ano").value(1899))
                .andExpect(jsonPath("$.romancistaId").value(romancistaId));

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroRequest("Memórias Póstumas de Brás Cubas", 1881, romancistaId))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/livro")
                        .param("titulo", "  CASMURRO ")
                        .param("ano", "1899"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("dom casmurro"))
                .andExpect(jsonPath("$.content[0].romancistaId").value(romancistaId));
    }

    @Test
    @DisplayName("POST /livro - Deve retornar 409 em título duplicado (após sanitização) e 404 para romancista inexistente")
    void deveValidarConflitoDeTituloERomancistaInexistente() throws Exception {
        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroRequest("Dom Casmurro", 1899, 999L))))
                .andExpect(status().isNotFound());

        MvcResult romancistaResult = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RomancistRequest("Clarice Lispector"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long romancistaId = extractId(romancistaResult);

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroRequest("A Hora da Estrela", 1977, romancistaId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroRequest("  a   HORA  da  estrela? ", 1977, romancistaId))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH e DELETE /livro/{id} - Deve atualizar parcialmente, impedir excluir romancista com livro e excluir livro")
    void deveAtualizarEExcluirLivroEndToEnd() throws Exception {
        MvcResult romancistaResult = mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RomancistRequest("Jorge Amado"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long romancistaId = extractId(romancistaResult);

        MvcResult livroResult = mockMvc.perform(post("/livro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroRequest("Capitães da Areia", 1937, romancistaId))))
                .andExpect(status().isCreated())
                .andReturn();

        Long livroId = extractId(livroResult);

        mockMvc.perform(patch("/livro/{id}", livroId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LivroPatchRequest("  Capitães da Areia Edição Comemorativa  ", 1938, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("capitães da areia edição comemorativa"))
                .andExpect(jsonPath("$.ano").value(1938))
                .andExpect(jsonPath("$.romancistaId").value(romancistaId));

        mockMvc.perform(delete("/romancista/{id}", romancistaId))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/livro/{id}", livroId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/livro/{id}", livroId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/romancista/{id}", romancistaId))
                .andExpect(status().isNoContent());
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
