package com.hakandincturk.webapi.controllers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.enums.sort.TransactionSortColumn;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.utils.PaginationUtils;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.concretes.TransactionController;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/transaction")
@Tag(name = "Transaction", description = "Transaction işlemleri")
public class TransactionControllerImpl extends BaseController implements TransactionController {

  @Autowired
  private TransactionService transactionService;

  @Override
  @PostMapping(value = "/")
  @Operation(summary = "Create Transaction and installments", description = "Yeni borç girişi oluşturulmasını sağlar")
  public ApiResponse<?> createTransaction(@Valid @RequestBody CreateTransactionRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      transactionService.createTransaction(userId, body);
      return success("Borç girişi başarıyla tamamlandı", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/my")
  @Operation(summary = "List my transactions", description = "Borçların listelenmesini sağlar")
  public ApiResponse<PagedResponse<ListMyTransactionsResponseDto>> listMyTransactions(@ModelAttribute SortablePageRequest pageData) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      Pageable pageable = PaginationUtils.toPageable(pageData, TransactionSortColumn.class);
      return successPaged("Borçlar başarıyla getirildi", transactionService.listMyTransactions(userId, pageable));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @DeleteMapping(value = "/my/{transactionId}")
  @Operation(summary = "Delete my transactions", description = "Borcun silinmesini sağlar")
  public ApiResponse<?> deleteMyTransaction(@PathVariable(value = "transactionId") Long transactionId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      transactionService.deleteMyTransaction(userId, transactionId);
      return success("Borç başarıyla silindi", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
  
}
