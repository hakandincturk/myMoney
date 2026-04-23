package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.security.services.JwtService;
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
  private JwtService jwtService;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private AuthRules authRules;

  @Test
  @DisplayName("Başarılı giriş - token döndürmeli")
  void login_shouldReturnToken_whenCredentialsValid() {
    LoginRequestDto body = new LoginRequestDto("test@test.com", "password123");
    Users user = new Users();
    user.setId(1L);
    user.setEmail("test@test.com");

    when(userRepository.findByEmailAndIsRemovedFalse("test@test.com"))
        .thenReturn(Optional.of(user));
    when(jwtService.generateToken(user)).thenReturn("jwt-token-123");

    LoginResponseDto result = authService.login(body);

    assertNotNull(result);
    assertEquals("jwt-token-123", result.getToken());
    verify(authenticationProvider).authenticate(any());
  }

  @Test
  @DisplayName("Geçersiz kimlik bilgileri ile UnauthorizedException fırlatılmalı")
  void login_shouldThrowUnauthorizedException_whenCredentialsInvalid() {
    LoginRequestDto body = new LoginRequestDto("wrong@test.com", "wrongpassword");

    when(authenticationProvider.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> authService.login(body));

    assertEquals("Giriş başarısız. Lütfen bilgilerinizi kontrol edin", exception.getMessage());
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
}
