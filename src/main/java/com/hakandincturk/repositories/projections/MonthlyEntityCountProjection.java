package com.hakandincturk.repositories.projections;

/**
 * Bir aydaki farklı işlem ve taksit adetleri.
 * Aynı işlemin birden fazla taksiti aynı aya düşebildiği için işlem sayısı DISTINCT alınır.
 */
public record MonthlyEntityCountProjection(
  Integer year,
  Integer month,
  Long transactionCount,
  Long installmentCount
) {}
