package com.hakandincturk.dtos.dashboard.response;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryResponseDto {
  List<CategorySummaryDataDto> categorySummaryDatas;
}
