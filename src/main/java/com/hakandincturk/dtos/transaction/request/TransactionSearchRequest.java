package com.hakandincturk.dtos.transaction.request;

import com.hakandincturk.dtos.SortablePageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionSearchRequest extends SortablePageRequest {
    // Transaction'a özel arama kriterleri buraya eklenebilir
    // Örneğin:
    // private String accountId;
    // private BigDecimal minAmount;
    // private BigDecimal maxAmount;
    // private LocalDate startDate;
    // private LocalDate endDate;
}
