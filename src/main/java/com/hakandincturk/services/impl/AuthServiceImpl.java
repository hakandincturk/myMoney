package com.hakandincturk.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.RevokeSessionsResponseDto;
import com.hakandincturk.dtos.auth.response.SessionResponseDto;
import com.hakandincturk.mappers.SessionMapper;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.security.session.ClientInfo;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.services.abstracts.SessionService;
import com.hakandincturk.services.rules.AuthRules;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService  {

  private final UserRepository userRepository;
  private final AuthenticationProvider authenticationProvider;
  private final SessionService sessionService;
  private final BCryptPasswordEncoder passwordEncoder;
  private final AuthRules authRules;
  private final SessionMapper sessionMapper;

  @Override
  @Transactional
  public LoginResult login(LoginRequestDto body, ClientInfo client, Optional<String> previousRawToken) throws UnauthorizedException {
    Users user;
    try{
      authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(body.getEmail(), body.getPassword()));
      user = userRepository.findByEmailAndIsRemovedFalse(body.getEmail()).orElseThrow();
    }
    catch(Exception ex){
      throw new UnauthorizedException("Giriş başarısız. Lütfen bilgilerinizi kontrol edin");
    }

    // Logging in again from the same browser replaces its session instead of adding a duplicate row.
    previousRawToken
      .flatMap(sessionService::authenticate)
      .ifPresent(previous -> sessionService.revokeCurrent(previous.getId()));

    SessionService.CreatedSession created = sessionService.create(user, client);
    return new LoginResult(created.rawToken(), new LoginResponseDto(user.getFullName(), user.getEmail()));
  }

  @Override
  public void register(RegisterRequestDto body) {
    authRules.checkUserEmailExist(body.getEmail());

    Users newUser = new Users(
      body.getFullName(),
      body.getEmail(),
      passwordEncoder.encode(body.getPassword()),
      body.getPhone(),
      List.of()
    );
    userRepository.save(newUser);
  }

  @Override
  public MeResponseDto me(Long userId) {
    Users user = userRepository.findByIdAndIsRemovedFalse(userId)
      .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));
    return sessionMapper.toMeResponseDto(user);
  }

  @Override
  public List<SessionResponseDto> listSessions(Long userId, Long currentSessionId) {
    return sessionService.listActive(userId).stream()
      .map(session -> sessionMapper.toSessionResponseDto(session, currentSessionId))
      .toList();
  }

  @Override
  public void logout(Long currentSessionId) {
    sessionService.revokeCurrent(currentSessionId);
  }

  @Override
  public void revokeSession(Long userId, Long sessionId) {
    sessionService.revoke(userId, sessionId);
  }

  @Override
  public RevokeSessionsResponseDto revokeOtherSessions(Long userId, Long currentSessionId) {
    return new RevokeSessionsResponseDto(sessionService.revokeAllExcept(userId, currentSessionId));
  }
}
