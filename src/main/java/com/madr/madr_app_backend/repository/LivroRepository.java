package com.madr.madr_app_backend.repository;

import com.madr.madr_app_backend.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LivroRepository extends JpaRepository<Livro, Long>, JpaSpecificationExecutor<Livro> {
    boolean existsByTitulo(String titulo);

    boolean existsByTituloAndIdNot(String titulo, Long id);
}
