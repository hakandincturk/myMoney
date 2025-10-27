package com.hakandincturk.services.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.events.InstallmentPaidEvent;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.services.abstracts.RecalculateMonthlySummaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecalculateMonthlySummaryServiceImpl implements RecalculateMonthlySummaryService {

  private final MonthlySummaryRepository monthlySummaryRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Ödenen taksitlerin ilgili olduğu ayların özetlerini tekrar hesaplar
   * @param installments Ödenen installmentlar
   * @param paidDate Installment'ın ödendiği tarih
  */
  @Override
  public void reCalculteAfterInstallmentPayment(Users installmentUser, List<Installment> installments, LocalDate paidDate) {
    Set<LocalDate> allEffectedDates = new HashSet<>();
    for(Installment installment : installments){
      Set<LocalDate> installmentEffectedMonths = this.getEffectedMonths(installment, paidDate);
      allEffectedDates.addAll(installmentEffectedMonths);
    }

    for(LocalDate effectedDate : allEffectedDates){
      this.clearOldMonthlySummary(installmentUser, effectedDate.getYear(), effectedDate.getMonthValue());
      eventPublisher.publishEvent(new InstallmentPaidEvent(installmentUser, effectedDate.getYear(), effectedDate.getMonthValue()));
    }
  }

  /**
   * Yeni oluşturulan transactionda ki installmentarın ilgili ayları için aylık özetleri hesaplar
   * @param transaction transaction bilgileri
  */
  @Override
  public void reCalculateAfterTransactionCreate(Transaction transaction){
    Set<LocalDate> allEffectedDates = new HashSet<LocalDate>();
    transaction.getInstallments().forEach(installment -> allEffectedDates.add(installment.getDebtDate()));
    if(transaction.getType() == TransactionTypes.PAYMENT || transaction.getType() == TransactionTypes.CREDIT ){
      allEffectedDates.add(transaction.getInstallments().get(0).getDebtDate().minusMonths(1));
    }

    for (LocalDate effectedDate : allEffectedDates) {
      this.clearOldMonthlySummary(transaction.getUser(), effectedDate.getYear(), effectedDate.getMonthValue());
      eventPublisher.publishEvent(new InstallmentPaidEvent(transaction.getUser(), effectedDate.getYear(), effectedDate.getMonthValue()));
    }
  }

  /**
   * Bir taksit ödendiğinde etkilenen ayları bulur.
   * @param installment Hesap yapılacak installment
   * @param paidDate Installment'ın ödendiği tarih
  */
  private Set<LocalDate> getEffectedMonths(Installment installment, LocalDate paidDate){
    Set<LocalDate> effectedMonths = new HashSet<LocalDate>();
    effectedMonths.add(installment.getDebtDate().withDayOfMonth(1));

    // odenen tarih ile taksit tarihi farkli ise
    if(!installment.getDebtDate().withDayOfMonth(1).isEqual(paidDate.withDayOfMonth(1))){
      effectedMonths.add(paidDate.withDayOfMonth(1));      
    }

    // odenen tarih ile bir onceki ay farkli ise
    LocalDate previousMonth = installment.getDebtDate().minusMonths(1);
    if(!previousMonth.withDayOfMonth(1).isEqual(paidDate.withDayOfMonth(1))){
      effectedMonths.add(previousMonth.withDayOfMonth(1));
    }

    return effectedMonths;
  }

  /**
   * Belirtilen kullanıcının o ay ve yıldaki aylık özeti siler
   * @param user Aylık özeti silinecek olan kullanıcı
   * @param year Silinecek yıl
   * @param month Silinecek ay
  */
  private void clearOldMonthlySummary(Users user, int year, int month){
    List<MonthlySummary> monthlySummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(user.getId(), year, month);
    monthlySummaryRepository.deleteAll(monthlySummary);
  }

}
