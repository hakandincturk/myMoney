package com.hakandincturk.mappers;

import org.mapstruct.Mapper;

import com.hakandincturk.dtos.tag.response.ListUserTagsDto;
import com.hakandincturk.dtos.tag.response.TransactionTagInfoDto;
import com.hakandincturk.models.Tag;

@Mapper(componentModel = "spring")
public interface TagMapper {
  ListUserTagsDto toListUserTagsDto(Tag tag);
  TransactionTagInfoDto toTransactionTagInfoDto(Tag tag);
}
