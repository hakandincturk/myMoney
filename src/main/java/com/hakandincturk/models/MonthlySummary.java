package com.hakandincturk.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hakandincturk.core.enums.MonthlySummeryTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MonthlySummary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummary extends BaseEntitiy {

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "user_id")
  private Users user;

  private Integer year;
  
  private Integer month;

  private BigDecimal totalIncome;

  private BigDecimal totalExpense;

  private BigDecimal totalWaitingIncome;

  private BigDecimal totalWaitingExpense;

  @Enumerated(EnumType.STRING)
  private MonthlySummeryTypes type;
}
