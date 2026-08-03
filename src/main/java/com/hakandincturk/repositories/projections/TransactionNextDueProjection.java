package com.hakandincturk.repositories.projections;

import java.time.LocalDate;

/**
 * Bir işlemin ödenmemiş ilk taksit tarihi; taksitli tekrar eden kalemlerin
 * bir sonraki beklenen ödeme tarihini verir.
 */
public record TransactionNextDueProjection(
  Long transactionId,
  LocalDate nextDueDate
) {}
