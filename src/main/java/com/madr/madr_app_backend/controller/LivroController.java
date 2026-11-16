package com.madr.madr_app_backend.controller;

import com.madr.madr_app_backend.dto.LivroPatchRequest;
import com.madr.madr_app_backend.dto.LivroRequest;
import com.madr.madr_app_backend.dto.LivroResponse;
import com.madr.madr_app_backend.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/livro")
@Tag(name = "Livros", description = "Endpoints para gerenciamento do acervo de livros")
public class LivroController {
    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um novo livro", description = "Salva o título sanitizado e vincula a um romancista existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Romancista informado não encontrado"),
            @ApiResponse(responseCode = "409", description = "Já existe um livro cadastrado com esse título")
    })
    @PostMapping
    public ResponseEntity<LivroResponse> criar(@RequestBody @Valid LivroRequest request) {
        LivroResponse criado = service.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @Operation(summary = "Busca livro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LivroResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Lista livros com filtro opcional por título e ano (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de livros retornada com sucesso")
    @GetMapping
    public ResponseEntity<Page<LivroResponse>> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Integer ano,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listar(titulo, ano, pageable));
    }

    @Operation(summary = "Atualiza parcialmente os dados de um livro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Livro ou romancista não encontrado"),
            @ApiResponse(responseCode = "409", description = "Título já em uso por outro livro")
    })

    @PatchMapping("/{id}")
    public ResponseEntity<LivroResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid LivroPatchRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @Operation(summary = "Exclui um livro do acervo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
