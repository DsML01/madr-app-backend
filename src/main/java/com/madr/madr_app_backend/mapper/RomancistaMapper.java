package com.madr.madr_app_backend.mapper;

import com.madr.madr_app_backend.dto.RomancistRequest;
import com.madr.madr_app_backend.dto.RomancistResponse;
import com.madr.madr_app_backend.model.Romancista;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RomancistaMapper {
    RomancistResponse toResponse(Romancista romancista);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "livros", ignore = true)
    Romancista toEntity(RomancistRequest request);
}
