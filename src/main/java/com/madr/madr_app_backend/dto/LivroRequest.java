package com.madr.madr_app_backend.dto;

import com.madr.madr_app_backend.util.StringSanitizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LivroRequest(
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 2, max = 255, message = "O título deve ter entre 2 e 255 caracteres")
    String titulo,

    @NotNull(message = "O ano é obrigatório")
    @Positive(message = "O ano deve ser um número positivo")
    Integer ano,

    @NotNull(message = "O id do romancista é obrigatório")
    @Positive(message = "O id do romancista deve ser positivo")
    Long romancistaId
) {
    public LivroRequest {
        titulo = StringSanitizer.sanitize(titulo);
    }
}
