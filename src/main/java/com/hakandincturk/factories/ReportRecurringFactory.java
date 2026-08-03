package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.hakandincturk.core.enums.RecurringKinds;
import com.hakandincturk.dtos.report.response.RecurringItemDto;
import com.hakandincturk.dtos.report.response.RecurringMonthAmountDto;
import com.hakandincturk.dtos.report.response.ReportPeriodRefDto;
import com.hakandincturk.dtos.report.response.ReportRecurringResponseDto;
import com.hakandincturk.dtos.report.response.ReportTagRefDto;
import com.hakandincturk.repositories.projections.RecurringInstallmentProjection;
import com.hakandincturk.utils.ReportMathUtils;
import com.hakandincturk.utils.ReportPeriodUtils;

/**
 * Pencere içindeki taksit satırlarından tekrar eden harcama gruplarını çıkarır.
 * Çok taksitli işlemler (INSTALLMENT) tanım gereği tekrar eder ve isim eşleşmesiyle
 * kurulan gruplara (REPEATED) tekrar dahil edilmez; böylece çift sayım oluşmaz.
 */
@Component
public class ReportRecurringFactory {

  private static final String INSTALLMENT_GROUP_PREFIX = "TX:";
  private static final String REPEATED_GROUP_PREFIX = "NAME:";

  // Türkçe karakterlerin doğru küçültülmesi için (İ -> i, I -> ı)
  private static final Locale TURKISH_LOCALE = Locale.of("tr");

  private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");

  // "Son 1 ay içinde geçiş var mı" kontrolünün penceresi
  private static final int ACTIVITY_MONTH_WINDOW = 1;

  /**
   * Tekrar eden harcama listesini üretir.
   * @param rows Pencere içindeki taksit satırları (tarihe göre artan sıralı beklenir)
   * @param windowMonths Pencerenin tüm ayları, kronolojik sırada
   * @param tagsByTransaction İşlem bazlı etiketler
   * @param nextDueByTransaction İşlem bazlı ödenmemiş ilk taksit tarihi
   * @param minOccurrence Bir grubun tekrar sayılması için gereken en az tekrar
   * @param limit Kaç grup döneceği; monthlyFixedCost bu limitten etkilenmez
   * @param today Aktiflik hesabı için referans tarih
   * @param currentMonthIncome İçinde bulunulan ayın geliri
  */
  public ReportRecurringResponseDto build(
    List<RecurringInstallmentProjection> rows,
    List<YearMonth> windowMonths,
    Map<Long, List<ReportTagRefDto>> tagsByTransaction,
    Map<Long, LocalDate> nextDueByTransaction,
    int minOccurrence,
    int limit,
    LocalDate today,
    BigDecimal currentMonthIncome
  ){
    Map<String, RecurringGroup> groups = this.groupRows(rows, minOccurrence);

    List<RecurringItemDto> allItems = groups.values().stream()
      .filter(group -> this.isRecurring(group, minOccurrence))
      .map(group -> this.toItem(group, windowMonths, tagsByTransaction, nextDueByTransaction, today))
      .sorted(Comparator.comparing(RecurringItemDto::getAverageAmount).reversed())
      .toList();

    BigDecimal monthlyFixedCost = allItems.stream()
      .map(RecurringItemDto::getAverageAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    ReportRecurringResponseDto response = new ReportRecurringResponseDto();
    response.setItems(allItems.stream().limit(limit).toList());
    response.setMonthlyFixedCost(ReportMathUtils.money(monthlyFixedCost));
    response.setFixedCostShareOfIncome(ReportMathUtils.share(monthlyFixedCost, currentMonthIncome));
    // Uç year/month almadığı için pencerenin hangi aralık olduğu her zaman response ile bildirilir
    response.setWindowStart(this.toPeriodRef(windowMonths.getFirst()));
    response.setWindowEnd(this.toPeriodRef(windowMonths.getLast()));

    return response;
  }

  private ReportPeriodRefDto toPeriodRef(YearMonth period){
    return new ReportPeriodRefDto(period.getYear(), period.getMonthValue(), ReportPeriodUtils.labelOf(period));
  }

  /**
   * Satırları taksitli işlem veya normalize edilmiş isim anahtarına göre gruplar.
  */
  private Map<String, RecurringGroup> groupRows(List<RecurringInstallmentProjection> rows, int minOccurrence){
    Map<String, RecurringGroup> groups = new LinkedHashMap<>();

    for (RecurringInstallmentProjection row : rows) {
      boolean isInstallmentBased = row.totalInstallment() != null && row.totalInstallment() >= minOccurrence;

      String groupKey = isInstallmentBased
        ? INSTALLMENT_GROUP_PREFIX + row.transactionId()
        : REPEATED_GROUP_PREFIX + this.normalizeName(row.transactionName());

      RecurringKinds kind = isInstallmentBased ? RecurringKinds.INSTALLMENT : RecurringKinds.REPEATED;

      groups.computeIfAbsent(groupKey, key -> new RecurringGroup(key, kind)).add(row);
    }

    return groups;
  }

  /**
   * Taksitli işlemler tanım gereği tekrar eder; isim eşleşmeli gruplar ise
   * pencere içinde en az minOccurrence farklı ayda geçmelidir.
  */
  private boolean isRecurring(RecurringGroup group, int minOccurrence){
    if(group.getKind() == RecurringKinds.INSTALLMENT){
      return true;
    }

    return group.getMonths().size() >= minOccurrence;
  }

  private RecurringItemDto toItem(
    RecurringGroup group,
    List<YearMonth> windowMonths,
    Map<Long, List<ReportTagRefDto>> tagsByTransaction,
    Map<Long, LocalDate> nextDueByTransaction,
    LocalDate today
  ){
    int monthsSpan = group.getMonths().size();
    BigDecimal totalAmount = ReportMathUtils.money(group.getTotalAmount());
    // Aylık ortalama: sabit gider hesabına girdiği için tekrar sayısına değil, ay sayısına bölünür
    BigDecimal averageAmount = ReportMathUtils.divide(totalAmount, monthsSpan);
    LocalDate nextExpectedDate = this.resolveNextExpectedDate(group, nextDueByTransaction);

    RecurringItemDto item = new RecurringItemDto();
    item.setGroupKey(group.getGroupKey());
    item.setName(group.getName());
    item.setKind(group.getKind());
    item.setOccurrenceCount(group.getOccurrenceCount());
    item.setMonthsSpan(monthsSpan);
    item.setAverageAmount(averageAmount);
    item.setTotalAmount(totalAmount);
    item.setLastAmount(ReportMathUtils.money(group.getLastAmount()));
    item.setLastDate(group.getLastDate());
    item.setNextExpectedDate(nextExpectedDate);
    item.setAmountByMonth(this.buildAmountByMonth(group, windowMonths));
    item.setTags(this.collectTags(group, tagsByTransaction));
    item.setActive(this.isActive(group, nextExpectedDate, today));

    return item;
  }

  /**
   * Taksitli gruplarda bir sonraki ödenmemiş taksit tarihi, isim eşleşmeli gruplarda
   * son geçiş tarihine ortalama periyot eklenerek bulunan tahmindir.
  */
  private LocalDate resolveNextExpectedDate(RecurringGroup group, Map<Long, LocalDate> nextDueByTransaction){
    if(group.getKind() == RecurringKinds.INSTALLMENT){
      return group.getTransactionIds().stream()
        .map(nextDueByTransaction::get)
        .filter(Objects::nonNull)
        .min(Comparator.naturalOrder())
        .orElse(null);
    }

    if(group.getOccurrenceCount() < 2){
      return null;
    }

    long averagePeriodInDays = ChronoUnit.DAYS.between(group.getFirstDate(), group.getLastDate()) / (group.getOccurrenceCount() - 1);
    if(averagePeriodInDays <= 0){
      return null;
    }

    return group.getLastDate().plusDays(averagePeriodInDays);
  }

  /**
   * Sparkline serisi delikli olamayacağı için pencerenin her ayı üretilir, boş aylar 0 döner.
  */
  private List<RecurringMonthAmountDto> buildAmountByMonth(RecurringGroup group, List<YearMonth> windowMonths){
    List<RecurringMonthAmountDto> amountByMonth = new ArrayList<>();
    for (YearMonth month : windowMonths) {
      BigDecimal amount = group.getAmountOf(month);
      amountByMonth.add(new RecurringMonthAmountDto(month.getYear(), month.getMonthValue(), ReportMathUtils.money(amount)));
    }

    return amountByMonth;
  }

  private List<ReportTagRefDto> collectTags(RecurringGroup group, Map<Long, List<ReportTagRefDto>> tagsByTransaction){
    Map<Long, ReportTagRefDto> tags = new LinkedHashMap<>();
    for (Long transactionId : group.getTransactionIds()) {
      tagsByTransaction.getOrDefault(transactionId, List.of())
        .forEach(tag -> tags.putIfAbsent(tag.getId(), tag));
    }

    return List.copyOf(tags.values());
  }

  /**
   * Son 1 ay içinde geçiş varsa ya da gelecekte planlı bir taksit varsa grup aktiftir.
  */
  private boolean isActive(RecurringGroup group, LocalDate nextExpectedDate, LocalDate today){
    boolean seenRecently = !group.getLastDate().isBefore(today.minusMonths(ACTIVITY_MONTH_WINDOW));
    boolean hasPlannedOccurrence = group.getLastDate().isAfter(today);
    boolean hasUnpaidInstallmentAhead = group.getKind() == RecurringKinds.INSTALLMENT
      && nextExpectedDate != null
      && !nextExpectedDate.isBefore(today);

    return seenRecently || hasPlannedOccurrence || hasUnpaidInstallmentAhead;
  }

  /**
   * İsim normalizasyonu: kırp, Türkçe kurallarına göre küçült, ardışık boşlukları teke indir.
  */
  private String normalizeName(String name){
    if(name == null){
      return "";
    }

    String normalized = name.trim().toLowerCase(TURKISH_LOCALE);

    return MULTIPLE_WHITESPACE.matcher(normalized).replaceAll(" ");
  }

  /**
   * Tek bir tekrar grubunun biriken durumu.
   */
  private static final class RecurringGroup {

    private final String groupKey;
    private final RecurringKinds kind;
    private final Set<Long> transactionIds = new LinkedHashSet<>();
    private final Map<YearMonth, BigDecimal> amountByMonth = new LinkedHashMap<>();

    private String name;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal lastAmount = BigDecimal.ZERO;
    private int occurrenceCount;
    private LocalDate firstDate;
    private LocalDate lastDate;

    private RecurringGroup(String groupKey, RecurringKinds kind){
      this.groupKey = groupKey;
      this.kind = kind;
    }

    private void add(RecurringInstallmentProjection row){
      BigDecimal amount = ReportMathUtils.zeroIfNull(row.amount());
      YearMonth month = YearMonth.from(row.debtDate());

      this.transactionIds.add(row.transactionId());
      this.amountByMonth.merge(month, amount, BigDecimal::add);
      this.totalAmount = this.totalAmount.add(amount);
      this.occurrenceCount++;

      if(this.firstDate == null || row.debtDate().isBefore(this.firstDate)){
        this.firstDate = row.debtDate();
      }

      // Görünen ad ve son tutar her zaman en güncel geçişten alınır
      if(this.lastDate == null || !row.debtDate().isBefore(this.lastDate)){
        this.lastDate = row.debtDate();
        this.lastAmount = amount;
        this.name = row.transactionName();
      }

      if(this.name == null){
        this.name = row.transactionName();
      }
    }

    private String getGroupKey(){
      return this.groupKey;
    }

    private RecurringKinds getKind(){
      return this.kind;
    }

    private String getName(){
      return this.name;
    }

    private Set<Long> getTransactionIds(){
      return this.transactionIds;
    }

    private Set<YearMonth> getMonths(){
      return this.amountByMonth.keySet();
    }

    private BigDecimal getAmountOf(YearMonth month){
      return this.amountByMonth.getOrDefault(month, BigDecimal.ZERO);
    }

    private BigDecimal getTotalAmount(){
      return this.totalAmount;
    }

    private BigDecimal getLastAmount(){
      return this.lastAmount;
    }

    private int getOccurrenceCount(){
      return this.occurrenceCount;
    }

    private LocalDate getFirstDate(){
      return this.firstDate;
    }

    private LocalDate getLastDate(){
      return this.lastDate;
    }
  }

}
