package com.hakandincturk.services.abstracts;

import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;

public interface AuthService {

  public LoginResponseDto login(LoginRequestDto body);
  public void register(RegisterRequestDto body);
  
}