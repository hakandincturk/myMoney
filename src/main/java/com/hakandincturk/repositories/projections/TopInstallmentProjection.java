package com.hakandincturk.repositories.projections;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ayın en büyük kalemleri için taksit satırı detayı.
 * Sıralama işlemin toplam tutarına göre değil, taksitin o aya düşen tutarına göre yapılır.
 */
public record TopInstallmentProjection(
  Long installmentId,
  Long transactionId,
  String transactionName,
  String transactionDescription,
  String installmentDescription,
  BigDecimal amount,
  LocalDate debtDate,
  String accountName,
  String contactName,
  Integer installmentNumber,
  Integer totalInstallment,
  Boolean paid
) {}
