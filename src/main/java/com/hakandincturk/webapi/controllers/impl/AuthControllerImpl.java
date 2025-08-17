package com.hakandincturk.webapi.controllers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.concretes.AuthController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/auth")
@Tag(name = "Auth", description = "Kullanıcı giriş işlemleri")
public class AuthControllerImpl extends BaseController implements AuthController {

  @Autowired
  private AuthService authService;

  @Override
  @PostMapping(value = "/login")
  @Operation(summary = "Login", description = "Kullanıcının sisteme giriş yapmasını sağlar.")
  public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto body) {
    return success("Giriş başarılı", authService.login(body));
  }

  @Override
  @PostMapping(value = "/register")
  public ApiResponse<?> register(@Valid @RequestBody RegisterRequestDto body) {
    authService.register(body);
    return success("Kayıt başarılı, lütfen giriş yapınız.", null);
  }
  
}
