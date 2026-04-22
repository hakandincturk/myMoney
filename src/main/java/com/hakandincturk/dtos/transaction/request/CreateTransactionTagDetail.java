package com.hakandincturk.dtos.transaction.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionTagDetail {
  private List<Long> tagIds;
  private List<String> newTags;
}
