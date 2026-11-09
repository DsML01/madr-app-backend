package com.madr.madr_app_backend.service;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.exception.RomancistaConflictException;
import com.madr.madr_app_backend.exception.RomancistaNotFoundException;
import com.madr.madr_app_backend.model.Romancista;
import com.madr.madr_app_backend.util.StringSanitizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RomancistaService {

    private final Map<Long, Romancista> repositorio = new ConcurrentHashMap<>();
    private final AtomicLong idGerador = new AtomicLong(1);

    private RomancistResponse toResponse(Romancista romancista) {
        return new RomancistResponse(romancista.getId(), romancista.getNome());
    }

    public RomancistResponse criar(RomancistRequest request) {
        String nomeSanitizado = StringSanitizer.sanitize(request.nome());

        boolean jaExiste = repositorio.values().stream().anyMatch(r -> r.getNome().equalsIgnoreCase(nomeSanitizado));

        if (jaExiste) {
            throw new RomancistaConflictException(nomeSanitizado);
        }

        Long novoId = idGerador.getAndIncrement();
        Romancista romancista = new Romancista(novoId, nomeSanitizado);
        repositorio.put(novoId, romancista);

        return toResponse(romancista);
    }

    public RomancistResponse buscarPorId(Long id) {
        Romancista romancista = repositorio.get(id);

        if (romancista == null) {
            throw new RomancistaNotFoundException(id);
        }

        return toResponse(romancista);
    }

    public List<RomancistResponse> listar(String nomeFiltro) {
        return repositorio.values().stream().filter(r -> {
            if (nomeFiltro == null || nomeFiltro.isBlank()) {
                return true;
            }
            String filtroSanitizado = StringSanitizer.sanitize(nomeFiltro);
            return r.getNome().contains(filtroSanitizado);
        }).map(this::toResponse).toList();
    }

    public RomancistResponse atualizar(Long id, RomancistRequest request) {
        Romancista existente = repositorio.get(id);

        if(existente == null) {
            throw new RomancistaNotFoundException(id);
        }

        String novoNomeSanitizado = StringSanitizer.sanitize(request.nome());

        boolean conflito = repositorio.values().stream().anyMatch(r -> !r.getId().equals(id)
        && r.getNome().equalsIgnoreCase(novoNomeSanitizado));

        if (conflito) {
            throw new RomancistaConflictException(novoNomeSanitizado);
        }

        existente.setNome(novoNomeSanitizado);
        return toResponse(existente);
    }

    public void excluir(Long id) {
        if (!repositorio.containsKey(id)) {
            throw new RomancistaNotFoundException(id);
        }
        repositorio.remove(id);
    }
}
