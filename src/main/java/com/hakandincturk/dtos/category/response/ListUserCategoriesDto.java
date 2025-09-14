package com.hakandincturk.dtos.category.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListUserCategoriesDto {
  private Long id;
  private String name;
  private LocalDate createdAt;
}
