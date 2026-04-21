package com.hakandincturk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.TransactionCategory;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long>, JpaSpecificationExecutor<TransactionCategory>{

  Page<TransactionCategory> findAllByCategoryIdAndTransaction_UserIdAndIsRemovedFalse(Long categoryId, Long userId, Pageable page);

}