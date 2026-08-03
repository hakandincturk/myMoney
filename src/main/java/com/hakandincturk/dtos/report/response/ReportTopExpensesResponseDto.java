package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ayın en büyük kalemleri. periodTotal, kalemin toplam içindeki payını hesaplamak için döner.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportTopExpensesResponseDto {

  private List<TopExpenseItemDto> items;
  private BigDecimal periodTotal;

}
