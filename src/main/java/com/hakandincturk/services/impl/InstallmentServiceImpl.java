package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.sort.InstallmentSortColumn;
import com.hakandincturk.core.events.PayInstallmentEvent;
import com.hakandincturk.core.specs.FilterListMyInstallmentSpecification;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecificDateInstallmentsResponseDto;
import com.hakandincturk.dtos.installment.response.TransactionDetailDto;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
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
  private final MonthlySummaryRepository monthlySummaryRepository;
  private final ApplicationEventPublisher eventPublisher;

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
  public void payInstallments(Long userId, PayInstallmentRequestDto body) {
    List<Installment> installments = installmentRules.checkUserInstallmentExistAndGet(userId, body.getIds());
    for (Installment installment : installments) {
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
  
      int instalmentYear = installment.getDebtDate().getYear();
      int installmentMonthValue = installment.getDebtDate().getMonthValue();
      List<MonthlySummary> dbMonthlySummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(userId, instalmentYear, installmentMonthValue);
      monthlySummaryRepository.deleteAll(dbMonthlySummary);
  
      if (
        (
          installment.getDebtDate().getMonthValue() != body.getPaidDate().getMonthValue() &&
          installment.getDebtDate().getYear() != body.getPaidDate().getYear()
        ) || 
        (
          installment.getDebtDate().getMonthValue() != body.getPaidDate().getMonthValue() &&
          installment.getDebtDate().getYear() == body.getPaidDate().getYear()
        )
      ) {
        int paidDateYear = body.getPaidDate().getYear();
        int paidDateMonthValue = body.getPaidDate().getMonthValue();
        List<MonthlySummary> dbPaidDateSummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(userId, paidDateYear, paidDateMonthValue);
        monthlySummaryRepository.deleteAll(dbPaidDateSummary); // delete or is_removed: true ?
        eventPublisher.publishEvent(new PayInstallmentEvent(transaction.getUser(), paidDateYear, paidDateMonthValue));
      }
  
      int instalmentPreviousYear = installment.getDebtDate().minusMonths(1).getYear();
      int installmentPreviousMonthValue = installment.getDebtDate().minusMonths(1).getMonthValue();
      if(
        (
          installmentPreviousMonthValue != body.getPaidDate().getMonthValue() &&
          instalmentPreviousYear != body.getPaidDate().getYear()
        ) || 
        (
          installmentPreviousMonthValue != body.getPaidDate().getMonthValue() &&
          instalmentPreviousYear == body.getPaidDate().getYear()
        )
      ){
        List<MonthlySummary> dbPreviousMonthlySummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(userId, instalmentPreviousYear, installmentPreviousMonthValue);
        monthlySummaryRepository.deleteAll(dbPreviousMonthlySummary);
        eventPublisher.publishEvent(new PayInstallmentEvent(transaction.getUser(), instalmentPreviousYear, installmentPreviousMonthValue));
      }
  
      eventPublisher.publishEvent(new PayInstallmentEvent(transaction.getUser(), instalmentYear, installmentMonthValue));
    }
  }
}
