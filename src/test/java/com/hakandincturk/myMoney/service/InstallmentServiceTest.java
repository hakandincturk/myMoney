package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.events.InstallmentsPaidEvent;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.impl.InstallmentServiceImpl;
import com.hakandincturk.services.rules.InstallmentRules;

@ExtendWith(MockitoExtension.class)
class InstallmentServiceTest {

  @InjectMocks
  private InstallmentServiceImpl installmentService;

  @Mock
  private InstallmentRepository installmentRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private InstallmentRules installmentRules;

  @Mock
  private AccountFactory accountFactory;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("Taksit ödeme - tam ödeme yapıldığında status PAID olmalı")
  void payInstallments_shouldSetStatusPaid_whenFullyPaid() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);

    Transaction transaction = new Transaction();
    transaction.setId(100L);
    transaction.setType(TransactionTypes.DEBT);
    transaction.setTotalAmount(BigDecimal.valueOf(1000));
    transaction.setPaidAmount(BigDecimal.valueOf(500));
    transaction.setAccount(account);
    transaction.setUser(user);

    Installment installment = new Installment();
    installment.setId(1L);
    installment.setAmount(BigDecimal.valueOf(500));
    installment.setDebtDate(LocalDate.of(2025, 6, 15));
    installment.setTransaction(transaction);

    PayInstallmentRequestDto body = new PayInstallmentRequestDto(List.of(1L), LocalDate.of(2025, 6, 15));

    when(installmentRules.checkUserInstallmentExistAndGet(userId, List.of(1L)))
        .thenReturn(List.of(installment));
    when(accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.DEBT, BigDecimal.valueOf(500)))
        .thenReturn(account);

    installmentService.payInstallments(userId, body);

    assertTrue(installment.isPaid());
    assertEquals(LocalDate.of(2025, 6, 15), installment.getPaidDate());
    assertEquals(BigDecimal.valueOf(1000), transaction.getPaidAmount());
    assertEquals(TransactionStatuses.PAID, transaction.getStatus());
    verify(transactionRepository).save(transaction);
    verify(accountRepository).save(account);
    verify(eventPublisher).publishEvent(any(InstallmentsPaidEvent.class));
  }

  @Test
  @DisplayName("Taksit ödeme - kısmi ödeme yapıldığında status PARTIAL olmalı")
  void payInstallments_shouldSetStatusPartial_whenPartiallyPaid() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);

    Transaction transaction = new Transaction();
    transaction.setId(100L);
    transaction.setType(TransactionTypes.DEBT);
    transaction.setTotalAmount(BigDecimal.valueOf(3000));
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setAccount(account);
    transaction.setUser(user);

    Installment installment = new Installment();
    installment.setId(1L);
    installment.setAmount(BigDecimal.valueOf(1000));
    installment.setDebtDate(LocalDate.of(2025, 6, 15));
    installment.setTransaction(transaction);

    PayInstallmentRequestDto body = new PayInstallmentRequestDto(List.of(1L), LocalDate.of(2025, 6, 15));

    when(installmentRules.checkUserInstallmentExistAndGet(userId, List.of(1L)))
        .thenReturn(List.of(installment));
    when(accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.DEBT, BigDecimal.valueOf(1000)))
        .thenReturn(account);

    installmentService.payInstallments(userId, body);

    assertEquals(TransactionStatuses.PARTIAL, transaction.getStatus());
    assertEquals(BigDecimal.valueOf(1000), transaction.getPaidAmount());
  }

  @Test
  @DisplayName("Birden fazla taksit ödeme")
  void payInstallments_shouldPayMultipleInstallments() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);

    Transaction transaction = new Transaction();
    transaction.setId(100L);
    transaction.setType(TransactionTypes.PAYMENT);
    transaction.setTotalAmount(BigDecimal.valueOf(3000));
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setAccount(account);
    transaction.setUser(user);

    Installment i1 = new Installment();
    i1.setId(1L);
    i1.setAmount(BigDecimal.valueOf(1000));
    i1.setDebtDate(LocalDate.of(2025, 6, 15));
    i1.setTransaction(transaction);

    Installment i2 = new Installment();
    i2.setId(2L);
    i2.setAmount(BigDecimal.valueOf(1000));
    i2.setDebtDate(LocalDate.of(2025, 7, 15));
    i2.setTransaction(transaction);

    PayInstallmentRequestDto body = new PayInstallmentRequestDto(List.of(1L, 2L), LocalDate.of(2025, 6, 20));

    when(installmentRules.checkUserInstallmentExistAndGet(userId, List.of(1L, 2L)))
        .thenReturn(List.of(i1, i2));
    when(accountFactory.reCalculateBalanceOnPayment(eq(account), eq(TransactionTypes.PAYMENT), any()))
        .thenReturn(account);

    installmentService.payInstallments(userId, body);

    assertTrue(i1.isPaid());
    assertTrue(i2.isPaid());
    verify(installmentRepository, times(2)).save(any(Installment.class));
    verify(transactionRepository, times(2)).save(transaction);
    verify(accountRepository, times(2)).save(account);
  }
}
