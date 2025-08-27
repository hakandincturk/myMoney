package com.hakandincturk.dtos.transaction.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hakandincturk.core.enums.TransactionTypes;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

  @NotNull(message = "Borç türü boş olamaz")
  private TransactionTypes type;

  @NotNull(message = "Toplam tutar boş olamaz")
  private BigDecimal totalAmount;

  @Min(value = 1, message = "Taksit sayısı en az 0 olmalıdır")
  private Integer totalInstallment;

  @NotBlank(message = "Gelir/Gider ismi boş olamaz")
  @Size(min = 2, message = "Gelir/Gider ismi en az 2 karakterden oluşmalıdır")
  private String name;
  
  private String description;

  @NotNull(message = "Tarih boş olamaz")
  private LocalDate debtDate;

  private boolean equalSharingBetweenInstallments = true;

}
