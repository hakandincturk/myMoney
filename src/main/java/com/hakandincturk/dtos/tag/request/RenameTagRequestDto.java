package com.hakandincturk.dtos.tag.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenameTagRequestDto {
  @NotBlank(message = "Etiket adı boş olamaz")
  @Size(max = 100, message = "Etiket adı en fazla 100 karakter olabilir")
  private String name;
}
