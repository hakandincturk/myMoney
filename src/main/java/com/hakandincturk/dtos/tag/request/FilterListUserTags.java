package com.hakandincturk.dtos.tag.request;

import java.time.LocalDate;

import com.hakandincturk.dtos.SortablePageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterListUserTags extends SortablePageRequest {
  private String name;
  private LocalDate createdStartDate;
  private LocalDate createdEndDate;
}
