package com.hakandincturk.dtos.installment.response;

import com.hakandincturk.core.enums.TransactionTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailDto {

  private Long id;
  private String name;
  private TransactionTypes type;
  
}
