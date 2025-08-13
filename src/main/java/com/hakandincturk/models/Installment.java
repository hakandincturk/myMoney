package com.hakandincturk.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Table(name = "Installment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Installment extends BaseEntitiy {

  @ManyToOne(fetch = FetchType.LAZY)
  private Transaction transaction;

  @Column(name = "installmentNumber")
  private int installmentNumber;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "isPaid", nullable =  false)
  private boolean isPaid = false;

  @Column(name = "paidDate")
  private LocalDateTime paidDate;

  
}
