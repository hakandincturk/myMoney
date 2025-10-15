package com.hakandincturk.dtos.installment.request;

import java.time.LocalDate;
import java.util.List;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoAutoStart
@AllArgsConstructor
public class PayInstallmentRequestDto {

  private List<Long> ids;
  private LocalDate paidDate;
  
}
