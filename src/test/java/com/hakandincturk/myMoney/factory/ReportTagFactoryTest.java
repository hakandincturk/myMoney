package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.dtos.report.response.ReportTagBreakdownResponseDto;
import com.hakandincturk.dtos.report.response.TagBreakdownItemDto;
import com.hakandincturk.dtos.report.response.TopTagDto;
import com.hakandincturk.factories.ReportTagFactory;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;

class ReportTagFactoryTest {

  private final ReportTagFactory factory = new ReportTagFactory();

  @Test
  @DisplayName("DISTRIBUTED modda etiket toplamları ayın toplamına eşit olmalı")
  void buildBreakdown_shouldKeepTotalConsistent_inDistributedMode() {
    // 100.00 TL'lik taksit 3 etikete bölünüyor; kuruş artığı kaybolmamalı
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), 1L, "Market"),
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), 2L, "Ev"),
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), 3L, "Fatura")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(rows, List.of(), DashboardTagSummarySumMode.DISTRIBUTED, 10);

    BigDecimal itemSum = response.getItems().stream()
        .map(TagBreakdownItemDto::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    assertEquals(3, response.getItems().size());
    assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getTotal()));
    assertEquals(0, response.getTotal().compareTo(itemSum));
  }

  @Test
  @DisplayName("DOUBLE_COUNT modda her etikete tam tutar yazılmalı")
  void buildBreakdown_shouldWriteFullAmount_inDoubleCountMode() {
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), 1L, "Market"),
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), 2L, "Ev")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(rows, List.of(), DashboardTagSummarySumMode.DOUBLE_COUNT, 10);

    assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getItems().get(0).getAmount()));
    assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getItems().get(1).getAmount()));
    assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getTotal()));
  }

  @Test
  @DisplayName("Etiketsiz taksitler UNTAGGED grubunda toplanmalı")
  void buildBreakdown_shouldGroupUntaggedInstallments() {
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(320), null, null),
        new InstallmentTagAmountProjection(2L, 11L, BigDecimal.valueOf(80), 1L, "Market")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(rows, List.of(), DashboardTagSummarySumMode.DISTRIBUTED, 10);

    TagBreakdownItemDto untagged = response.getItems().stream()
        .filter(item -> item.getTagId() == null)
        .findFirst()
        .orElseThrow();

    assertEquals("UNTAGGED", untagged.getName());
    assertEquals(0, BigDecimal.valueOf(320).compareTo(untagged.getAmount()));
    assertEquals(0, BigDecimal.valueOf(320).compareTo(response.getUntaggedAmount()));
    assertEquals(0, BigDecimal.valueOf(400).compareTo(response.getTotal()));
  }

  @Test
  @DisplayName("Önceki ayda olmayan etiketin changeRate değeri null olmalı")
  void buildBreakdown_shouldReturnNullChangeRate_whenPreviousIsZero() {
    List<InstallmentTagAmountProjection> currentRows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(500), 1L, "Market")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(currentRows, List.of(), DashboardTagSummarySumMode.DISTRIBUTED, 10);

    TagBreakdownItemDto item = response.getItems().get(0);
    assertNull(item.getChangeRate());
    assertEquals(0, BigDecimal.ZERO.compareTo(item.getPreviousAmount()));
  }

  @Test
  @DisplayName("Önceki ay tutarı ve değişim oranı doldurulmalı")
  void buildBreakdown_shouldCompareWithPreviousMonth() {
    List<InstallmentTagAmountProjection> currentRows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(8400), 3L, "Market")
    );
    List<InstallmentTagAmountProjection> previousRows = List.of(
        new InstallmentTagAmountProjection(2L, 11L, BigDecimal.valueOf(6270), 3L, "Market")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(currentRows, previousRows, DashboardTagSummarySumMode.DISTRIBUTED, 10);

    TagBreakdownItemDto item = response.getItems().get(0);
    assertEquals(0, BigDecimal.valueOf(6270).compareTo(item.getPreviousAmount()));
    assertEquals(33.97, item.getChangeRate());
    assertEquals(0, BigDecimal.valueOf(6270).compareTo(response.getPreviousTotal()));
  }

  @Test
  @DisplayName("Limit sadece listeyi kısaltmalı, toplamı etkilememeli")
  void buildBreakdown_shouldNotLetLimitAffectTotal() {
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(300), 1L, "Market"),
        new InstallmentTagAmountProjection(2L, 11L, BigDecimal.valueOf(200), 2L, "Ev"),
        new InstallmentTagAmountProjection(3L, 12L, BigDecimal.valueOf(100), 3L, "Fatura")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(rows, List.of(), DashboardTagSummarySumMode.DISTRIBUTED, 1);

    assertEquals(1, response.getItems().size());
    assertEquals("Market", response.getItems().get(0).getName());
    assertEquals(0, BigDecimal.valueOf(600).compareTo(response.getTotal()));
  }

  @Test
  @DisplayName("Aynı işlemin farklı taksitleri etiket bazında tek işlem sayılmalı")
  void buildBreakdown_shouldCountDistinctTransactions() {
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), 1L, "Market"),
        new InstallmentTagAmountProjection(2L, 10L, BigDecimal.valueOf(100), 1L, "Market")
    );

    ReportTagBreakdownResponseDto response = factory.buildBreakdown(rows, List.of(), DashboardTagSummarySumMode.DISTRIBUTED, 10);

    assertEquals(1, response.getItems().get(0).getTransactionCount());
    assertEquals(0, BigDecimal.valueOf(200).compareTo(response.getTotal()));
  }

  @Test
  @DisplayName("En çok harcanan etiket bulunmalı")
  void buildTopTag_shouldReturnLargestTag() {
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(100), null, null),
        new InstallmentTagAmountProjection(2L, 11L, BigDecimal.valueOf(900), 5L, "Market")
    );

    TopTagDto topTag = factory.buildTopTag(rows);

    assertNotNull(topTag);
    assertEquals(5L, topTag.getTagId());
    assertEquals("Market", topTag.getName());
    assertEquals(90.0, topTag.getShare());
  }

  @Test
  @DisplayName("Etiketsiz grup en büyükse topTag olarak dönmeli")
  void buildTopTag_shouldReturnUntaggedGroup_whenItIsLargest() {
    List<InstallmentTagAmountProjection> rows = List.of(
        new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(900), null, null),
        new InstallmentTagAmountProjection(2L, 11L, BigDecimal.valueOf(100), 5L, "Market")
    );

    TopTagDto topTag = factory.buildTopTag(rows);

    assertNotNull(topTag);
    assertNull(topTag.getTagId());
    assertEquals("UNTAGGED", topTag.getName());
    assertEquals(90.0, topTag.getShare());
  }

  @Test
  @DisplayName("Hiç harcama yoksa en çok harcanan etiket null dönmeli")
  void buildTopTag_shouldReturnNull_whenNoExpenseExists() {
    assertNull(factory.buildTopTag(List.of()));
  }
}
