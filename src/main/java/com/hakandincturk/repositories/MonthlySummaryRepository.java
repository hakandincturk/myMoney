package com.hakandincturk.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.models.MonthlySummary;

@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {
  List<MonthlySummary> findByUser_IdAndYearAndMonthAndIsRemovedFalse(Long userId, Integer year, Integer month);
  Optional<MonthlySummary> findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(Long userId, Integer year, Integer month, MonthlySummeryTypes type);
  List<MonthlySummary> findByUser_IdAndSummaryDateBetweenAndTypeAndIsRemovedFalse(Long userId, LocalDate startDate, LocalDate endDate, MonthlySummeryTypes type);

}
