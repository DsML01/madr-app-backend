package com.madr.madr_app_backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.Random.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RomancistaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /romancista - Deve cadastrar e depois retornar 409 ao repetir nome com outra caixa e espaços")
    void deveRetornar409AoCadastrarDuplicadoComOutraCaixaEEspacos() throws Exception {
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Machado de Assis"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/romancista/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("machado de assis"));

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
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  Clarice Lispector  "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/romancista/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("clarice lispector"));

        mockMvc.perform(get("/romancista").param("nome", "  CLARICE "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("clarice lispector"));

        mockMvc.perform(patch("/romancista/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Clarice Atualizada"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("clarice atualizada"));

        mockMvc.perform(delete("/romancista/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/romancista/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /romancista/{id} - Deve retornar 409 ao tentar atualizar para nome já usado por outro romancista")
    void deveRetornar409NoPatchParaNomeDeOutroRomancista() throws Exception {
        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Machado de Assis"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/romancista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Jorge Amado"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/romancista/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  MACHADO DE ASSIS  "}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/romancista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}
