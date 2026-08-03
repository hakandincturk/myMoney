package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.hakandincturk.core.handler.GlobalExceptionHandler;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.core.exception.ValidationException;
import com.hakandincturk.services.rules.DashboardRules;



@ExtendWith(MockitoExtension.class)
class DashboardRulesTest {

  @InjectMocks
  private DashboardRules dashboardRules;

  @Test
  @DisplayName("Başlangıç tarihi bitiş tarihinden önce olduğunda hata fırlatılmamalı")
  void tagSummaryDatesControl_shouldNotThrow_whenStartDateBeforeEndDate() {
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 1, 31)
    );

    assertDoesNotThrow(() -> dashboardRules.tagSummaryDatesControl(body));
  }

  @Test
  @DisplayName("Bitiş tarihi başlangıç tarihinden önce olduğunda ValidationException fırlatılmalı")
  void tagSummaryDatesControl_shouldThrowValidationException_whenEndDateBeforeStartDate() {
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 6, 1),
        LocalDate.of(2025, 1, 31)
    );

    ValidationException exception = assertThrows(ValidationException.class,
        () -> dashboardRules.tagSummaryDatesControl(body));

    assertEquals("Bitiş tarihi, başlangıç tarihinden daha önce olamaz", exception.getMessage());
  }

  @Test
  @DisplayName("Başlangıç ve bitiş tarihleri aynı olduğunda hata fırlatılmamalı")
  void tagSummaryDatesControl_shouldNotThrow_whenDatesAreEqual() {
    LocalDate sameDate = LocalDate.of(2025, 3, 15);
    TagSummaryRequestDto body = new TagSummaryRequestDto(sameDate, sameDate);

    assertDoesNotThrow(() -> dashboardRules.tagSummaryDatesControl(body));
  }

  @Test
  @DisplayName("Aylık tarih aralığı olduğunda hata fırlatılmamalı")
  void tagSummaryDatesOnly1MonthOr1Year_shouldNotThrow_whenMonthlyRange() {
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 3, 1),
        LocalDate.of(2025, 3, 31)
    );

    assertDoesNotThrow(() -> dashboardRules.tagSummaryDatesOnly1MonthOr1Year(body));
  }

  @Test
  @DisplayName("Yıllık tarih aralığı olduğunda hata fırlatılmamalı")
  void tagSummaryDatesOnly1MonthOr1Year_shouldNotThrow_whenYearlyRange() {
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 12, 31)
    );

    assertDoesNotThrow(() -> dashboardRules.tagSummaryDatesOnly1MonthOr1Year(body));
  }

  @Test
  @DisplayName("Geçersiz tarih aralığı olduğunda ValidationException fırlatılmalı")
  void tagSummaryDatesOnly1MonthOr1Year_shouldThrowValidationException_whenInvalidRange() {
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 3, 15),
        LocalDate.of(2025, 6, 30)
    );

    ValidationException exception = assertThrows(ValidationException.class,
        () -> dashboardRules.tagSummaryDatesOnly1MonthOr1Year(body));

    assertEquals("Başlangıç ve bitiş tarihleri aylık veya yıllık olmalı", exception.getMessage());
  }

  @Test
  @DisplayName("Dashboard validasyon hatası global handler üzerinden 400 dönmeli")
  void validationException_shouldBeHandledAsBadRequest() {
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 6, 30),
        LocalDate.of(2025, 6, 1)
    );

    ValidationException exception = assertThrows(ValidationException.class,
        () -> dashboardRules.tagSummaryDatesControl(body));

    ResponseEntity<ApiResponse<?>> response = new GlobalExceptionHandler()
        .handleBusinessException(exception, new ServletWebRequest(new MockHttpServletRequest()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertFalse(response.getBody().isType());
  }
}
