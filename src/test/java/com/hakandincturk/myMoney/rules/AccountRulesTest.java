package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Account;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.services.rules.AccountRules;

@ExtendWith(MockitoExtension.class)
class AccountRulesTest {

  @InjectMocks
  private AccountRules accountRules;

  @Mock
  private AccountRepository accountRepository;

  @Test
  @DisplayName("Hesap mevcut olduğunda başarıyla döndürülmeli")
  void checkUserAccountExistAndGet_shouldReturnAccount_whenExists() {
    Long userId = 1L;
    Long accountId = 10L;
    Account account = new Account();
    account.setId(accountId);

    when(accountRepository.findByIdAndUserIdAndIsRemovedFalse(accountId, userId))
        .thenReturn(Optional.of(account));

    Account result = accountRules.checkUserAccountExistAndGet(userId, accountId);

    assertNotNull(result);
    assertEquals(accountId, result.getId());
    verify(accountRepository).findByIdAndUserIdAndIsRemovedFalse(accountId, userId);
  }

  @Test
  @DisplayName("Hesap bulunamadığında NotFoundException fırlatılmalı")
  void checkUserAccountExistAndGet_shouldThrowNotFoundException_whenNotExists() {
    Long userId = 1L;
    Long accountId = 10L;

    when(accountRepository.findByIdAndUserIdAndIsRemovedFalse(accountId, userId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> accountRules.checkUserAccountExistAndGet(userId, accountId));

    assertEquals("Hesap bulunamadı", exception.getMessage());
  }
}
