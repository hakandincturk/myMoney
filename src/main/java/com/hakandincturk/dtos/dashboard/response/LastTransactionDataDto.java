package com.hakandincturk.dtos.dashboard.response;

import java.math.BigDecimal;
import java.util.Date;

import com.hakandincturk.core.enums.TransactionTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LastTransactionDataDto {
  private String name;
  private String description;
  private TransactionTypes type;
  private BigDecimal totalAmount;
  private Date createdAt;
}
