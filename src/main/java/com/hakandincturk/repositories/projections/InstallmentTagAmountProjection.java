package com.hakandincturk.repositories.projections;

import java.math.BigDecimal;

/**
 * Taksit - etiket ilişkisinin düz (flat) hali.
 * Etiketi olmayan taksitlerde tagId ve tagName null gelir; bu satırlar UNTAGGED grubunu oluşturur.
 * Aynı taksit birden fazla etikete sahipse taksit başına birden fazla satır döner.
 */
public record InstallmentTagAmountProjection(
  Long installmentId,
  Long transactionId,
  BigDecimal amount,
  Long tagId,
  String tagName
) {}
