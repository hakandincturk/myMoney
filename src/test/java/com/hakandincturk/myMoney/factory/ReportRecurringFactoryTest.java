package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.RecurringKinds;
import com.hakandincturk.dtos.report.response.RecurringItemDto;
import com.hakandincturk.dtos.report.response.ReportRecurringResponseDto;
import com.hakandincturk.factories.ReportRecurringFactory;
import com.hakandincturk.repositories.projections.RecurringInstallmentProjection;
import com.hakandincturk.utils.ReportPeriodUtils;

class ReportRecurringFactoryTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);
  private static final int MIN_OCCURRENCE = 3;

  private final ReportRecurringFactory factory = new ReportRecurringFactory();

  private final List<YearMonth> windowMonths = ReportPeriodUtils.monthsBetween(YearMonth.of(2026, 1), YearMonth.of(2026, 6));

  @Test
  @DisplayName("Taksitli işlem INSTALLMENT olur ve isim grubuna tekrar dahil edilmez")
  void build_shouldNotDoubleCountInstallmentAndRepeatedGroups() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(1L, "Netflix", 12, 229, LocalDate.of(2026, 1, 5)),
        row(1L, "Netflix", 12, 229, LocalDate.of(2026, 2, 5)),
        row(1L, "Netflix", 12, 229, LocalDate.of(2026, 3, 5)),
        row(1L, "Netflix", 12, 229, LocalDate.of(2026, 4, 5)),
        row(1L, "Netflix", 12, 229, LocalDate.of(2026, 5, 5)),
        row(1L, "Netflix", 12, 229, LocalDate.of(2026, 6, 5)),
        row(2L, "Netflix", 1, 100, LocalDate.of(2026, 4, 20)),
        row(3L, "Netflix", 1, 100, LocalDate.of(2026, 5, 20)),
        row(4L, "Netflix", 1, 100, LocalDate.of(2026, 6, 20))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 10, TODAY, BigDecimal.valueOf(10000));

    assertEquals(2, response.getItems().size());

    RecurringItemDto installmentGroup = itemOf(response, "TX:1");
    RecurringItemDto repeatedGroup = itemOf(response, "NAME:netflix");

    assertEquals(RecurringKinds.INSTALLMENT, installmentGroup.getKind());
    assertEquals(RecurringKinds.REPEATED, repeatedGroup.getKind());

    // Taksitli işlemin tutarları isim grubunda tekrar sayılmamalı
    assertEquals(0, BigDecimal.valueOf(1374).compareTo(installmentGroup.getTotalAmount()));
    assertEquals(0, BigDecimal.valueOf(300).compareTo(repeatedGroup.getTotalAmount()));

    BigDecimal totalOfGroups = response.getItems().stream()
        .map(RecurringItemDto::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, BigDecimal.valueOf(1674).compareTo(totalOfGroups));
  }

  @Test
  @DisplayName("Yeterli ayda tekrar etmeyen isim grubu listelenmemeli")
  void build_shouldSkipRepeatedGroupBelowMinOccurrence() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(2L, "Spotify", 1, 60, LocalDate.of(2026, 5, 10)),
        row(3L, "Spotify", 1, 60, LocalDate.of(2026, 6, 10))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 10, TODAY, BigDecimal.valueOf(10000));

    assertTrue(response.getItems().isEmpty());
    assertEquals(0, BigDecimal.ZERO.compareTo(response.getMonthlyFixedCost()));
    assertEquals(2026, response.getWindowStart().getYear());
    assertEquals(1, response.getWindowStart().getMonth());
    assertEquals("JANUARY", response.getWindowStart().getLabel());
    assertEquals(6, response.getWindowEnd().getMonth());
    assertEquals("JUNE", response.getWindowEnd().getLabel());
  }

  @Test
  @DisplayName("İsim normalizasyonu kırpma, Türkçe küçültme ve boşluk sadeleştirme yapmalı")
  void build_shouldNormalizeNamesWithTurkishLocale() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(2L, "  NETFLİX  TV ", 1, 100, LocalDate.of(2026, 4, 20)),
        row(3L, "netflix tv", 1, 100, LocalDate.of(2026, 5, 20)),
        row(4L, "Netflİx   tv", 1, 100, LocalDate.of(2026, 6, 20))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 10, TODAY, BigDecimal.valueOf(10000));

    assertEquals(1, response.getItems().size());
    assertEquals("NAME:netflix tv", response.getItems().get(0).getGroupKey());
    assertEquals(3, response.getItems().get(0).getMonthsSpan());
    assertEquals(3, response.getItems().get(0).getOccurrenceCount());
  }

  @Test
  @DisplayName("amountByMonth penceredeki her ay için üretilmeli, boş aylar 0 olmalı")
  void build_shouldFillAmountByMonthForEveryWindowMonth() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(1L, "Kredi", 12, 500, LocalDate.of(2026, 5, 1)),
        row(1L, "Kredi", 12, 500, LocalDate.of(2026, 6, 1))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 10, TODAY, BigDecimal.valueOf(10000));

    RecurringItemDto item = response.getItems().get(0);
    assertEquals(6, item.getAmountByMonth().size());
    assertEquals(0, BigDecimal.ZERO.compareTo(item.getAmountByMonth().get(0).getAmount()));
    assertEquals(2026, item.getAmountByMonth().get(0).getYear());
    assertEquals(1, item.getAmountByMonth().get(0).getMonth());
    assertEquals(0, BigDecimal.valueOf(500).compareTo(item.getAmountByMonth().get(5).getAmount()));
  }

  @Test
  @DisplayName("monthlyFixedCost limitten etkilenmemeli")
  void build_shouldCalculateMonthlyFixedCostBeforeLimit() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(1L, "Kredi", 12, 600, LocalDate.of(2026, 5, 1)),
        row(1L, "Kredi", 12, 600, LocalDate.of(2026, 6, 1)),
        row(2L, "Sigorta", 6, 300, LocalDate.of(2026, 5, 2)),
        row(2L, "Sigorta", 6, 300, LocalDate.of(2026, 6, 2))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 1, TODAY, BigDecimal.valueOf(10000));

    assertEquals(1, response.getItems().size());
    assertEquals(0, BigDecimal.valueOf(900).compareTo(response.getMonthlyFixedCost()));
    assertEquals(9.0, response.getFixedCostShareOfIncome());
  }

  @Test
  @DisplayName("Gelir 0 ise sabit gider payı 0 dönmeli")
  void build_shouldReturnZeroShare_whenIncomeIsZero() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(1L, "Kredi", 12, 600, LocalDate.of(2026, 6, 1))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 10, TODAY, BigDecimal.ZERO);

    assertEquals(0.0, response.getFixedCostShareOfIncome());
  }

  @Test
  @DisplayName("Taksitli grupta bir sonraki beklenen tarih ödenmemiş taksitten gelmeli")
  void build_shouldResolveNextExpectedDateFromUnpaidInstallment() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(1L, "Kredi", 12, 500, LocalDate.of(2026, 6, 1))
    );
    Map<Long, LocalDate> nextDueDates = Map.of(1L, LocalDate.of(2026, 7, 1));

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), nextDueDates, MIN_OCCURRENCE, 10, TODAY, BigDecimal.valueOf(10000));

    RecurringItemDto item = response.getItems().get(0);
    assertEquals(LocalDate.of(2026, 7, 1), item.getNextExpectedDate());
    assertTrue(item.isActive());
  }

  @Test
  @DisplayName("İsim grubunda bir sonraki tarih ortalama periyotla tahmin edilmeli")
  void build_shouldEstimateNextExpectedDateForRepeatedGroup() {
    List<RecurringInstallmentProjection> rows = List.of(
        row(2L, "Netflix", 1, 100, LocalDate.of(2026, 4, 5)),
        row(3L, "Netflix", 1, 100, LocalDate.of(2026, 5, 5)),
        row(4L, "Netflix", 1, 100, LocalDate.of(2026, 6, 5))
    );

    ReportRecurringResponseDto response = factory.build(
        rows, windowMonths, Map.of(), Map.of(), MIN_OCCURRENCE, 10, TODAY, BigDecimal.valueOf(10000));

    RecurringItemDto item = response.getItems().get(0);
    // (2026-04-05 -> 2026-06-05) = 61 gün / 2 tekrar aralığı = 30 gün
    assertEquals(LocalDate.of(2026, 7, 5), item.getNextExpectedDate());
  }

  private RecurringInstallmentProjection row(Long transactionId, String name, int totalInstallment, int amount, LocalDate debtDate) {
    return new RecurringInstallmentProjection(transactionId, name, totalInstallment, BigDecimal.valueOf(amount), debtDate);
  }

  private RecurringItemDto itemOf(ReportRecurringResponseDto response, String groupKey) {
    return response.getItems().stream()
        .filter(item -> item.getGroupKey().equals(groupKey))
        .findFirst()
        .orElseThrow();
  }
}
