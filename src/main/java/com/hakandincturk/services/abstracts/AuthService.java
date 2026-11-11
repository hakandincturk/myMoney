package com.hakandincturk.services.abstracts;

import java.util.List;
import java.util.Optional;

import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.RevokeSessionsResponseDto;
import com.hakandincturk.dtos.auth.response.SessionResponseDto;
import com.hakandincturk.security.session.ClientInfo;

public interface AuthService {

  /** Raw token for the cookie, kept apart from the response body. */
  record LoginResult(String rawToken, LoginResponseDto body) {
  }

  /** Checks credentials and opens a session; a still-valid session from the same browser is revoked first. */
  LoginResult login(LoginRequestDto body, ClientInfo client, Optional<String> previousRawToken);

  void register(RegisterRequestDto body);

  MeResponseDto me(Long userId);

  List<SessionResponseDto> listSessions(Long userId, Long currentSessionId);

  void logout(Long currentSessionId);

  void revokeSession(Long userId, Long sessionId);

  RevokeSessionsResponseDto revokeOtherSessions(Long userId, Long currentSessionId);
}
