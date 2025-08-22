package com.hakandincturk.dtos.installment.request;

import java.time.LocalDate;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoAutoStart
@AllArgsConstructor
public class PayInstallmentRequestDto {

  private LocalDate paidDate;
  
}
