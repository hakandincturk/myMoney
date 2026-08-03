package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ayın en çok harcanan etiketi; share, ayın toplam giderine oranıdır (%).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopTagDto {

  /** Etiketsiz grup kazandığında null döner; o durumda name "UNTAGGED" olur. */
  private Long tagId;
  private String name;
  private BigDecimal amount;
  private Double share;

}
