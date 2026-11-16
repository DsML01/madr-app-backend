package com.madr.madr_app_backend.service;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.exception.RomancistaConflictException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import com.madr.madr_app_backend.mapper.RomancistaMapper;
import com.madr.madr_app_backend.model.Romancista;
import com.madr.madr_app_backend.repository.RomancistaRepository;
import com.madr.madr_app_backend.util.StringSanitizer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RomancistaService {

    private final RomancistaRepository repository;
    private final RomancistaMapper mapper;

    public RomancistaService(RomancistaRepository repository, RomancistaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public RomancistResponse criar(RomancistRequest request) {
        String nomeSanitizado = StringSanitizer.sanitize(request.nome());

        if (repository.existsByNome(nomeSanitizado)) {
            throw new RomancistaConflictException(nomeSanitizado);
        }

        Romancista romancista = mapper.toEntity(request);
        romancista.setNome(nomeSanitizado);

        Romancista salvo = repository.save(romancista);
        return mapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public RomancistResponse buscarPorId(Long id) {
        Romancista romancista = repository.findById(id)
                .orElseThrow(() -> new RomancistaNotFoundException(id));
        return mapper.toResponse(romancista);
    }

    @Transactional(readOnly = true)
    public List<RomancistResponse> listar(String nomeFiltro) {
        if (nomeFiltro == null || nomeFiltro.isBlank()) {
            return repository.findAll(Sort.by("id")).stream()
                    .map(mapper::toResponse)
                    .toList();
        }
        String filtroSanitizado = StringSanitizer.sanitize(nomeFiltro);
        return repository.findByNomeContainingIgnoreCase(filtroSanitizado, Pageable.unpaged(Sort.by("id")))
                .map(mapper::toResponse)
                .getContent();
    }

    @Transactional
    public RomancistResponse atualizar(Long id, RomancistRequest request) {
        Romancista existente = repository.findById(id)
                .orElseThrow(() -> new RomancistaNotFoundException(id));

        String novoNomeSanitizado = StringSanitizer.sanitize(request.nome());

        if (repository.existsByNomeAndIdNot(novoNomeSanitizado, id)) {
            throw new RomancistaConflictException(novoNomeSanitizado);
        }

        existente.setNome(novoNomeSanitizado);
        return mapper.toResponse(repository.save(existente));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RomancistaNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
