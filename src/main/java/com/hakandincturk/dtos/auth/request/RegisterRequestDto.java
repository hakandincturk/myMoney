package com.hakandincturk.dtos.auth.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

  @NotNull
  private String fullName;

  @NotNull
  private String email;

  @NotNull
  private String password;

  @NotNull
  private String phone;
}
