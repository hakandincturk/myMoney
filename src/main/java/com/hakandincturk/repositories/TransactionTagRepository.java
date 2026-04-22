package com.hakandincturk.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.TransactionTag;

@Repository
public interface TransactionTagRepository extends JpaRepository<TransactionTag, Long>, JpaSpecificationExecutor<TransactionTag>{

  Page<TransactionTag> findAllByTagIdAndTransaction_UserIdAndIsRemovedFalse(Long tagId, Long userId, Pageable page);

  List<TransactionTag> findAllByTagIdAndIsRemovedFalse(Long tagId);

}
