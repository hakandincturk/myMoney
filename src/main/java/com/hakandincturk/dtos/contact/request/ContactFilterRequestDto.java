package com.hakandincturk.dtos.contact.request;

import com.hakandincturk.dtos.SortablePageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactFilterRequestDto extends SortablePageRequest {
  private String fullName;
  private String note;
}
