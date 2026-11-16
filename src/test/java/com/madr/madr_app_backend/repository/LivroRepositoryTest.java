package com.madr.madr_app_backend.repository;

import com.madr.madr_app_backend.model.Livro;
import com.madr.madr_app_backend.model.Romancista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LivroRepositoryTest {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private RomancistaRepository romancistaRepository;

    @Test
    @DisplayName("Deve salvar livro associado a romancista e verificar a existencia do título")
    void
}
