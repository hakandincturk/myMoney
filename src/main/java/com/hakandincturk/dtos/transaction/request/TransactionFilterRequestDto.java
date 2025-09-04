package com.hakandincturk.dtos.transaction.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.SortablePageRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionFilterRequestDto extends SortablePageRequest {
    private String name;
    private List<Long> accountIds;
    private List<Long> contactIds;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minInstallmentCount;
    private Integer maxInstallmentCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TransactionTypes> types;
}
