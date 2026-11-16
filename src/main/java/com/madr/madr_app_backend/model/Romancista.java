package com.madr.madr_app_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "romancista")
public class Romancista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roma_cd_id")
    private Long id;

    @Column(name = "roma_nm_nome", nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "romancista", fetch = FetchType.LAZY)
    private List<Livro> livros = new ArrayList<>();

    public Romancista(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
