package com.hakandincturk.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.SessionResponseDto;
import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;

@Mapper(componentModel = "spring")
public interface SessionMapper {

  @Mapping(target = "current", expression = "java(session.getId() != null && session.getId().equals(currentSessionId))")
  SessionResponseDto toSessionResponseDto(UserSession session, @Context Long currentSessionId);

  MeResponseDto toMeResponseDto(Users user);
}
