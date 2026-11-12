package com.madr.madr_app_backend.controller;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.service.RomancistaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/romancistas")
@Tag(name = "Romancistas", description = "Endpoints para gerenciamento do acervo de romancistas")
public class RomancistaController {

    private final RomancistaService service;

    public RomancistaController(RomancistaService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um novo romancista", description = "Salva o nome sanitizado em minusculas e sem espaços excedentes.")
    @ApiResponses({
            @ApiResponse(responseCode =  "201", description = "Romancista cadastrado com sucesso"),
            @ApiResponse(responseCode =  "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode =  "409", description = "Romancista já cadastrado com esse nome")
    })
    @PostMapping
    public ResponseEntity<RomancistResponse> criar(@RequestBody @Valid RomancistRequest request) {
        RomancistResponse criado = service.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @Operation(summary = "Busca romancista por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Romancista encontrado"),
            @ApiResponse(responseCode = "404", description = "Romancista não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RomancistResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Lista romancistas com suporte a filtro por nome parcial")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<RomancistResponse>> listar(@RequestParam(required = false) String nome) {
        return ResponseEntity.ok(service.listar(nome));
    }

    @Operation(summary = "Atualiza os dados de um romancista")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Romancista atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Romancista não encontrado"),
            @ApiResponse(responseCode = "409", description = "Nome já em uso por outro romancista")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<RomancistResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid RomancistRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @Operation(summary = "Exclui um romancista do acervo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Romancista excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Romancista não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
