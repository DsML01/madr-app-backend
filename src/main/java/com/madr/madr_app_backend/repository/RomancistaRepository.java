package com.madr.madr_app_backend.repository;

import com.madr.madr_app_backend.model.Romancista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RomancistaRepository extends JpaRepository<Romancista, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    Page<Romancista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
