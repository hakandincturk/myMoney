package com.hakandincturk.dtos.dashboard.response;

import com.hakandincturk.core.enums.TransactionTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomingInstallmentTransactionDetailDataDto {
  private String name;
  private String description;
  private TransactionTypes type;
}
