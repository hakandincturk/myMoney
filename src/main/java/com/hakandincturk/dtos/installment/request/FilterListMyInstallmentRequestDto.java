package com.hakandincturk.dtos.installment.request;

import java.math.BigDecimal;
import java.util.List;

import com.hakandincturk.dtos.SortablePageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterListMyInstallmentRequestDto extends SortablePageRequest {
  private int month;
  private int year;
  private String transactionName;
  private String description;
  private BigDecimal minTotalAmount;
  private BigDecimal maxTotalAmount;
  private List<Boolean> isPaid;
}
