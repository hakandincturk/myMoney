package com.hakandincturk.repositories.projections;

import java.math.BigDecimal;

import com.hakandincturk.core.enums.TransactionTypes;

/**
 * Ay + hareket tipi + ödeme durumu kırılımındaki taksit tutarı toplamı.
 * Gelir/gider sınıflandırması bilerek SQL'e gömülmez; toplamlar ham kırılımda çekilip
 * TransactionClassifier üzerinden sınıflandırılır, böylece tek bir kural kaynağı kalır.
 */
public record MonthlyTypeAmountProjection(
  Integer year,
  Integer month,
  TransactionTypes type,
  Boolean paid,
  BigDecimal amount
) {}
