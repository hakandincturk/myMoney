package com.hakandincturk.dtos.transaction.request;

import java.math.BigDecimal;
import java.util.List;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.SortablePageRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTransactionsFilterRequestDto extends SortablePageRequest {
  private String transactionName;
  private List<Long> accountIds;
  private List<TransactionTypes> types;
  private List<TransactionStatuses> statuses;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private Integer minInstallmentCount;
  private Integer maxInstallmentCount;
}
