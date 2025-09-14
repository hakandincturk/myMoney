package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;

public interface AuthController {
  ApiResponse<LoginResponseDto> login(LoginRequestDto body);
  ApiResponse<?> register(RegisterRequestDto body);
}
