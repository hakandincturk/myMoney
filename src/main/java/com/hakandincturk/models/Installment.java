package com.hakandincturk.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
  @JsonIgnore
  @JoinColumn(name = "transaction_id")
  private Transaction transaction;

  @Column(name = "installmentNumber")
  private int installmentNumber;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "isPaid", nullable =  false)
  private boolean isPaid = false;

  @Column(name = "paidDate")
  private LocalDate paidDate;

  @Column(name = "description", nullable = true)
  private String descripton;

  @Column(name = "debtDate")
  private LocalDate debtDate;
  
}
