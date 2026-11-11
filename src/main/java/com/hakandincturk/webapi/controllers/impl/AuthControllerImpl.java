package com.hakandincturk.webapi.controllers.impl;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.RevokeSessionsResponseDto;
import com.hakandincturk.dtos.auth.response.SessionResponseDto;
import com.hakandincturk.security.UserSessionAuthentication;
import com.hakandincturk.security.session.ClientInfoResolver;
import com.hakandincturk.security.session.SessionCookies;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.services.abstracts.AuthService.LoginResult;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.AuthController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Tag(name = "Auth", description = "Kullanıcı giriş ve oturum işlemleri")
public class AuthControllerImpl extends BaseController implements AuthController {

  private final AuthService authService;
  private final SessionCookies sessionCookies;
  private final ClientInfoResolver clientInfoResolver;

  @Override
  @PostMapping(value = "/login")
  @Operation(summary = "Login", description = "Kullanıcının sisteme giriş yapmasını sağlar; oturum HttpOnly cookie ile taşınır.")
  public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto body, HttpServletRequest request, HttpServletResponse response) {
    LoginResult result = authService.login(body, clientInfoResolver.resolve(request), sessionCookies.read(request));
    response.addHeader(HttpHeaders.SET_COOKIE, sessionCookies.issue(result.rawToken()).toString());
    return success("Giriş başarılı", result.body());
  }

  @Override
  @PostMapping(value = "/register")
  public ApiResponse<?> register(@Valid @RequestBody RegisterRequestDto body) {
    authService.register(body);
    return success("Kayıt başarılı, lütfen giriş yapınız.", null);
  }

  @Override
  @PostMapping(value = "/logout")
  @Operation(summary = "Logout", description = "Mevcut oturumu sunucuda kapatır.")
  public ApiResponse<Void> logout(HttpServletResponse response) {
    authService.logout(current().getSessionId());
    response.addHeader(HttpHeaders.SET_COOKIE, sessionCookies.clear().toString());
    return success("Çıkış yapıldı");
  }

  @Override
  @GetMapping(value = "/me")
  @Operation(summary = "Me", description = "Oturumu açık kullanıcının bilgilerini döner.")
  public ApiResponse<MeResponseDto> me() {
    return success("Kullanıcı bilgileri getirildi", authService.me(current().getUserId()));
  }

  @Override
  @GetMapping(value = "/sessions")
  @Operation(summary = "List sessions", description = "Kullanıcının aktif oturumlarını listeler.")
  public ApiResponse<List<SessionResponseDto>> sessions() {
    UserSessionAuthentication auth = current();
    return success("Oturumlar getirildi", authService.listSessions(auth.getUserId(), auth.getSessionId()));
  }

  @Override
  @DeleteMapping(value = "/sessions/{sessionId}")
  @Operation(summary = "Revoke session", description = "Kullanıcının seçtiği oturumu kapatır.")
  public ApiResponse<Void> revokeSession(@PathVariable Long sessionId, HttpServletResponse response) {
    UserSessionAuthentication auth = current();
    authService.revokeSession(auth.getUserId(), sessionId);
    if (sessionId.equals(auth.getSessionId())) {
      response.addHeader(HttpHeaders.SET_COOKIE, sessionCookies.clear().toString());
    }
    return success("Oturum kapatıldı");
  }

  @Override
  @DeleteMapping(value = "/sessions")
  @Operation(summary = "Revoke other sessions", description = "Mevcut oturum dışındaki tüm oturumları kapatır.")
  public ApiResponse<RevokeSessionsResponseDto> revokeOtherSessions() {
    UserSessionAuthentication auth = current();
    return success("Diğer oturumlar kapatıldı", authService.revokeOtherSessions(auth.getUserId(), auth.getSessionId()));
  }

  private UserSessionAuthentication current() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof UserSessionAuthentication userAuth) {
      return userAuth;
    }
    throw new UnauthorizedException("Yetkisiz giriş. Lütfen tekrar giriş yapınız");
  }
}
