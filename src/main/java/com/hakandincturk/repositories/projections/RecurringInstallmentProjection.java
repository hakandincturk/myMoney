package com.hakandincturk.repositories.projections;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tekrar eden harcama tespiti için pencere içindeki ham taksit satırları.
 * İsim normalizasyonu ve gruplama servis katmanında yapılır.
 */
public record RecurringInstallmentProjection(
  Long transactionId,
  String transactionName,
  Integer totalInstallment,
  BigDecimal amount,
  LocalDate debtDate
) {}
