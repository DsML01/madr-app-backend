package com.madr.madr_app_backend.mapper;

import com.madr.madr_app_backend.dto.LivroRequest;
import com.madr.madr_app_backend.dto.LivroResponse;
import com.madr.madr_app_backend.model.Livro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper (componentModel = "spring")
public interface LivroMapper {
    @Mapping(target = "romancistaId", source = "romancista.id")
    LivroResponse toResponse(Livro livro);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "romancista", ignore = true)
    Livro toEntity(LivroRequest request);
}
