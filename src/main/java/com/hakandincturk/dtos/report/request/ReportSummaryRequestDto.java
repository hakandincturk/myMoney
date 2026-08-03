package com.hakandincturk.dtos.report.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryRequestDto {

  @NotNull(message = "Yıl bilgisi boş olamaz")
  @Min(value = 2000, message = "Yıl bilgisi 2000'den küçük olamaz")
  @Max(value = 2100, message = "Yıl bilgisi 2100'den büyük olamaz")
  private Integer year;

  @NotNull(message = "Ay bilgisi boş olamaz")
  @Min(value = 1, message = "Ay bilgisi 1'den küçük olamaz")
  @Max(value = 12, message = "Ay bilgisi 12'den büyük olamaz")
  private Integer month;

}
