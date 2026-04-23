package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.CreateTransactionTagDetail;
import com.hakandincturk.dtos.transaction.request.TagTransactionsFilterRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.dtos.transaction.response.ListTagTransactionsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.webapi.controllers.impl.TransactionControllerImpl;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

  @InjectMocks
  private TransactionControllerImpl controller;

  @Mock
  private TransactionService transactionService;

  private static final Long USER_ID = 1L;

  @BeforeEach
  void setUpSecurity() {
    JwtAuthentication auth = new JwtAuthentication("test@test.com", null, List.of(), USER_ID);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Transaction oluşturma - başarılı")
  void createTransaction_shouldReturnSuccess() {
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(1000));
    body.setAccountId(10L);
    body.setDebtDate(LocalDate.now());
    body.setName("Test");
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    ApiResponse<?> response = controller.createTransaction(body);

    assertTrue(response.isType());
    verify(transactionService).createTransaction(USER_ID, body);
  }

  @Test
  @DisplayName("Transaction oluşturma - auth başarısız")
  void createTransaction_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass"));

    ApiResponse<?> response = controller.createTransaction(new CreateTransactionRequestDto());

    assertFalse(response.isType());
  }

  @Test
  @DisplayName("Transaction listeleme - başarılı")
  void listMyTransactions_shouldReturnSuccess() {
    Page<ListMyTransactionsResponseDto> page = new PageImpl<>(List.of(new ListMyTransactionsResponseDto()));
    when(transactionService.listMyTransactions(eq(USER_ID), any())).thenReturn(page);

    TransactionFilterRequestDto pageData = new TransactionFilterRequestDto();
    ApiResponse<PagedResponse<ListMyTransactionsResponseDto>> response = controller.listMyTransactions(pageData);

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Transaction silme - başarılı")
  void deleteMyTransaction_shouldReturnSuccess() {
    ApiResponse<?> response = controller.deleteMyTransaction(100L);

    assertTrue(response.isType());
    verify(transactionService).deleteMyTransaction(USER_ID, 100L);
  }

  @Test
  @DisplayName("Transaction taksitleri listeleme - başarılı")
  void listTransactionInstallments_shouldReturnSuccess() {
    when(transactionService.listTransactionInstallments(USER_ID, 100L))
        .thenReturn(List.of(new ListInstallments()));

    ApiResponse<List<ListInstallments>> response = controller.listTransactionInstallments(100L);

    assertTrue(response.isType());
    assertEquals(1, response.getData().size());
  }

  @Test
  @DisplayName("Tag transaction listeleme - başarılı")
  void listTagTransactions_shouldReturnSuccess() {
    Page<ListTagTransactionsResponseDto> page = new PageImpl<>(List.of(new ListTagTransactionsResponseDto()));
    when(transactionService.listTagTransactions(eq(USER_ID), eq(5L), any())).thenReturn(page);

    TagTransactionsFilterRequestDto body = new TagTransactionsFilterRequestDto();
    ApiResponse<PagedResponse<ListTagTransactionsResponseDto>> response = controller.listTagTransactions(5L, body);

    assertTrue(response.isType());
  }
}
