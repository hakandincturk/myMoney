package com.hakandincturk.webapi.controllers.concretes;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;

public interface AuthController {
  public ApiResponse<LoginResponseDto> login(LoginRequestDto body);
  public ApiResponse<?> register(RegisterRequestDto body);
}
