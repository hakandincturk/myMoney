package com.hakandincturk.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  Page<Account> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageData);
  Optional<Account> findByIdAndUserIdAndIsRemovedFalse(Long accountId, Long userId);

  @Query(
          "SELECT COALESCE(SUM(a.balance), 0) " +
          "FROM Account a " +
          "WHERE a.user.id = :userId " +
          "AND a.type IN :types " +
          "AND a.isRemoved = false"
        )
    BigDecimal sumBalanceByUserIdAndTypes(@Param("userId") Long userId, @Param("types") List<AccountTypes> types);
}
