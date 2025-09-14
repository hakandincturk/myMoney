package com.hakandincturk.dtos.transaction.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionCategoryDetail {
  private List<Long> categoryIds;
  private List<String> newCategories;
}
