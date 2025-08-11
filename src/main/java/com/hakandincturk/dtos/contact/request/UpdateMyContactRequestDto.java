package com.hakandincturk.dtos.contact.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMyContactRequestDto {

  @NotNull(message = "Ad boş olamaz")
  private String fullName;

  private String note;
}
