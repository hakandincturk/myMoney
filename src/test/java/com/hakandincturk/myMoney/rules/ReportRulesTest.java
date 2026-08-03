package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.exception.ValidationException;
import com.hakandincturk.services.rules.ReportRules;

class ReportRulesTest {

  private final ReportRules reportRules = new ReportRules();

  @Test
  @DisplayName("Geçerli dönem hata fırlatmamalı")
  void checkPeriodIsValid_shouldPass_whenPeriodIsValid() {
    assertDoesNotThrow(() -> reportRules.checkPeriodIsValid(2026, 6));
  }

  @Test
  @DisplayName("Ay 12'den büyükse hata fırlatmalı")
  void checkPeriodIsValid_shouldThrow_whenMonthIsTooBig() {
    ValidationException exception = assertThrows(ValidationException.class, () -> reportRules.checkPeriodIsValid(2026, 13));

    assertEquals("Ay bilgisi 1 ile 12 arasında olmalıdır", exception.getMessage());
  }

  @Test
  @DisplayName("Ay 1'den küçükse hata fırlatmalı")
  void checkPeriodIsValid_shouldThrow_whenMonthIsTooSmall() {
    assertThrows(ValidationException.class, () -> reportRules.checkPeriodIsValid(2026, 0));
  }

  @Test
  @DisplayName("Yıl makul aralık dışındaysa hata fırlatmalı")
  void checkPeriodIsValid_shouldThrow_whenYearIsOutOfRange() {
    assertThrows(ValidationException.class, () -> reportRules.checkPeriodIsValid(1999, 6));
    assertThrows(ValidationException.class, () -> reportRules.checkPeriodIsValid(2101, 6));
  }

  @Test
  @DisplayName("Yıl veya ay boş ise hata fırlatmalı")
  void checkPeriodIsValid_shouldThrow_whenPeriodIsNull() {
    assertThrows(ValidationException.class, () -> reportRules.checkPeriodIsValid(null, 6));
    assertThrows(ValidationException.class, () -> reportRules.checkPeriodIsValid(2026, null));
  }
}
