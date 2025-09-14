package com.hakandincturk.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntitiy {

  @Column(name = "name")
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<TransactionCategory> categoryTransactions;
  
}
