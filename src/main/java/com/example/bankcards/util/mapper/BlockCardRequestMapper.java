package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.BlockCardRequestDto;
import com.example.bankcards.entity.BlockCardRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BlockCardRequestMapper {

    BlockCardRequestMapper INSTANCE = Mappers.getMapper(BlockCardRequestMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "card.id", target = "cardId")
    BlockCardRequestDto blockCardRequestToBlockCardRequestDto(BlockCardRequest blockCardRequest);
}
