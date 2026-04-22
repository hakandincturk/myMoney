package com.hakandincturk.dtos.transaction.response;

import java.math.BigDecimal;
import java.util.List;

import com.hakandincturk.dtos.tag.response.TransactionTagInfoDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListMyTransactionsResponseDto {

  private Long id;
  private String name;
  private String contactName;
  private String accountName;
  private String type;
  private String status;
  private BigDecimal totalAmount;
  private BigDecimal paidAmount;
  private int totalInstallment;
  private List<TransactionTagInfoDto> tags;

}
