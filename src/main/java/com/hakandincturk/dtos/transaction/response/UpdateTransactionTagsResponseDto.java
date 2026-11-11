package com.hakandincturk.dtos.transaction.response;

import java.util.List;

import com.hakandincturk.dtos.tag.response.TransactionTagInfoDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The transaction's tags after the update, so the client can refresh the detail view without a refetch.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionTagsResponseDto {
  private Long transactionId;
  private List<TransactionTagInfoDto> tags;
}
