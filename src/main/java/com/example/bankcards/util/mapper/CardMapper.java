package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CardMapper {
    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    @Mapping(source = "cardHolder.id", target = "userId")
    CardDto cardToCardDto(Card card);

    @Mapping(target = "cardHolder", ignore = true)
    Card cardDtoToCard(CardDto cardDto);
}
