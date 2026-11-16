package com.madr.madr_app_backend.service;

import com.madr.madr_app_backend.dto.LivroPatchRequest;
import com.madr.madr_app_backend.dto.LivroRequest;
import com.madr.madr_app_backend.dto.LivroResponse;
import com.madr.madr_app_backend.exception.LivroConflictException;
import com.madr.madr_app_backend.exception.LivroNotFoundException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import com.madr.madr_app_backend.mapper.LivroMapper;
import com.madr.madr_app_backend.model.Livro;
import com.madr.madr_app_backend.model.Romancista;
import com.madr.madr_app_backend.repository.LivroRepository;
import com.madr.madr_app_backend.repository.RomancistaRepository;
import com.madr.madr_app_backend.util.StringSanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

@Service
public class LivroService {
    private final LivroRepository livroRepository;
    private final RomancistaRepository romancistaRepository;
    private final LivroMapper livroMapper;

    public LivroService(
            LivroRepository livroRepository,
            RomancistaRepository romancistaRepository,
            LivroMapper livroMapper
    ) {
        this.livroRepository = livroRepository;
        this.romancistaRepository = romancistaRepository;
        this.livroMapper = livroMapper;
    }

    @Transactional
    public LivroResponse criar(LivroRequest request) {
        String tituloSanitizado = StringSanitizer.sanitize(request.titulo());

        if(livroRepository.existsByTitulo(tituloSanitizado)) {
            throw new LivroConflictException(tituloSanitizado);
        }

        Romancista romancista = romancistaRepository.findById(request.romancistaId())
                .orElseThrow(() -> new RomancistaNotFoundException(request.romancistaId()));

        Livro livro = livroMapper.toEntity(request);
        livro.setTitulo(tituloSanitizado);
        livro.setRomancista(romancista);

        Livro salvo = livroRepository.save(livro);
        return livroMapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public LivroResponse buscarPorId(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        return livroMapper.toResponse(livro);
    }

    @Transactional(readOnly = true)
    public Page<LivroResponse> listar(String titulo, Integer ano, Pageable pageable) {
        Specification<Livro> spec = (root, query, cb) -> cb.conjunction();

        if(titulo != null && !titulo.isBlank()) {
            String filtroSanitizado = StringSanitizer.sanitize(titulo);
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("titulo")),
                    "%" + filtroSanitizado + "%"));
        }

        if (ano != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ano"), ano));
        }

        return livroRepository.findAll(spec, pageable).map(livroMapper::toResponse);
    }

    @Transactional
    public LivroResponse atualizar(Long id, LivroPatchRequest request) {
        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        if (request.titulo() != null && !request.titulo().isBlank()) {
            String novoTitulo = StringSanitizer.sanitize(request.titulo());
            if(livroRepository.existsByTituloAndIdNot(novoTitulo, id)) {
                throw new LivroConflictException(novoTitulo);
            }
            existente.setTitulo(novoTitulo);
        }

        if(request.ano() != null) {
            existente.setAno(request.ano());
        }

        if(request.romancistaId() != null) {
            Romancista novoRomancista = romancistaRepository.findById(request.romancistaId())
                    .orElseThrow(() -> new RomancistaNotFoundException(request.romancistaId()));
            existente.setRomancista(novoRomancista);
        }

        return livroMapper.toResponse(livroRepository.save(existente));
    }

    @Transactional
    public void excluir(Long id) {
        if(!livroRepository.existsById(id)) {
            throw new LivroNotFoundException(id);
        }
        livroRepository.deleteById(id);
    }
}
