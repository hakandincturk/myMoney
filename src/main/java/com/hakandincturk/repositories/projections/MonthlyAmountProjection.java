package com.hakandincturk.repositories.projections;

import java.math.BigDecimal;

/**
 * Ay bazlı tek tutar toplamı taşıyan genel projeksiyon.
 */
public record MonthlyAmountProjection(
  Integer year,
  Integer month,
  BigDecimal amount
) {}
