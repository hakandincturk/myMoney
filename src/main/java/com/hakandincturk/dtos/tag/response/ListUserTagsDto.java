package com.hakandincturk.dtos.tag.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListUserTagsDto {
  private Long id;
  private String name;
  private LocalDateTime createdAt;
}
