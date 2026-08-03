package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.dtos.report.response.ReportTagBreakdownResponseDto;
import com.hakandincturk.dtos.report.response.TagBreakdownItemDto;
import com.hakandincturk.dtos.report.response.TopTagDto;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.utils.ReportMathUtils;

/**
 * Taksit - etiket satırlarını etiket bazlı kırılıma çevirir.
 * Toplama modu semantiği dashboard'daki /dashboard/tag-summary ile aynıdır:
 * DISTRIBUTED tutarı etiketlere böler, DOUBLE_COUNT her etikete tam tutarı yazar.
 */
@Component
public class ReportTagFactory {

  // Etiketsiz grubun sabit adı; frontend bu literal'i çeviriyor
  public static final String UNTAGGED_NAME = "UNTAGGED";

  private static final TagKey UNTAGGED_KEY = new TagKey(null, UNTAGGED_NAME);

  /**
   * Etiket kırılımını ve bir önceki ay karşılaştırmasını üretir.
   * @param currentRows Seçilen ayın taksit - etiket satırları
   * @param previousRows Bir önceki ayın taksit - etiket satırları
   * @param sumMode Etiket toplama modu
   * @param limit Kaç etiket döneceği; total ve untaggedAmount bu limitten etkilenmez
  */
  public ReportTagBreakdownResponseDto buildBreakdown(
    List<InstallmentTagAmountProjection> currentRows,
    List<InstallmentTagAmountProjection> previousRows,
    DashboardTagSummarySumMode sumMode,
    int limit
  ){
    TagAggregation current = this.aggregate(currentRows, sumMode);
    TagAggregation previous = this.aggregate(previousRows, sumMode);

    Map<Long, BigDecimal> previousAmounts = new HashMap<>();
    previous.byTag().forEach((key, accumulator) -> previousAmounts.put(key.tagId(), accumulator.getAmount()));

    List<TagBreakdownItemDto> items = current.byTag().entrySet().stream()
      .sorted(
        Comparator.comparing((Map.Entry<TagKey, TagAccumulator> entry) -> entry.getValue().getAmount()).reversed()
          .thenComparing(entry -> entry.getKey().name())
      )
      .limit(limit)
      .map(entry -> this.toBreakdownItem(entry.getKey(), entry.getValue(), previousAmounts, current.total()))
      .toList();

    ReportTagBreakdownResponseDto response = new ReportTagBreakdownResponseDto();
    response.setItems(items);
    response.setTotal(ReportMathUtils.money(current.total()));
    response.setPreviousTotal(ReportMathUtils.money(previous.total()));
    response.setUntaggedAmount(ReportMathUtils.money(current.untaggedAmount()));

    return response;
  }

  /**
   * Ayın en çok harcanan etiketini üretir.
   * Etiketsiz grup da yarışa dahildir; kazanırsa tag-breakdown ile aynı örüntüde
   * tagId = null ve name = "UNTAGGED" döner.
   * Dağıtım her zaman DISTRIBUTED yapılır, böylece paylar ayın gerçek toplamına göre hesaplanır.
   * @param rows Seçilen ayın taksit - etiket satırları
  */
  public TopTagDto buildTopTag(List<InstallmentTagAmountProjection> rows){
    TagAggregation aggregation = this.aggregate(rows, DashboardTagSummarySumMode.DISTRIBUTED);

    return aggregation.byTag().entrySet().stream()
      .max(Comparator.comparing(entry -> entry.getValue().getAmount()))
      .map(entry -> {
        TopTagDto topTag = new TopTagDto();
        topTag.setTagId(entry.getKey().tagId());
        topTag.setName(entry.getKey().name());
        topTag.setAmount(ReportMathUtils.money(entry.getValue().getAmount()));
        topTag.setShare(ReportMathUtils.share(entry.getValue().getAmount(), aggregation.total()));

        return topTag;
      })
      .orElse(null);
  }

  /**
   * Düz satırları önce taksit bazında toplar, ardından etiketlere dağıtır.
   * Toplam her taksit bir kez sayılarak hesaplandığı için toplama modundan etkilenmez.
  */
  private TagAggregation aggregate(List<InstallmentTagAmountProjection> rows, DashboardTagSummarySumMode sumMode){
    Map<Long, InstallmentTags> installments = new LinkedHashMap<>();
    for (InstallmentTagAmountProjection row : rows) {
      InstallmentTags installment = installments.computeIfAbsent(
        row.installmentId(),
        id -> new InstallmentTags(row.transactionId(), ReportMathUtils.money(row.amount()))
      );
      installment.addTag(row.tagId(), row.tagName());
    }

    Map<TagKey, TagAccumulator> byTag = new LinkedHashMap<>();
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal untaggedAmount = BigDecimal.ZERO;

    for (InstallmentTags installment : installments.values()) {
      BigDecimal amount = installment.getAmount();
      total = total.add(amount);

      List<TagKey> tags = installment.getTags();
      if(tags.isEmpty()){
        untaggedAmount = untaggedAmount.add(amount);
        this.accumulate(byTag, UNTAGGED_KEY, amount, installment.getTransactionId());
        continue;
      }

      if(sumMode == DashboardTagSummarySumMode.DOUBLE_COUNT){
        tags.forEach(tag -> this.accumulate(byTag, tag, amount, installment.getTransactionId()));
        continue;
      }

      List<BigDecimal> shares = this.distribute(amount, tags.size());
      for (int index = 0; index < tags.size(); index++) {
        this.accumulate(byTag, tags.get(index), shares.get(index), installment.getTransactionId());
      }
    }

    return new TagAggregation(byTag, total, untaggedAmount);
  }

  /**
   * Tutarı etiketlere böler. Kuruş artığı ilk etikete eklenir; böylece etiket toplamları
   * ayın gerçek toplamına birebir eşit kalır.
  */
  private List<BigDecimal> distribute(BigDecimal amount, int tagCount){
    BigDecimal share = amount.divide(BigDecimal.valueOf(tagCount), ReportMathUtils.MONEY_SCALE, RoundingMode.DOWN);
    BigDecimal remainder = amount.subtract(share.multiply(BigDecimal.valueOf(tagCount)));

    List<BigDecimal> shares = new ArrayList<>();
    shares.add(share.add(remainder));
    for (int index = 1; index < tagCount; index++) {
      shares.add(share);
    }

    return shares;
  }

  private void accumulate(Map<TagKey, TagAccumulator> byTag, TagKey key, BigDecimal amount, Long transactionId){
    byTag.computeIfAbsent(key, tag -> new TagAccumulator()).add(amount, transactionId);
  }

  private TagBreakdownItemDto toBreakdownItem(TagKey key, TagAccumulator accumulator, Map<Long, BigDecimal> previousAmounts, BigDecimal total){
    BigDecimal previousAmount = ReportMathUtils.money(previousAmounts.get(key.tagId()));

    TagBreakdownItemDto item = new TagBreakdownItemDto();
    item.setTagId(key.tagId());
    item.setName(key.name());
    item.setAmount(ReportMathUtils.money(accumulator.getAmount()));
    item.setPercentage(ReportMathUtils.share(accumulator.getAmount(), total));
    item.setPreviousAmount(previousAmount);
    item.setChangeRate(ReportMathUtils.changeRate(accumulator.getAmount(), previousAmount));
    item.setTransactionCount(accumulator.getTransactionCount());

    return item;
  }

  /**
   * Etiket kimliği; etiketsiz grup için tagId null'dır.
   */
  private record TagKey(Long tagId, String name) {}

  /**
   * Etiket bazında biriken tutar ve farklı işlem sayısı.
   */
  private static final class TagAccumulator {

    private BigDecimal amount = BigDecimal.ZERO;
    private final Set<Long> transactionIds = new LinkedHashSet<>();

    private void add(BigDecimal value, Long transactionId){
      this.amount = this.amount.add(value);
      if(transactionId != null){
        this.transactionIds.add(transactionId);
      }
    }

    private BigDecimal getAmount(){
      return this.amount;
    }

    private Integer getTransactionCount(){
      return this.transactionIds.size();
    }
  }

  /**
   * Tek bir taksitin tutarı ve o taksite bağlı farklı etiketler.
   */
  private static final class InstallmentTags {

    private final Long transactionId;
    private final BigDecimal amount;
    private final Set<TagKey> tags = new LinkedHashSet<>();

    private InstallmentTags(Long transactionId, BigDecimal amount){
      this.transactionId = transactionId;
      this.amount = amount;
    }

    private void addTag(Long tagId, String tagName){
      if(tagId == null){
        return;
      }

      this.tags.add(new TagKey(tagId, tagName));
    }

    private Long getTransactionId(){
      return this.transactionId;
    }

    private BigDecimal getAmount(){
      return this.amount;
    }

    private List<TagKey> getTags(){
      return List.copyOf(this.tags);
    }
  }

  /**
   * Bir ayın etiket kırılımı sonucu.
   */
  private record TagAggregation(Map<TagKey, TagAccumulator> byTag, BigDecimal total, BigDecimal untaggedAmount) {}

}
