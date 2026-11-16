package com.madr.madr_app_backend.dto;

public record LivroResponse(
        Long id,
        String titulo,
        Integer ano,
        Long romancistaId
) {}
