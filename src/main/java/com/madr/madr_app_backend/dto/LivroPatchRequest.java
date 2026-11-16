package com.madr.madr_app_backend.dto;

import com.madr.madr_app_backend.util.StringSanitizer;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LivroPatchRequest(
        @Size(min = 2, max = 255, message = "O título deve ter entre 2 e 255 caracteres")
        String titulo,

        @Positive(message = "O ano deve ser um número positivo")
        Integer ano,

        @Positive(message = "O id do romancista deve ser positivo")
        Long romancistaId
) {
    public LivroPatchRequest {
        titulo = StringSanitizer.sanitize(titulo);
    }
}
