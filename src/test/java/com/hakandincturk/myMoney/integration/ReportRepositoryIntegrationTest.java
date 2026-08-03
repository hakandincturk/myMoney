package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;
import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.repositories.projections.DailyAmountProjection;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyEntityCountProjection;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.repositories.projections.RecurringInstallmentProjection;
import com.hakandincturk.repositories.projections.TopInstallmentProjection;
import com.hakandincturk.repositories.projections.TransactionNextDueProjection;
import com.hakandincturk.repositories.projections.TransactionTagProjection;
import com.hakandincturk.utils.TransactionClassifier;

/**
 * Rapor sorgularının gerçek veritabanında doğrulanması.
 * SKIPPED filtresi, soft delete ve kullanıcı izolasyonu bu seviyede garanti altına alınır.
 */
class ReportRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private ContactRepository contactRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private TransactionRepository transactionRepository;
  @Autowired
  private InstallmentRepository installmentRepository;
  @Autowired
  private TransactionTagRepository transactionTagRepository;
  @Autowired
  private MonthlySummaryRepository monthlySummaryRepository;

  private static final LocalDate MONTH_START = LocalDate.of(2026, 6, 1);
  private static final LocalDate MONTH_END = LocalDate.of(2026, 6, 30);

  private Users testUser;
  private Users otherUser;
  private Account testAccount;
  private Account otherAccount;

  @BeforeEach
  void setUp() {
    monthlySummaryRepository.deleteAll();
    transactionTagRepository.deleteAll();
    installmentRepository.deleteAll();
    transactionRepository.deleteAll();
    tagRepository.deleteAll();
    contactRepository.deleteAll();
    accountRepository.deleteAll();
    userRepository.deleteAll();

    testUser = createUser("report@test.com");
    otherUser = createUser("other@test.com");
    testAccount = createAccount(testUser, "Garanti Bonus");
    otherAccount = createAccount(otherUser, "Diğer Hesap");
  }

  @Test
  @DisplayName("Ay bazlı toplamlar tip ve ödeme durumuna göre gruplanmalı")
  void sumMonthlyAmountsByTypeAndPaidState_shouldGroupByTypeAndPaidState() {
    Transaction expense = createTransaction(testUser, testAccount, "Market", TransactionTypes.DEBT, 1);
    createInstallment(expense, 1, LocalDate.of(2026, 6, 10), BigDecimal.valueOf(300), true);
    createInstallment(expense, 2, LocalDate.of(2026, 6, 20), BigDecimal.valueOf(200), false);

    Transaction income = createTransaction(testUser, testAccount, "Maaş", TransactionTypes.CREDIT, 1);
    createInstallment(income, 1, LocalDate.of(2026, 6, 1), BigDecimal.valueOf(1000), true);

    List<MonthlyTypeAmountProjection> result = installmentRepository
        .sumMonthlyAmountsByTypeAndPaidState(testUser.getId(), MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);

    assertEquals(3, result.size());
    result.forEach(projection -> {
      assertEquals(2026, projection.year());
      assertEquals(6, projection.month());
    });

    BigDecimal expenseTotal = result.stream()
        .filter(projection -> TransactionClassifier.isExpense(projection.type()))
        .map(MonthlyTypeAmountProjection::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, BigDecimal.valueOf(500).compareTo(expenseTotal));
  }

  @Test
  @DisplayName("SKIPPED taksitler hiçbir toplama dahil edilmemeli")
  void sumMonthlyAmountsByTypeAndPaidState_shouldExcludeSkippedInstallments() {
    Transaction transaction = createTransaction(testUser, testAccount, "Kredi", TransactionTypes.DEBT, 3);
    createInstallment(transaction, 1, LocalDate.of(2026, 6, 5), BigDecimal.valueOf(400), false);
    Installment skipped = createInstallment(transaction, 2, LocalDate.of(2026, 6, 15), BigDecimal.valueOf(999), false);
    skipped.setStatus(InstallmentStatuses.SKIPPED);
    installmentRepository.save(skipped);

    List<MonthlyTypeAmountProjection> result = installmentRepository
        .sumMonthlyAmountsByTypeAndPaidState(testUser.getId(), MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);

    BigDecimal total = result.stream().map(MonthlyTypeAmountProjection::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, BigDecimal.valueOf(400).compareTo(total));
  }

  @Test
  @DisplayName("Silinmiş taksit ve işlemler toplamlara girmemeli")
  void sumMonthlyAmountsByTypeAndPaidState_shouldExcludeSoftDeletedRows() {
    Transaction removedTransaction = createTransaction(testUser, testAccount, "Silinmiş İşlem", TransactionTypes.DEBT, 1);
    createInstallment(removedTransaction, 1, LocalDate.of(2026, 6, 5), BigDecimal.valueOf(500), false);
    removedTransaction.setRemoved(true);
    transactionRepository.save(removedTransaction);

    Transaction activeTransaction = createTransaction(testUser, testAccount, "Aktif İşlem", TransactionTypes.DEBT, 1);
    Installment removedInstallment = createInstallment(activeTransaction, 1, LocalDate.of(2026, 6, 6), BigDecimal.valueOf(700), false);
    removedInstallment.setRemoved(true);
    installmentRepository.save(removedInstallment);
    createInstallment(activeTransaction, 2, LocalDate.of(2026, 6, 7), BigDecimal.valueOf(100), false);

    List<MonthlyTypeAmountProjection> result = installmentRepository
        .sumMonthlyAmountsByTypeAndPaidState(testUser.getId(), MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);

    BigDecimal total = result.stream().map(MonthlyTypeAmountProjection::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, BigDecimal.valueOf(100).compareTo(total));
  }

  @Test
  @DisplayName("Başka kullanıcının verisi rapor sorgularına sızmamalı")
  void reportQueries_shouldNotLeakOtherUsersData() {
    Transaction otherTransaction = createTransaction(otherUser, otherAccount, "Başkasının Harcaması", TransactionTypes.DEBT, 1);
    createInstallment(otherTransaction, 1, LocalDate.of(2026, 6, 10), BigDecimal.valueOf(5000), false);

    Transaction ownTransaction = createTransaction(testUser, testAccount, "Kendi Harcamam", TransactionTypes.DEBT, 1);
    createInstallment(ownTransaction, 1, LocalDate.of(2026, 6, 10), BigDecimal.valueOf(100), false);

    List<MonthlyTypeAmountProjection> amounts = installmentRepository
        .sumMonthlyAmountsByTypeAndPaidState(testUser.getId(), MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);
    BigDecimal total = amounts.stream().map(MonthlyTypeAmountProjection::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, BigDecimal.valueOf(100).compareTo(total));

    List<TopInstallmentProjection> topInstallments = installmentRepository.findTopInstallments(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, MONTH_START, MONTH_END,
        InstallmentStatuses.SKIPPED, PageRequest.of(0, 10));
    assertEquals(1, topInstallments.size());
    assertEquals("Kendi Harcamam", topInstallments.get(0).transactionName());

    List<InstallmentTagAmountProjection> tagAmounts = installmentRepository.findInstallmentTagAmounts(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);
    assertEquals(1, tagAmounts.size());

    List<TransactionTagProjection> otherUsersTags = transactionTagRepository
        .findTagsOfTransactions(testUser.getId(), List.of(otherTransaction.getId()));
    assertTrue(otherUsersTags.isEmpty());

    List<TransactionNextDueProjection> otherUsersDueDates = installmentRepository
        .findNextDueDates(testUser.getId(), List.of(otherTransaction.getId()), InstallmentStatuses.SKIPPED);
    assertTrue(otherUsersDueDates.isEmpty());
  }

  @Test
  @DisplayName("Ay bazlı adetlerde aynı işlem tek kez sayılmalı")
  void countMonthlyEntities_shouldCountDistinctTransactions() {
    Transaction transaction = createTransaction(testUser, testAccount, "Taksitli Alışveriş", TransactionTypes.DEBT, 2);
    createInstallment(transaction, 1, LocalDate.of(2026, 6, 5), BigDecimal.valueOf(100), false);
    createInstallment(transaction, 2, LocalDate.of(2026, 6, 25), BigDecimal.valueOf(100), false);

    List<MonthlyEntityCountProjection> result = installmentRepository
        .countMonthlyEntities(testUser.getId(), MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).transactionCount());
    assertEquals(2L, result.get(0).installmentCount());
  }

  @Test
  @DisplayName("Gün bazlı gider toplamları hesaplanmalı")
  void sumDailyAmounts_shouldGroupByDate() {
    Transaction transaction = createTransaction(testUser, testAccount, "Market", TransactionTypes.DEBT, 1);
    createInstallment(transaction, 1, LocalDate.of(2026, 6, 15), BigDecimal.valueOf(700), false);
    createInstallment(transaction, 2, LocalDate.of(2026, 6, 15), BigDecimal.valueOf(300), false);
    createInstallment(transaction, 3, LocalDate.of(2026, 6, 16), BigDecimal.valueOf(100), false);

    List<DailyAmountProjection> result = installmentRepository.sumDailyAmounts(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);

    Map<LocalDate, BigDecimal> amountByDate = result.stream()
        .collect(Collectors.toMap(DailyAmountProjection::date, DailyAmountProjection::amount));

    assertEquals(0, BigDecimal.valueOf(1000).compareTo(amountByDate.get(LocalDate.of(2026, 6, 15))));
    assertEquals(0, BigDecimal.valueOf(100).compareTo(amountByDate.get(LocalDate.of(2026, 6, 16))));
  }

  @Test
  @DisplayName("Etiketsiz taksitler etiket sorgusunda null etiketle dönmeli")
  void findInstallmentTagAmounts_shouldReturnNullTagForUntaggedInstallments() {
    Tag tag = createTag(testUser, "Market");
    Transaction tagged = createTransaction(testUser, testAccount, "Etiketli", TransactionTypes.DEBT, 1);
    createTransactionTag(tagged, tag);
    createInstallment(tagged, 1, LocalDate.of(2026, 6, 10), BigDecimal.valueOf(200), false);

    Transaction untagged = createTransaction(testUser, testAccount, "Etiketsiz", TransactionTypes.DEBT, 1);
    createInstallment(untagged, 1, LocalDate.of(2026, 6, 11), BigDecimal.valueOf(300), false);

    List<InstallmentTagAmountProjection> result = installmentRepository.findInstallmentTagAmounts(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, MONTH_START, MONTH_END, InstallmentStatuses.SKIPPED);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(row -> row.tagId() == null));
    assertTrue(result.stream().anyMatch(row -> "Market".equals(row.tagName())));
  }

  @Test
  @DisplayName("En büyük kalemler taksit tutarına göre azalan sıralanmalı")
  void findTopInstallments_shouldOrderByInstallmentAmount() {
    Transaction big = createTransaction(testUser, testAccount, "Buzdolabı", TransactionTypes.DEBT, 12);
    createInstallment(big, 3, LocalDate.of(2026, 6, 15), BigDecimal.valueOf(1200), false);

    Transaction small = createTransaction(testUser, testAccount, "Kahve", TransactionTypes.DEBT, 1);
    createInstallment(small, 1, LocalDate.of(2026, 6, 16), BigDecimal.valueOf(80), false);

    List<TopInstallmentProjection> result = installmentRepository.findTopInstallments(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, MONTH_START, MONTH_END,
        InstallmentStatuses.SKIPPED, PageRequest.of(0, 1));

    assertEquals(1, result.size());
    assertEquals("Buzdolabı", result.get(0).transactionName());
    assertEquals(3, result.get(0).installmentNumber());
    assertEquals(12, result.get(0).totalInstallment());
    assertEquals("Garanti Bonus", result.get(0).accountName());
    assertNull(result.get(0).contactName());
    assertFalse(result.get(0).paid());
  }

  @Test
  @DisplayName("Ödenmemiş toplam borç ve ödeme ayına göre toplamlar hesaplanmalı")
  void unpaidAndPaidQueries_shouldCalculateRemainingDebtInputs() {
    Transaction transaction = createTransaction(testUser, testAccount, "Kredi", TransactionTypes.DEBT, 3);
    Installment paid = createInstallment(transaction, 1, LocalDate.of(2026, 5, 10), BigDecimal.valueOf(500), true);
    paid.setPaidDate(LocalDate.of(2026, 5, 10));
    installmentRepository.save(paid);
    createInstallment(transaction, 2, LocalDate.of(2026, 6, 10), BigDecimal.valueOf(500), false);
    createInstallment(transaction, 3, LocalDate.of(2026, 7, 10), BigDecimal.valueOf(500), false);

    BigDecimal unpaidTotal = installmentRepository.sumUnpaidAmount(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, InstallmentStatuses.SKIPPED);
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(unpaidTotal));

    assertEquals(1, installmentRepository.sumPaidAmountsByPaidMonth(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, LocalDate.of(2026, 1, 1), InstallmentStatuses.SKIPPED).size());

    // Projeksiyon borcu için vade ayına göre ödenmemiş tutarlar; ödenmiş mayıs taksiti dışarıda kalır
    Map<Integer, BigDecimal> unpaidByDebtMonth = installmentRepository.sumUnpaidAmountsByDebtMonth(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES, LocalDate.of(2026, 1, 1), InstallmentStatuses.SKIPPED)
        .stream()
        .collect(Collectors.toMap(MonthlyAmountProjection::month, MonthlyAmountProjection::amount));

    assertEquals(2, unpaidByDebtMonth.size());
    assertFalse(unpaidByDebtMonth.containsKey(5));
    assertEquals(0, BigDecimal.valueOf(500).compareTo(unpaidByDebtMonth.get(6)));
    assertEquals(0, BigDecimal.valueOf(500).compareTo(unpaidByDebtMonth.get(7)));
  }

  @Test
  @DisplayName("Tekrar eden aday satırları ve ödenmemiş ilk taksit tarihi getirilmeli")
  void recurringQueries_shouldReturnWindowRowsAndNextDueDate() {
    Transaction transaction = createTransaction(testUser, testAccount, "Netflix", TransactionTypes.DEBT, 12);
    Installment paid = createInstallment(transaction, 1, LocalDate.of(2026, 5, 5), BigDecimal.valueOf(229), true);
    paid.setPaidDate(LocalDate.of(2026, 5, 5));
    installmentRepository.save(paid);
    createInstallment(transaction, 2, LocalDate.of(2026, 6, 5), BigDecimal.valueOf(229), false);

    List<RecurringInstallmentProjection> rows = installmentRepository.findRecurringCandidates(
        testUser.getId(), TransactionClassifier.EXPENSE_TYPES,
        LocalDate.of(2026, 1, 1), MONTH_END, InstallmentStatuses.SKIPPED);

    assertEquals(2, rows.size());
    assertEquals("Netflix", rows.get(0).transactionName());
    assertEquals(12, rows.get(0).totalInstallment());

    List<TransactionNextDueProjection> nextDueDates = installmentRepository.findNextDueDates(
        testUser.getId(), List.of(transaction.getId()), InstallmentStatuses.SKIPPED);

    assertEquals(1, nextDueDates.size());
    assertEquals(LocalDate.of(2026, 6, 5), nextDueDates.get(0).nextDueDate());
  }

  private Users createUser(String email) {
    Users user = new Users();
    user.setFullName("Test User");
    user.setEmail(email);
    user.setPassword("encoded-password");
    user.setPhone("5551234567");

    return userRepository.save(user);
  }

  private Account createAccount(Users user, String name) {
    Account account = new Account();
    account.setName(name);
    account.setType(AccountTypes.BANK);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(BigDecimal.valueOf(1000));
    account.setBalance(BigDecimal.valueOf(1000));
    account.setUser(user);

    return accountRepository.save(account);
  }

  private Tag createTag(Users user, String name) {
    Tag tag = new Tag();
    tag.setName(name);
    tag.setUser(user);

    return tagRepository.save(tag);
  }

  private TransactionTag createTransactionTag(Transaction transaction, Tag tag) {
    TransactionTag transactionTag = new TransactionTag();
    transactionTag.setTransaction(transaction);
    transactionTag.setTag(tag);

    return transactionTagRepository.save(transactionTag);
  }

  private Transaction createTransaction(Users user, Account account, String name, TransactionTypes type, int totalInstallment) {
    Transaction transaction = new Transaction();
    transaction.setName(name);
    transaction.setDescription(name + " açıklaması");
    transaction.setUser(user);
    transaction.setAccount(account);
    transaction.setType(type);
    transaction.setStatus(TransactionStatuses.PENDING);
    transaction.setTotalAmount(BigDecimal.valueOf(1000));
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setTotalInstallment(totalInstallment);
    transaction.setDebtDate(LocalDate.of(2026, 6, 1));

    return transactionRepository.save(transaction);
  }

  private Installment createInstallment(Transaction transaction, int number, LocalDate debtDate, BigDecimal amount, boolean paid) {
    Installment installment = new Installment();
    installment.setTransaction(transaction);
    installment.setInstallmentNumber(number);
    installment.setAmount(amount);
    installment.setPaid(paid);
    installment.setDebtDate(debtDate);
    installment.setStatus(InstallmentStatuses.ACTIVE);

    return installmentRepository.save(installment);
  }
}
