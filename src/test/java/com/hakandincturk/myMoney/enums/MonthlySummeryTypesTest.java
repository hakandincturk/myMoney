package com.hakandincturk.myMoney.enums;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.MonthlySummeryTypes;

class MonthlySummeryTypesTest {

  @Test
  @DisplayName("Aylık özet tiplerinin numerik değerleri benzersiz olmalı")
  void values_shouldBeUnique() {
    long distinctValueCount = Arrays.stream(MonthlySummeryTypes.values())
        .map(MonthlySummeryTypes::getValue)
        .collect(Collectors.toSet())
        .size();

    assertEquals(MonthlySummeryTypes.values().length, distinctValueCount);
  }

  @Test
  @DisplayName("Aylık özet tipleri beklenen değerleri taşımalı")
  void values_shouldMatchExpectedNumbers() {
    assertEquals(1, MonthlySummeryTypes.TRANSACTION.getValue());
    assertEquals(2, MonthlySummeryTypes.PAYMENT.getValue());
  }
}
