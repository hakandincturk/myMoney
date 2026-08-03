package com.hakandincturk.utils;

import java.util.List;

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.ReportFlowTypes;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Installment;

import lombok.experimental.UtilityClass;

/**
 * Gelir/gider sınıflandırmasının tek kaynağı.
 * Aylık özet (MonthlySummeryFactory), dashboard ve rapor modülü aynı kuralı kullanmak
 * zorundadır; aksi halde kullanıcı aynı veriyi farklı ekranlarda farklı görür.
 */
@UtilityClass
public class TransactionClassifier {

  // Gelir sayılan hareket tipleri - Income transaction types
  public static final List<TransactionTypes> INCOME_TYPES = List.of(TransactionTypes.CREDIT, TransactionTypes.COLLECTION);

  // Gider sayılan hareket tipleri - Expense transaction types
  public static final List<TransactionTypes> EXPENSE_TYPES = List.of(TransactionTypes.DEBT, TransactionTypes.PAYMENT);

  public static boolean isIncome(TransactionTypes type){
    return INCOME_TYPES.contains(type);
  }

  public static boolean isExpense(TransactionTypes type){
    return EXPENSE_TYPES.contains(type);
  }

  public static boolean isIncomeInstallment(Installment installment){
    return isIncome(installment.getTransaction().getType());
  }

  public static boolean isExpenseInstallment(Installment installment){
    return isExpense(installment.getTransaction().getType());
  }

  /**
   * Ödenmeyecek (SKIPPED) taksitler hiçbir toplama dahil edilmez.
   */
  public static boolean isActiveInstallment(Installment installment){
    return installment.getStatus() != InstallmentStatuses.SKIPPED;
  }

  public static boolean isPaidInstallment(Installment installment){
    return installment.isPaid();
  }

  public static boolean isUnpaidInstallment(Installment installment){
    return !installment.isPaid();
  }

  /**
   * Rapor uçlarındaki EXPENSE/INCOME parametresini hareket tiplerine çevirir.
   */
  public static List<TransactionTypes> typesOf(ReportFlowTypes flowType){
    return flowType == ReportFlowTypes.INCOME ? INCOME_TYPES : EXPENSE_TYPES;
  }
}
