package com.hakandincturk.dtos.category.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListUserCategoriesWithTransactionCountDto {
  private Long id;
  private String name;
  private LocalDateTime createdAt;
  private Long transactionCount;
}
