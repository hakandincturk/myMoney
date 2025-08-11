package com.hakandincturk.dtos.contact.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListMyContactsResponseDto {
  private Long id;
  private String fullName;
  private String note;
}
