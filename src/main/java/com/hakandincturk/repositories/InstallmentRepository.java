package com.hakandincturk.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Installment;
import com.hakandincturk.repositories.projections.DailyAmountProjection;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyEntityCountProjection;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.repositories.projections.RecurringInstallmentProjection;
import com.hakandincturk.repositories.projections.TopInstallmentProjection;
import com.hakandincturk.repositories.projections.TransactionNextDueProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long>, JpaSpecificationExecutor<Installment> {
  Optional<Installment> findByIdAndTransactionUserIdAndIsRemovedFalse(Long id, Long userId);
  List<Installment> findByIdInAndTransactionUserIdAndIsRemovedFalse(List<Long> ids, Long userId);
  List<Installment> findByTransaction_UserIdOrderByDebtDate(Long userId);
  List<Installment> findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(Long userId, LocalDate starDate, LocalDate endDate);
  List<Installment> findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(Long userId, List<TransactionTypes> type, LocalDate starDate, LocalDate endDate);
  List<Installment> findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(Long userId, LocalDate starDate, LocalDate endDate);
  // start: 2025-08-01, end: 2025-08-31
  // Page<Installment> findByTransactionUserIdAndDebtDateBetweenAndIsRemovedFalse(Long userId, LocalDate start, LocalDate end, Pageable pageData);

  List<Installment> findTop10ByTransaction_UserIdAndDebtDateBetweenAndIsPaidFalseAndIsRemovedFalseOrderByDebtDate(Long userId, LocalDate startDate, LocalDate endDate);

  /**
   * Rapor: aralıktaki taksit tutarlarını ay + hareket tipi + ödeme durumu kırılımında toplar.
   * Gelir/gider ayrımı bilerek SQL'e gömülmez; sınıflandırma tek kural kaynağı olan
   * TransactionClassifier üzerinden servis katmanında yapılır.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection(
      year(i.debtDate),
      month(i.debtDate),
      t.type,
      i.isPaid,
      sum(i.amount)
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND i.debtDate BETWEEN :startDate AND :endDate
    GROUP BY year(i.debtDate), month(i.debtDate), t.type, i.isPaid
      """)
  List<MonthlyTypeAmountProjection> sumMonthlyAmountsByTypeAndPaidState(
    @Param("userId") Long userId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: aralıktaki her ay için farklı işlem ve taksit adetleri.
   * Aynı işlemin birden fazla taksiti aynı aya düşebildiği için işlem sayısı DISTINCT alınır.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.MonthlyEntityCountProjection(
      year(i.debtDate),
      month(i.debtDate),
      count(DISTINCT t.id),
      count(i.id)
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND i.debtDate BETWEEN :startDate AND :endDate
    GROUP BY year(i.debtDate), month(i.debtDate)
      """)
  List<MonthlyEntityCountProjection> countMonthlyEntities(
    @Param("userId") Long userId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: ay içindeki gün bazlı tutar toplamları; ayın en yoğun günü bu veriden bulunur.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.DailyAmountProjection(
      i.debtDate,
      sum(i.amount)
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND t.type IN :types
      AND i.debtDate BETWEEN :startDate AND :endDate
    GROUP BY i.debtDate
      """)
  List<DailyAmountProjection> sumDailyAmounts(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: kullanıcının halen ödenmemiş toplam borcu (tarihten bağımsız).
   * Timeline'daki kümülatif kalan borç serisi bu değerin üzerine kurulur.
  */
  @Query("""
    SELECT coalesce(sum(i.amount), 0)
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND i.isPaid = false
      AND t.type IN :types
      """)
  BigDecimal sumUnpaidAmount(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: ödeme tarihine göre ay bazlı ödenen tutarlar.
   * Geçmiş bir ayın sonundaki kalan borcu geri hesaplamak için kullanılır.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.MonthlyAmountProjection(
      year(i.paidDate),
      month(i.paidDate),
      sum(i.amount)
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND i.isPaid = true
      AND i.paidDate >= :fromDate
      AND t.type IN :types
    GROUP BY year(i.paidDate), month(i.paidDate)
      """)
  List<MonthlyAmountProjection> sumPaidAmountsByPaidMonth(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("fromDate") LocalDate fromDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: vade tarihine göre ay bazlı ödenmemiş taksit tutarları.
   * "Plana uyulursa borç nasıl erir" projeksiyonunun girdisidir.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.MonthlyAmountProjection(
      year(i.debtDate),
      month(i.debtDate),
      sum(i.amount)
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND i.isPaid = false
      AND i.debtDate >= :fromDate
      AND t.type IN :types
    GROUP BY year(i.debtDate), month(i.debtDate)
      """)
  List<MonthlyAmountProjection> sumUnpaidAmountsByDebtMonth(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("fromDate") LocalDate fromDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: aralıktaki taksitleri etiketleriyle birlikte düz satırlar halinde getirir.
   * Etiketi olmayan taksitler LEFT JOIN sayesinde tagId null olarak döner (UNTAGGED grubu).
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.InstallmentTagAmountProjection(
      i.id,
      t.id,
      i.amount,
      tag.id,
      tag.name
    )
    FROM Installment i
    JOIN i.transaction t
    LEFT JOIN t.transactionTags tt ON tt.isRemoved = false
    LEFT JOIN tt.tag tag ON tag.isRemoved = false
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND t.type IN :types
      AND i.debtDate BETWEEN :startDate AND :endDate
      """)
  List<InstallmentTagAmountProjection> findInstallmentTagAmounts(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: ayın en büyük kalemleri. Sıralama işlemin toplam tutarına göre değil,
   * taksitin o aya düşen tutarına göre yapılır.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.TopInstallmentProjection(
      i.id,
      t.id,
      t.name,
      t.description,
      i.description,
      i.amount,
      i.debtDate,
      a.name,
      c.fullName,
      i.installmentNumber,
      t.totalInstallment,
      i.isPaid
    )
    FROM Installment i
    JOIN i.transaction t
    JOIN t.account a
    LEFT JOIN t.contact c
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND t.type IN :types
      AND i.debtDate BETWEEN :startDate AND :endDate
    ORDER BY i.amount DESC, i.id ASC
      """)
  List<TopInstallmentProjection> findTopInstallments(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus,
    Pageable pageable
  );

  /**
   * Rapor: tekrar eden harcama penceresindeki ham taksit satırları.
   * İsim normalizasyonu ve gruplama servis katmanında yapılır.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.RecurringInstallmentProjection(
      t.id,
      t.name,
      t.totalInstallment,
      i.amount,
      i.debtDate
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND t.type IN :types
      AND i.debtDate BETWEEN :startDate AND :endDate
    ORDER BY i.debtDate ASC
      """)
  List<RecurringInstallmentProjection> findRecurringCandidates(
    @Param("userId") Long userId,
    @Param("types") List<TransactionTypes> types,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );

  /**
   * Rapor: verilen işlemlerin ödenmemiş ilk taksit tarihleri.
  */
  @Query("""
    SELECT new com.hakandincturk.repositories.projections.TransactionNextDueProjection(
      t.id,
      min(i.debtDate)
    )
    FROM Installment i
    JOIN i.transaction t
    WHERE t.user.id = :userId
      AND t.id IN :transactionIds
      AND i.isRemoved = false
      AND t.isRemoved = false
      AND i.status <> :skippedStatus
      AND i.isPaid = false
    GROUP BY t.id
      """)
  List<TransactionNextDueProjection> findNextDueDates(
    @Param("userId") Long userId,
    @Param("transactionIds") List<Long> transactionIds,
    @Param("skippedStatus") InstallmentStatuses skippedStatus
  );
}
