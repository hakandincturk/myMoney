package com.hakandincturk.dtos.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Login result body. The session token is never in the body; it travels only in the HttpOnly cookie. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
  private String fullName;
  private String email;
}
