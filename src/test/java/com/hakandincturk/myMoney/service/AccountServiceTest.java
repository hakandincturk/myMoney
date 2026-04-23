package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.mappers.AccountMapper;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.services.impl.AccountServiceImpl;
import com.hakandincturk.services.rules.AccountRules;
import com.hakandincturk.services.rules.UserRules;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @InjectMocks
  private AccountServiceImpl accountService;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountRules accountRules;

  @Mock
  private UserRules userRules;

  @Mock
  private AccountMapper accountMapper;

  @Test
  @DisplayName("Başarılı hesap oluşturma")
  void createAccount_shouldSaveAccount() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    CreateAccountRequestDto body = new CreateAccountRequestDto(
        "Banka Hesabım", AccountTypes.BANK, CurrencyTypes.TL, BigDecimal.valueOf(5000));

    when(userRules.checkUserExistAndGet(userId)).thenReturn(user);

    accountService.createAccount(body, userId);

    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(captor.capture());

    Account saved = captor.getValue();
    assertEquals("Banka Hesabım", saved.getName());
    assertEquals(AccountTypes.BANK, saved.getType());
    assertEquals(CurrencyTypes.TL, saved.getCurrency());
    assertEquals(BigDecimal.valueOf(5000), saved.getBalance());
    assertEquals(user, saved.getUser());
  }

  @Test
  @DisplayName("Aktif hesapları listeleme")
  void listMyActiveAccounts_shouldReturnMappedPage() {
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 10);

    Account account = new Account();
    account.setId(1L);
    account.setName("Test Hesap");

    ListMyAccountsResponseDto dto = new ListMyAccountsResponseDto();
    dto.setId(1L);
    dto.setName("Test Hesap");

    Page<Account> accountPage = new PageImpl<>(List.of(account));
    when(accountRepository.findByUserIdAndIsRemovedFalse(userId, pageable)).thenReturn(accountPage);
    when(accountMapper.toListMyAccountsResponseDto(account)).thenReturn(dto);

    Page<ListMyAccountsResponseDto> result = accountService.listMyActiveAccounts(userId, pageable);

    assertEquals(1, result.getContent().size());
    assertEquals("Test Hesap", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Hesap güncelleme - bakiye farkı pozitif olduğunda")
  void updateMyAccount_shouldRecalculateBalance_whenTotalBalanceIncreased() {
    Long userId = 1L;
    Long accountId = 10L;

    Account dbAccount = new Account();
    dbAccount.setId(accountId);
    dbAccount.setName("Eski İsim");
    dbAccount.setTotalBalance(BigDecimal.valueOf(5000));
    dbAccount.setBalance(BigDecimal.valueOf(3000));

    UpdateAccountRequestDto body = new UpdateAccountRequestDto("Yeni İsim", BigDecimal.valueOf(7000));

    when(accountRules.checkUserAccountExistAndGet(userId, accountId)).thenReturn(dbAccount);

    accountService.updateMyAccount(userId, accountId, body);

    verify(accountRepository).save(dbAccount);
    assertEquals("Yeni İsim", dbAccount.getName());
    assertEquals(BigDecimal.valueOf(7000), dbAccount.getTotalBalance());
    assertEquals(BigDecimal.valueOf(5000), dbAccount.getBalance());
  }

  @Test
  @DisplayName("Hesap güncelleme - bakiye farkı negatif olduğunda")
  void updateMyAccount_shouldRecalculateBalance_whenTotalBalanceDecreased() {
    Long userId = 1L;
    Long accountId = 10L;

    Account dbAccount = new Account();
    dbAccount.setId(accountId);
    dbAccount.setName("Eski İsim");
    dbAccount.setTotalBalance(BigDecimal.valueOf(5000));
    dbAccount.setBalance(BigDecimal.valueOf(3000));

    UpdateAccountRequestDto body = new UpdateAccountRequestDto("Yeni İsim", BigDecimal.valueOf(4000));

    when(accountRules.checkUserAccountExistAndGet(userId, accountId)).thenReturn(dbAccount);

    accountService.updateMyAccount(userId, accountId, body);

    assertEquals(BigDecimal.valueOf(2000), dbAccount.getBalance());
    assertEquals(BigDecimal.valueOf(4000), dbAccount.getTotalBalance());
  }
}
