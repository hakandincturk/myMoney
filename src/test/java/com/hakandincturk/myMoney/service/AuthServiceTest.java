package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.SessionResponseDto;
import com.hakandincturk.mappers.SessionMapper;
import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.security.session.ClientInfo;
import com.hakandincturk.services.abstracts.AuthService.LoginResult;
import com.hakandincturk.services.abstracts.SessionService;
import com.hakandincturk.services.abstracts.SessionService.CreatedSession;
import com.hakandincturk.services.impl.AuthServiceImpl;
import com.hakandincturk.services.rules.AuthRules;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks
  private AuthServiceImpl authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthenticationProvider authenticationProvider;

  @Mock
  private SessionService sessionService;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private AuthRules authRules;

  @Spy
  private SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);

  private final ClientInfo client = new ClientInfo("JUnit", "127.0.0.1", "TR");

  private Users user() {
    Users user = new Users();
    user.setId(1L);
    user.setEmail("test@test.com");
    user.setFullName("Test User");
    return user;
  }

  @Test
  @DisplayName("Başarılı giriş oturum açmalı, ham token'ı döndürmeli, gövdede token olmamalı")
  void login_createsSession_andReturnsRawTokenSeparately() {
    Users user = user();
    UserSession session = new UserSession();
    session.setId(5L);
    when(userRepository.findByEmailAndIsRemovedFalse("test@test.com")).thenReturn(Optional.of(user));
    when(sessionService.create(user, client)).thenReturn(new CreatedSession("raw-token", session));

    LoginResult result = authService.login(new LoginRequestDto("test@test.com", "password123"), client, Optional.empty());

    assertEquals("raw-token", result.rawToken());
    assertEquals("Test User", result.body().getFullName());
    assertEquals("test@test.com", result.body().getEmail());
    verify(authenticationProvider).authenticate(any());
    verify(sessionService, never()).revokeCurrent(any());
  }

  @Test
  @DisplayName("Aynı tarayıcıdan tekrar girişte önceki oturum kapatılmalı")
  void login_revokesPreviousSessionFromSameCookie() {
    Users user = user();
    UserSession previous = new UserSession();
    previous.setId(3L);
    when(userRepository.findByEmailAndIsRemovedFalse("test@test.com")).thenReturn(Optional.of(user));
    when(sessionService.authenticate("old-raw")).thenReturn(Optional.of(previous));
    when(sessionService.create(user, client)).thenReturn(new CreatedSession("new-raw", new UserSession()));

    authService.login(new LoginRequestDto("test@test.com", "password123"), client, Optional.of("old-raw"));

    verify(sessionService).revokeCurrent(3L);
  }

  @Test
  @DisplayName("Geçersiz kimlik bilgileri ile UnauthorizedException fırlatılmalı ve oturum açılmamalı")
  void login_wrongPassword_throwsUnauthorized_noSession() {
    when(authenticationProvider.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> authService.login(new LoginRequestDto("wrong@test.com", "wrong"), client, Optional.empty()));

    assertEquals("Giriş başarısız. Lütfen bilgilerinizi kontrol edin", exception.getMessage());
    verify(sessionService, never()).create(any(), any());
  }

  @Test
  @DisplayName("Me mevcut kullanıcıyı döndürmeli")
  void me_returnsCurrentUser() {
    when(userRepository.findByIdAndIsRemovedFalse(1L)).thenReturn(Optional.of(user()));

    MeResponseDto me = authService.me(1L);

    assertEquals(1L, me.getId());
    assertEquals("Test User", me.getFullName());
    assertEquals("test@test.com", me.getEmail());
  }

  @Test
  @DisplayName("Oturum listesi yalnızca mevcut oturumu current işaretlemeli ve sırayı korumalı")
  void sessions_marksOnlyCurrentAsCurrent_newestFirst() {
    UserSession newer = session(11L, "Chrome · macOS", Instant.parse("2026-11-11T12:00:00Z"));
    UserSession older = session(10L, "Safari · iPhone", Instant.parse("2026-11-10T12:00:00Z"));
    when(sessionService.listActive(1L)).thenReturn(List.of(newer, older));

    List<SessionResponseDto> result = authService.listSessions(1L, 10L);

    assertEquals(List.of(11L, 10L), result.stream().map(SessionResponseDto::getId).toList());
    assertFalse(result.get(0).isCurrent());
    assertTrue(result.get(1).isCurrent());
    assertEquals("Chrome · macOS", result.get(0).getDeviceLabel());
    assertEquals("1.2.3.4", result.get(0).getIpAddress());
    assertEquals("TR", result.get(0).getCountry());
    assertEquals(Instant.parse("2026-11-11T12:00:00Z"), result.get(0).getLastSeenAt());
  }

  @Test
  @DisplayName("Başka kullanıcının oturumunu kapatma NotFound ile reddedilmeli")
  void revoke_otherUsersSessionId_isRejected() {
    doThrow(new NotFoundException("Oturum bulunamadı")).when(sessionService).revoke(1L, 99L);

    assertThrows(NotFoundException.class, () -> authService.revokeSession(1L, 99L));
  }

  @Test
  @DisplayName("Diğer oturumları kapatma sayıyı döndürmeli ve mevcut oturumu korumalı")
  void revokeOthers_returnsCount_keepsCurrent() {
    when(sessionService.revokeAllExcept(1L, 10L)).thenReturn(2);

    assertEquals(2, authService.revokeOtherSessions(1L, 10L).getRevokedCount());
  }

  @Test
  @DisplayName("Çıkış mevcut oturumu kapatmalı")
  void logout_revokesCurrent() {
    authService.logout(10L);

    verify(sessionService).revokeCurrent(10L);
  }

  @Test
  @DisplayName("Başarılı kayıt - kullanıcı kaydedilmeli")
  void register_shouldSaveUser() {
    RegisterRequestDto body = new RegisterRequestDto("Test User", "test@test.com", "password123", "5551234567");
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

    authService.register(body);

    verify(authRules).checkUserEmailExist("test@test.com");
    ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
    verify(userRepository).save(captor.capture());
    Users savedUser = captor.getValue();
    assertEquals("Test User", savedUser.getFullName());
    assertEquals("test@test.com", savedUser.getEmail());
    assertEquals("encoded-password", savedUser.getPassword());
    assertEquals("5551234567", savedUser.getPhone());
  }

  private UserSession session(Long id, String label, Instant lastSeen) {
    UserSession s = new UserSession();
    s.setId(id);
    s.setDeviceLabel(label);
    s.setIpAddress("1.2.3.4");
    s.setCountry("TR");
    s.setCreatedAt(lastSeen.minusSeconds(3600));
    s.setLastSeenAt(lastSeen);
    return s;
  }
}
