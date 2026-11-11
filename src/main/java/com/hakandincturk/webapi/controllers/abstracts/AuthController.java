package com.hakandincturk.webapi.controllers.abstracts;

import java.util.List;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.RevokeSessionsResponseDto;
import com.hakandincturk.dtos.auth.response.SessionResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthController {
  ApiResponse<LoginResponseDto> login(LoginRequestDto body, HttpServletRequest request, HttpServletResponse response);
  ApiResponse<?> register(RegisterRequestDto body);
  ApiResponse<Void> logout(HttpServletResponse response);
  ApiResponse<MeResponseDto> me();
  ApiResponse<List<SessionResponseDto>> sessions();
  ApiResponse<Void> revokeSession(Long sessionId, HttpServletResponse response);
  ApiResponse<RevokeSessionsResponseDto> revokeOtherSessions();
}
