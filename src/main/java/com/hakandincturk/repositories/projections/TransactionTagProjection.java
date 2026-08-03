package com.hakandincturk.repositories.projections;

/**
 * İşlemlerin etiketlerini tek sorguda toplu çekmek için kullanılır (N+1 engellenir).
 */
public record TransactionTagProjection(
  Long transactionId,
  Long tagId,
  String tagName
) {}
