package com.hakandincturk.repositories.projections;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gün bazlı tutar toplamı; ayın en yoğun gününü bulmak için kullanılır.
 */
public record DailyAmountProjection(
  LocalDate date,
  BigDecimal amount
) {}
