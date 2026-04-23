package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.core.exception.ValidationException;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.rules.AccountRules;
import com.hakandincturk.services.rules.ContactRules;
import com.hakandincturk.services.rules.TransactionRules;
import com.hakandincturk.services.rules.UserRules;

@ExtendWith(MockitoExtension.class)
class TransactionRulesTest {

  @InjectMocks
  private TransactionRules transactionRules;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private UserRules userRules;

  @Mock
  private ContactRules contactRules;

  @Mock
  private AccountRules accountRules;

  @Test
  @DisplayName("Geçerli istek olduğunda hata fırlatılmamalı")
  void validateCreateTransactionRequest_shouldNotThrow_whenValid() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setAccountId(1L);
    body.setTotalAmount(BigDecimal.valueOf(100));
    body.setTotalInstallment(3);

    assertDoesNotThrow(() -> transactionRules.validateCreateTransactionRequest(body));
  }

  @Test
  @DisplayName("Hesap ID null olduğunda ValidationException fırlatılmalı")
  void validateCreateTransactionRequest_shouldThrow_whenAccountIdNull() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setAccountId(null);
    body.setTotalAmount(BigDecimal.valueOf(100));
    body.setTotalInstallment(3);

    ValidationException exception = assertThrows(ValidationException.class,
        () -> transactionRules.validateCreateTransactionRequest(body));

    assertEquals("Hesap boş olamaz", exception.getMessage());
  }

  @Test
  @DisplayName("Toplam tutar null olduğunda ValidationException fırlatılmalı")
  void validateCreateTransactionRequest_shouldThrow_whenTotalAmountNull() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setAccountId(1L);
    body.setTotalAmount(null);
    body.setTotalInstallment(3);

    ValidationException exception = assertThrows(ValidationException.class,
        () -> transactionRules.validateCreateTransactionRequest(body));

    assertEquals("Toplam miktar 0'dan büyük olmak zorunda", exception.getMessage());
  }

  @Test
  @DisplayName("Toplam tutar 0 olduğunda ValidationException fırlatılmalı")
  void validateCreateTransactionRequest_shouldThrow_whenTotalAmountZero() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setAccountId(1L);
    body.setTotalAmount(BigDecimal.ZERO);
    body.setTotalInstallment(3);

    ValidationException exception = assertThrows(ValidationException.class,
        () -> transactionRules.validateCreateTransactionRequest(body));

    assertEquals("Toplam miktar 0'dan büyük olmak zorunda", exception.getMessage());
  }

  @Test
  @DisplayName("Toplam tutar negatif olduğunda ValidationException fırlatılmalı")
  void validateCreateTransactionRequest_shouldThrow_whenTotalAmountNegative() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setAccountId(1L);
    body.setTotalAmount(BigDecimal.valueOf(-50));
    body.setTotalInstallment(3);

    ValidationException exception = assertThrows(ValidationException.class,
        () -> transactionRules.validateCreateTransactionRequest(body));

    assertEquals("Toplam miktar 0'dan büyük olmak zorunda", exception.getMessage());
  }

  @Test
  @DisplayName("Taksit sayısı negatif olduğunda ValidationException fırlatılmalı")
  void validateCreateTransactionRequest_shouldThrow_whenTotalInstallmentNegative() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setAccountId(1L);
    body.setTotalAmount(BigDecimal.valueOf(100));
    body.setTotalInstallment(-1);

    ValidationException exception = assertThrows(ValidationException.class,
        () -> transactionRules.validateCreateTransactionRequest(body));

    assertEquals("Taksit miktaro 0'dan küçük olamaz", exception.getMessage());
  }

  @Test
  @DisplayName("Contact ID verildiğinde contact döndürülmeli")
  void getValidatedContact_shouldReturnContact_whenContactIdNotNull() {
    Long userId = 1L;
    Long contactId = 5L;
    Contact contact = new Contact();
    contact.setId(contactId);

    when(contactRules.checkUserContactExistAndGet(userId, contactId)).thenReturn(contact);

    Contact result = transactionRules.getValidatedContact(userId, contactId);

    assertNotNull(result);
    assertEquals(contactId, result.getId());
  }

  @Test
  @DisplayName("Contact ID null olduğunda null döndürülmeli")
  void getValidatedContact_shouldReturnNull_whenContactIdNull() {
    Contact result = transactionRules.getValidatedContact(1L, null);

    assertNull(result);
    verifyNoInteractions(contactRules);
  }

  @Test
  @DisplayName("Account ID verildiğinde account döndürülmeli")
  void getValidatedAccount_shouldReturnAccount_whenAccountIdNotNull() {
    Long userId = 1L;
    Long accountId = 10L;
    Account account = new Account();
    account.setId(accountId);

    when(accountRules.checkUserAccountExistAndGet(userId, accountId)).thenReturn(account);

    Account result = transactionRules.getValidatedAccount(userId, accountId);

    assertNotNull(result);
    assertEquals(accountId, result.getId());
  }

  @Test
  @DisplayName("Account ID null olduğunda null döndürülmeli")
  void getValidatedAccount_shouldReturnNull_whenAccountIdNull() {
    Account result = transactionRules.getValidatedAccount(1L, null);

    assertNull(result);
    verifyNoInteractions(accountRules);
  }

  @Test
  @DisplayName("User ID verildiğinde user döndürülmeli")
  void getValidatedUser_shouldReturnUser_whenUserIdNotNull() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    when(userRules.checkUserExistAndGet(userId)).thenReturn(user);

    Users result = transactionRules.getValidatedUser(userId);

    assertNotNull(result);
    assertEquals(userId, result.getId());
  }

  @Test
  @DisplayName("User ID null olduğunda null döndürülmeli")
  void getValidatedUser_shouldReturnNull_whenUserIdNull() {
    Users result = transactionRules.getValidatedUser(null);

    assertNull(result);
    verifyNoInteractions(userRules);
  }

  @Test
  @DisplayName("Transaction mevcut olduğunda başarıyla döndürülmeli")
  void checkUserTransactionExistAndGet_shouldReturnTransaction_whenExists() {
    Long userId = 1L;
    Long transactionId = 100L;
    Transaction transaction = new Transaction();
    transaction.setId(transactionId);

    when(transactionRepository.findByIdAndUserIdAndIsRemovedFalse(transactionId, userId))
        .thenReturn(Optional.of(transaction));

    Transaction result = transactionRules.checkUserTransactionExistAndGet(userId, transactionId);

    assertNotNull(result);
    assertEquals(transactionId, result.getId());
  }

  @Test
  @DisplayName("Transaction bulunamadığında NotFoundException fırlatılmalı")
  void checkUserTransactionExistAndGet_shouldThrowNotFoundException_whenNotExists() {
    Long userId = 1L;
    Long transactionId = 100L;

    when(transactionRepository.findByIdAndUserIdAndIsRemovedFalse(transactionId, userId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> transactionRules.checkUserTransactionExistAndGet(userId, transactionId));

    assertEquals("Borç bulunmadı", exception.getMessage());
  }
}
