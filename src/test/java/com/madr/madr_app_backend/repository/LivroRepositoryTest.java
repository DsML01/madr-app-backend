package com.madr.madr_app_backend.repository;

import com.madr.madr_app_backend.model.Livro;
import com.madr.madr_app_backend.model.Romancista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LivroRepositoryTest {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private RomancistaRepository romancistaRepository;

    @Test
    @DisplayName("Deve salvar livro associado a romancista e verificar existência por título")
    void deveSalvarEVerificarExistenciaPorTitulo() {
        Romancista romancista = romancistaRepository.save(new Romancista(null, "machado de assis"));
        Livro livro = livroRepository.save(new Livro(null, "dom casmurro", 1899, romancista));

        assertThat(livro.getId()).isNotNull();
        assertThat(livroRepository.existsByTitulo("dom casmurro")).isTrue();
        assertThat(livroRepository.existsByTituloAndIdNot("dom casmurro", livro.getId())).isFalse();
        assertThat(livroRepository.existsByTituloAndIdNot("dom casmurro", livro.getId() + 1)).isTrue();
    }

    @Test
    @DisplayName("Deve filtrar livros por título parcial e ano com paginação no H2 em modo PostgreSQL")
    void deveFiltrarLivrosPorTituloEAno() {
        Romancista romancista = romancistaRepository.save(new Romancista(null, "machado de assis"));
        livroRepository.save(new Livro(null, "dom casmurro", 1899, romancista));
        livroRepository.save(new Livro(null, "memórias póstumas de brás cubas", 1881, romancista));

        Specification<Livro> spec = (root, query, cb) -> cb.and(
                cb.like(cb.lower(root.get("titulo")), "%casmurro%"),
                cb.equal(root.get("ano"), 1899)
        );

        Page<Livro> resultado = livroRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().getFirst().getTitulo()).isEqualTo("dom casmurro");
        assertThat(resultado.getContent().getFirst().getRomancista().getId()).isEqualTo(romancista.getId());
    }
}
