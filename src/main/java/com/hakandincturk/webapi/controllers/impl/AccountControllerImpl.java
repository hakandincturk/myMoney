package com.hakandincturk.webapi.controllers.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.enums.sort.AccountSortColumn;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.AccountService;
import com.hakandincturk.utils.PaginationUtils;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.AccountController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/account")
@Tag(name = "Account", description = "Hesap işlemleri")
public class AccountControllerImpl extends BaseController implements AccountController {

  private final AccountService accountService;

  @Override
  @PostMapping(value = "/my")
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
  public ApiResponse<PagedResponse<ListMyAccountsResponseDto>> listMyActiveAccounts(@ModelAttribute SortablePageRequest pageData) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      Pageable pageable = PaginationUtils.toPageable(pageData, AccountSortColumn.class);
      return successPaged("Hesaplarınız getirildi", accountService.listMyActiveAccounts(userId, pageable));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @PutMapping(value = "/my/{accountId}")
  @Operation(summary = "Update my account", description = "Kullanıcının hesap bilgilerini ve bakiyesini günceller")
  public ApiResponse<?> updateMyAccount(@PathVariable(value = "accountId") Long accountId, @Valid @RequestBody UpdateAccountRequestDto body) {
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
