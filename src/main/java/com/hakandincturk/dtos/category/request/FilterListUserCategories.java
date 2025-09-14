package com.hakandincturk.dtos.category.request;

import java.time.LocalDate;

import com.hakandincturk.dtos.SortablePageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterListUserCategories extends SortablePageRequest {
  private String name;
  private LocalDate createdStartDate;
  private LocalDate createdEndDate;
}
