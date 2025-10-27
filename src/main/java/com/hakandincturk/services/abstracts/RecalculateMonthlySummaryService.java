package com.hakandincturk.services.abstracts;

import java.time.LocalDate;
import java.util.List;

import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;

public interface RecalculateMonthlySummaryService {
  void reCalculteAfterInstallmentPayment(Users installmentUser, List<Installment> installments, LocalDate paidDate);
  void reCalculateAfterTransactionCreate(Transaction transaction);
}
