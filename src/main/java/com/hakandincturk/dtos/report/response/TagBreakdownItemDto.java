package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Etiket kırılımındaki tek satır.
 * Etiketsiz işlemler tagId = null ve name = "UNTAGGED" ile döner (sabit literal, frontend çevirir).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagBreakdownItemDto {

  private Long tagId;
  private String name;
  private BigDecimal amount;
  private Double percentage;
  private BigDecimal previousAmount;
  private Double changeRate;
  private Integer transactionCount;

}
