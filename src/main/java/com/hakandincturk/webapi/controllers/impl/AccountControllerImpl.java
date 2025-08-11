package com.hakandincturk.webapi.controllers.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.AccountService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.concretes.AccountController;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/account")
public class AccountControllerImpl extends BaseController implements AccountController {

  @Autowired
  private AccountService accountService;

  @Override
  @PostMapping(value = "/")
  @Operation(summary = "Create Account", description = "Kullanıcının yeni hesap açmasını sağlar")
  public ApiResponse<?> createAccount(@Valid @RequestBody CreateAccountRequestDto body) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      accountService.createAccount(body, userId);

      return success("Hesap başarılı bir şekilde oluşturuldu", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/my/active")
  @Operation(summary = "List my active accounts", description = "Kullanıcının aktif olan hesaplarını listeler")
  public ApiResponse<List<ListMyAccountsResponseDto>> listMyActiveAccounts() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Hesaplarınız getirildi", accountService.listMyActiveAccounts(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @PutMapping(value = "/my/{id}")
  @Operation(summary = "Update my account", description = "Kullanıcının hesap bilgilerini ve bakiyesini günceller")
  public ApiResponse<?> updateMyAccount(@RequestParam(value = "accountId") Long accountId, @Valid @RequestBody UpdateAccountRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      accountService.updateMyAccount(userId, accountId, body);
      return success("Hesaplarınız güncellendi", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
  
}
