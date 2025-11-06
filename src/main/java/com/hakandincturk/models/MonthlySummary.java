package com.hakandincturk.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hakandincturk.core.enums.MonthlySummeryTypes;

import jakarta.persistence.Column;
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

  @Column(name = "total_income")
  private BigDecimal totalIncome;

  @Column(name = "total_expense")
  private BigDecimal totalExpense;

  @Column(name = "total_waiting_income")
  private BigDecimal totalWaitingIncome;

  @Column(name = "total_waiting_expense")
  private BigDecimal totalWaitingExpense;

  @Enumerated(EnumType.STRING)
  private MonthlySummeryTypes type;

  @Column(name = "summary_date")
  private LocalDate summaryDate;
}
