package com.hakandincturk.dtos.transaction.request;

import java.math.BigDecimal;

import com.hakandincturk.core.enums.TransactionTypes;

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
public class CreateTransactionRequestDto {
  
  private Long contactId;
  
  @NotNull(message = "Hesap boş olamaz")
  private Long accountId;

  @NotNull(message = "Tip boş olamaz")
  private TransactionTypes type;

  @NotNull(message = "Toplam tutar boş olamaz")
  private BigDecimal totalAmount;

  @Min(value = 1, message = "Taksit sayısı en az 0 olmalıdır")
  private Integer totalInstallment;

  private String description;

}
