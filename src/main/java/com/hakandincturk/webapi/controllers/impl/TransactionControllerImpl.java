package com.hakandincturk.webapi.controllers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.concretes.TransactionController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/transaction")
@Tag(name = "Transaction", description = "Transaction işlemleri")
public class TransactionControllerImpl extends BaseController implements TransactionController {

  @Autowired
  private TransactionService transactionService;

  @Override
  @PostMapping(value = "/")
  @Operation(summary = "Create Transaction and installments", description = "Yeni borç girişi oluşturulmasını sağlar")
  public ApiResponse<?> createTransaction(CreateTransactionRequestDto body) {
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
  
}
