package com.hakandincturk.dtos.installment.request;

import java.math.BigDecimal;

import com.hakandincturk.core.enums.InstallmentStatuses;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInstallmentRequestDto {

  @DecimalMin(value = "0.01", message = "Taksit tutarı 0'dan büyük olmalıdır")
  private BigDecimal amount;

  private InstallmentStatuses status;

}
