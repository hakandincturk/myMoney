package com.hakandincturk.dtos.dashboard.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDataDto {
  private String name;
  private BigDecimal amount;
  private Double percentage;
}
