package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Etiket bazlı kırılım. total ve untaggedAmount limitten etkilenmez, ayın tamamını yansıtır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportTagBreakdownResponseDto {

  private List<TagBreakdownItemDto> items;
  private BigDecimal total;
  private BigDecimal previousTotal;
  private BigDecimal untaggedAmount;

}
