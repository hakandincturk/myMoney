package com.hakandincturk.dtos.monthlySummery.request;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackFillMonthlySummeriesRequest {

  @Size(min = 0)
  private List<Long> userIds;
}
