package com.hakandincturk.mappers;

import org.mapstruct.Mapper;

import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;
import com.hakandincturk.models.Contact;

@Mapper(componentModel = "spring")
public interface ContactMapper {
  ListMyContactsResponseDto toListMyContactsResponseDto(Contact contact); 
}