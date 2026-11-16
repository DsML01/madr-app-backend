package com.madr.madr_app_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "livro")
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "livr_cd_id")
    private Long id;

    @Column(name = "livr_tx_titulo", nullable = false, unique = true)
    private String titulo;

    @Column(name = "livr_nr_ano", nullable = false)
    private Integer ano;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livr_cd_romancista", nullable = false)
    private Romancista romancista;
}
