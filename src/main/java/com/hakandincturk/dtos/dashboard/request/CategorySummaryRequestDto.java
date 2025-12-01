package com.hakandincturk.dtos.dashboard.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static java.time.temporal.TemporalAdjusters.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryRequestDto {

  @NotNull(message = "Başlangıç tarihi boş olamaz")
  private LocalDate startDate = LocalDate.now().with(firstDayOfMonth());

  @NotNull(message = "Bitiş tarihi boş olamaz")
  private LocalDate endDate = LocalDate.now().with(lastDayOfMonth());
}
