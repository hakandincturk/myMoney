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

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.events.InstallmentUpdatedEvent;
import com.hakandincturk.core.events.InstallmentsPaidEvent;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.UpdateInstallmentRequestDto;
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

  // --- updateInstallment tests ---

  private Transaction createTransactionWithInstallments(Users user, BigDecimal... amounts) {
    Transaction transaction = new Transaction();
    transaction.setId(100L);
    transaction.setType(TransactionTypes.DEBT);
    transaction.setUser(user);
    transaction.setTotalAmount(BigDecimal.ZERO);
    transaction.setPaidAmount(BigDecimal.ZERO);

    List<Installment> installments = new java.util.ArrayList<>();
    for (int i = 0; i < amounts.length; i++) {
      Installment inst = new Installment();
      inst.setId((long) (i + 1));
      inst.setAmount(amounts[i]);
      inst.setPaid(false);
      inst.setRemoved(false);
      inst.setStatus(InstallmentStatuses.ACTIVE);
      inst.setDebtDate(LocalDate.of(2025, 6 + i, 15));
      inst.setTransaction(transaction);
      installments.add(inst);
    }
    transaction.setInstallments(installments);

    BigDecimal total = java.util.Arrays.stream(amounts).reduce(BigDecimal.ZERO, BigDecimal::add);
    transaction.setTotalAmount(total);

    return transaction;
  }

  @Test
  @DisplayName("Taksit tutarı güncellendiğinde transaction totalAmount yeniden hesaplanmalı")
  void updateInstallment_shouldRecalculateTotalAmount_whenAmountChanged() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    Transaction transaction = createTransactionWithInstallments(user,
        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

    Installment targetInstallment = transaction.getInstallments().get(1);

    UpdateInstallmentRequestDto body = new UpdateInstallmentRequestDto(BigDecimal.valueOf(800), null);

    when(installmentRules.checkUserSingleInstallmentExistAndGet(userId, 2L))
        .thenReturn(targetInstallment);

    installmentService.updateInstallment(userId, 2L, body);

    assertEquals(BigDecimal.valueOf(800), targetInstallment.getAmount());
    assertEquals(BigDecimal.valueOf(2800), transaction.getTotalAmount());
    verify(installmentRepository).save(targetInstallment);
    verify(transactionRepository).save(transaction);
    verify(eventPublisher).publishEvent(any(InstallmentUpdatedEvent.class));
  }

  @Test
  @DisplayName("Taksit SKIPPED yapıldığında transaction totalAmount SKIPPED hariç hesaplanmalı")
  void updateInstallment_shouldExcludeSkippedFromTotalAmount() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    Transaction transaction = createTransactionWithInstallments(user,
        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

    Installment targetInstallment = transaction.getInstallments().get(1);

    UpdateInstallmentRequestDto body = new UpdateInstallmentRequestDto(null, InstallmentStatuses.SKIPPED);

    when(installmentRules.checkUserSingleInstallmentExistAndGet(userId, 2L))
        .thenReturn(targetInstallment);

    installmentService.updateInstallment(userId, 2L, body);

    assertEquals(InstallmentStatuses.SKIPPED, targetInstallment.getStatus());
    assertEquals(BigDecimal.valueOf(2000), transaction.getTotalAmount());
    assertEquals(TransactionStatuses.PENDING, transaction.getStatus());
    verify(eventPublisher).publishEvent(any(InstallmentUpdatedEvent.class));
  }

  @Test
  @DisplayName("SKIPPED taksit ACTIVE yapıldığında transaction totalAmount tekrar artmalı")
  void updateInstallment_shouldIncludeReactivatedInTotalAmount() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    Transaction transaction = createTransactionWithInstallments(user,
        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

    Installment targetInstallment = transaction.getInstallments().get(1);
    targetInstallment.setStatus(InstallmentStatuses.SKIPPED);

    UpdateInstallmentRequestDto body = new UpdateInstallmentRequestDto(null, InstallmentStatuses.ACTIVE);

    when(installmentRules.checkUserSingleInstallmentExistAndGet(userId, 2L))
        .thenReturn(targetInstallment);

    installmentService.updateInstallment(userId, 2L, body);

    assertEquals(InstallmentStatuses.ACTIVE, targetInstallment.getStatus());
    assertEquals(BigDecimal.valueOf(3000), transaction.getTotalAmount());
  }

  @Test
  @DisplayName("Ödenmiş taksitler varken SKIPPED yapılınca transaction status doğru hesaplanmalı")
  void updateInstallment_shouldCalculateCorrectStatus_whenSomePaidAndOneSkipped() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    Transaction transaction = createTransactionWithInstallments(user,
        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

    transaction.getInstallments().get(0).setPaid(true);

    Installment targetInstallment = transaction.getInstallments().get(1);

    UpdateInstallmentRequestDto body = new UpdateInstallmentRequestDto(null, InstallmentStatuses.SKIPPED);

    when(installmentRules.checkUserSingleInstallmentExistAndGet(userId, 2L))
        .thenReturn(targetInstallment);

    installmentService.updateInstallment(userId, 2L, body);

    assertEquals(BigDecimal.valueOf(2000), transaction.getTotalAmount());
    assertEquals(BigDecimal.valueOf(1000), transaction.getPaidAmount());
    assertEquals(TransactionStatuses.PARTIAL, transaction.getStatus());
  }

  @Test
  @DisplayName("Tüm aktif taksitler ödenmişken kalan taksit SKIPPED yapılınca status PAID olmalı")
  void updateInstallment_shouldSetStatusPaid_whenAllActiveInstallmentsPaid() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    Transaction transaction = createTransactionWithInstallments(user,
        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

    transaction.getInstallments().get(0).setPaid(true);

    Installment targetInstallment = transaction.getInstallments().get(1);

    UpdateInstallmentRequestDto body = new UpdateInstallmentRequestDto(null, InstallmentStatuses.SKIPPED);

    when(installmentRules.checkUserSingleInstallmentExistAndGet(userId, 2L))
        .thenReturn(targetInstallment);

    installmentService.updateInstallment(userId, 2L, body);

    assertEquals(BigDecimal.valueOf(1000), transaction.getTotalAmount());
    assertEquals(BigDecimal.valueOf(1000), transaction.getPaidAmount());
    assertEquals(TransactionStatuses.PAID, transaction.getStatus());
  }

  @Test
  @DisplayName("Hem tutar hem status aynı anda güncellenebilmeli")
  void updateInstallment_shouldUpdateBothAmountAndStatus() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    Transaction transaction = createTransactionWithInstallments(user,
        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

    Installment targetInstallment = transaction.getInstallments().get(0);

    UpdateInstallmentRequestDto body = new UpdateInstallmentRequestDto(BigDecimal.valueOf(500), InstallmentStatuses.ACTIVE);

    when(installmentRules.checkUserSingleInstallmentExistAndGet(userId, 1L))
        .thenReturn(targetInstallment);

    installmentService.updateInstallment(userId, 1L, body);

    assertEquals(BigDecimal.valueOf(500), targetInstallment.getAmount());
    assertEquals(InstallmentStatuses.ACTIVE, targetInstallment.getStatus());
    assertEquals(BigDecimal.valueOf(1500), transaction.getTotalAmount());
  }
}
