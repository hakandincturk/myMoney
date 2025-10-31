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
public class MonthlyTrend {
  List<Integer> incomes;
  List<Integer> expenses;
  List<String> months;
}
