package com.hakandincturk.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.repositories.projections.TransactionTagProjection;

@Repository
public interface TransactionTagRepository extends JpaRepository<TransactionTag, Long>, JpaSpecificationExecutor<TransactionTag>{

  Page<TransactionTag> findAllByTagIdAndTransaction_UserIdAndIsRemovedFalse(Long tagId, Long userId, Pageable page);

  List<TransactionTag> findAllByTagIdAndIsRemovedFalse(Long tagId);

  /**
   * Rapor: birden fazla işlemin etiketlerini tek sorguda getirir, böylece N+1 oluşmaz.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.TransactionTagProjection(
      t.id,
      tag.id,
      tag.name
    )
    FROM TransactionTag tt
    JOIN tt.transaction t
    JOIN tt.tag tag
    WHERE t.user.id = :userId
      AND t.id IN :transactionIds
      AND tt.isRemoved = false
      AND tag.isRemoved = false
    ORDER BY tag.name ASC
      """)
  List<TransactionTagProjection> findTagsOfTransactions(
    @Param("userId") Long userId,
    @Param("transactionIds") List<Long> transactionIds
  );

}
