package com.hakandincturk.services.impl;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.sort.InstallmentSortColumn;
import com.hakandincturk.core.specs.FilterListMyInstallmentSpecification;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecificDateInstallmentsResponseDto;
import com.hakandincturk.dtos.installment.response.TransactionDetailDto;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.services.rules.InstallmentRules;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {

  private final InstallmentRepository installmentRepository;
  private final TransactionRepository transactionRepository;
  private final InstallmentRules installmentRules;
  private final AccountFactory accountFactory;
  private final AccountRepository accountRepository;

  @Override
  public Page<ListMySpecificDateInstallmentsResponseDto> listMySpecisifDateInstallments(Long userId, FilterListMyInstallmentRequestDto pageData) {
    Pageable pageable = PaginationUtils.toPageable(pageData, InstallmentSortColumn.class);
    Specification<Installment> specs = FilterListMyInstallmentSpecification.filter(userId, pageData);

    Page<Installment> dbInstallments = installmentRepository.findAll(specs, pageable);

    Page<ListMySpecificDateInstallmentsResponseDto> installments = dbInstallments.map(installment -> {
      TransactionDetailDto transactionDetail = new TransactionDetailDto(
        installment.getTransaction().getId(),
        installment.getTransaction().getName(),
        installment.getTransaction().getType()
      );

      return new ListMySpecificDateInstallmentsResponseDto(
        installment.getId(),
        transactionDetail,
        installment.getAmount(),
        installment.getDebtDate(),
        installment.getInstallmentNumber(),
        installment.getDescripton(),
        installment.isPaid(),
        installment.getPaidDate()
      );
    });

    return installments;
  }

  @Override
  @Transactional
  public void payInstallment(Long userId, Long installmentId, PayInstallmentRequestDto body) {
    Installment installment = installmentRules.checkUserInstallmentExistAndGet(userId, installmentId);

    installment.setPaid(true);
    installment.setPaidDate(body.getPaidDate());
    installmentRepository.save(installment);

    
    Transaction transaction = installment.getTransaction();

    BigDecimal totalPaidAmount = installment.getTransaction().getPaidAmount().add(installment.getAmount());
    TransactionStatuses transactionStatuses = transaction.getTotalAmount().equals(totalPaidAmount) ? TransactionStatuses.PAID : TransactionStatuses.PARTIAL;

    transaction.setPaidAmount(totalPaidAmount);
    transaction.setStatus(transactionStatuses);
    transactionRepository.save(transaction);

    Account account = accountFactory.reCalculateBalanceOnPayment(transaction.getAccount(), transaction.getType(), installment.getAmount());
    accountRepository.save(account);
  }
  
}
