package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;
import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendDataDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.services.abstracts.DashboardService;
import com.hakandincturk.services.abstracts.TransactionService;

/**
 * The home page figures are computed live from installments. This fixture covers every case the old
 * monthly_summary cache got wrong or that changes which bucket an amount lands in: late and early
 * payments, skipped installments, deleted transactions, another user's data and the last month of the
 * trend window. Expected values are worked out by hand in each test.
 */
class DashboardLiveSummaryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private DashboardService dashboardService;
  @Autowired
  private TransactionService transactionService;
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

  private final YearMonth current = YearMonth.now();
  private final YearMonth previous = current.minusMonths(1);

  private Users user;
  private Account account;
  private Transaction monthlyGrocery;

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

    user = createUser("dashboard@test.com");
    account = createAccount(user);

    // Expense due this month: 300 paid this month, 200 still waiting
    monthlyGrocery = createTransaction(user, "Market", TransactionTypes.DEBT);
    createInstallment(monthlyGrocery, current.atDay(10), "300", current.atDay(10), InstallmentStatuses.ACTIVE);
    createInstallment(monthlyGrocery, current.atDay(20), "200", null, InstallmentStatuses.ACTIVE);

    // Income due this month: 1000 paid this month, 500 still waiting
    Transaction salary = createTransaction(user, "Maaş", TransactionTypes.CREDIT);
    createInstallment(salary, current.atDay(1), "1000", current.atDay(1), InstallmentStatuses.ACTIVE);
    createInstallment(salary, current.atDay(25), "500", null, InstallmentStatuses.ACTIVE);

    // Due last month, paid late this month: occurred this month, not part of this month's due total
    Transaction latePaid = createTransaction(user, "Fatura", TransactionTypes.DEBT);
    createInstallment(latePaid, previous.atDay(15), "150", current.atDay(3), InstallmentStatuses.ACTIVE);

    // Due this month, paid early last month: part of this month's due total, occurred last month
    Transaction earlyPaid = createTransaction(user, "Aidat", TransactionTypes.DEBT);
    createInstallment(earlyPaid, current.atDay(28), "80", previous.atDay(28), InstallmentStatuses.ACTIVE);

    // Income due and paid last month: base of the income change rate
    Transaction previousSalary = createTransaction(user, "Geçen ay maaş", TransactionTypes.CREDIT);
    createInstallment(previousSalary, previous.atDay(1), "800", previous.atDay(1), InstallmentStatuses.ACTIVE);

    // Never counted: a skipped installment and a deleted transaction
    Transaction skipped = createTransaction(user, "Ödenmeyecek", TransactionTypes.DEBT);
    createInstallment(skipped, current.atDay(5), "999", null, InstallmentStatuses.SKIPPED);

    // Deleted through the real service path (soft deletes the transaction and its installments)
    Transaction deleted = createTransaction(user, "Silinmiş", TransactionTypes.DEBT);
    createInstallment(deleted, current.atDay(6), "777", null, InstallmentStatuses.ACTIVE);
    transactionService.deleteMyTransaction(user.getId(), deleted.getId());

    // Last month of the trend window, and the first month outside it
    Transaction futurePlan = createTransaction(user, "Plan", TransactionTypes.DEBT);
    createInstallment(futurePlan, current.plusMonths(5).atDay(1), "1234", null, InstallmentStatuses.ACTIVE);
    createInstallment(futurePlan, current.plusMonths(6).atDay(1), "4321", null, InstallmentStatuses.ACTIVE);

    // Another user's data must never leak in
    Users otherUser = createUser("other@test.com");
    Transaction otherIncome = createTransaction(otherUser, "Diğer", TransactionTypes.CREDIT);
    otherIncome.setAccount(createAccount(otherUser));
    transactionRepository.save(otherIncome);
    createInstallment(otherIncome, current.atDay(2), "5000", current.atDay(2), InstallmentStatuses.ACTIVE);
  }

  @Test
  @DisplayName("Hızlı bakış - bekleyen vade ayına, gerçekleşen ödeme ayına göre canlı hesaplanmalı")
  void quickView_shouldComputeWaitingByDueDateAndOccurredByPaidDate() {
    QuickViewResponseDto result = dashboardService.quickViewResponse(user.getId());

    // Income: waiting 500 (due this month, unpaid), occurred 1000 (paid this month)
    assertMoney("500", result.getIncome().getWaiting());
    assertMoney("1000", result.getIncome().getOccured());
    // Expense: waiting 200; occurred 300 + 150 (late payment); skipped 999 and deleted 777 excluded
    assertMoney("200", result.getExpense().getWaiting());
    assertMoney("450", result.getExpense().getOccured());

    // Change rates compare what fell due this month with what fell due last month (like the reports)
    // income (1000 + 500 - 800) / 800 = 87.5
    assertEquals(87.5, result.getIncome().getLastMonthChangeRate());
    // expense: last month's bill (150) belongs to last month's base even though it was paid this month
    // (300 + 200 + 80 - 150) / 150 = 286.67
    assertEquals(286.67, result.getExpense().getLastMonthChangeRate());

    // (1500 - 650) / 1500 * 100
    assertEquals(56.67, result.getSavingRate());
  }

  @Test
  @DisplayName("Hızlı bakış - işlem silindiği anda rakamlardan düşmeli")
  void quickView_shouldReflectDeletedTransactionImmediately() {
    transactionService.deleteMyTransaction(user.getId(), monthlyGrocery.getId());

    QuickViewResponseDto result = dashboardService.quickViewResponse(user.getId());

    // Market's 200 waiting and 300 occurred are gone; only the late-paid 150 is left
    assertMoney("0", result.getExpense().getWaiting());
    assertMoney("150", result.getExpense().getOccured());
    assertMoney("500", result.getIncome().getWaiting());
  }

  @Test
  @DisplayName("Aylık trend - 6 ay geri 5 ay ileri, son ay dahil, her ay vade ayına göre canlı hesaplanmalı")
  void monthlyTrend_shouldComputeEveryMonthOfTheWindowLive() {
    List<MonthlyTrendDataDto> trend = dashboardService.monthlyTrend(user.getId()).getMonthlyTrendData();

    assertEquals(12, trend.size());
    assertEquals(current.minusMonths(6).getMonth().toString(), trend.get(0).getTitle());
    assertEquals(current.plusMonths(5).getMonth().toString(), trend.get(11).getTitle());

    // Previous month: salary 800 due; the late-paid bill (150) counts in the month it was due
    assertMoney("800", trend.get(5).getIncome());
    assertMoney("150", trend.get(5).getExpense());

    // Current month: income 1000 + 500, expense 300 + 200 + 80
    assertMoney("1500", trend.get(6).getIncome());
    assertMoney("580", trend.get(6).getExpense());

    // The last month of the window is computed (the old cache skipped users' last months);
    // the month after the window (4321) is not part of the chart
    assertMoney("1234", trend.get(11).getExpense());
    assertMoney("0", trend.get(10).getExpense());
  }

  private void assertMoney(String expected, BigDecimal actual) {
    assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
  }

  private Users createUser(String email) {
    Users created = new Users();
    created.setFullName("Test User");
    created.setEmail(email);
    created.setPassword("encoded-password");
    created.setPhone("5551234567");

    return userRepository.save(created);
  }

  private Account createAccount(Users owner) {
    Account created = new Account();
    created.setName("Hesap");
    created.setType(AccountTypes.BANK);
    created.setCurrency(CurrencyTypes.TL);
    created.setTotalBalance(BigDecimal.valueOf(1000));
    created.setBalance(BigDecimal.valueOf(1000));
    created.setUser(owner);

    return accountRepository.save(created);
  }

  private Transaction createTransaction(Users owner, String name, TransactionTypes type) {
    Transaction transaction = new Transaction();
    transaction.setName(name);
    transaction.setUser(owner);
    transaction.setAccount(account);
    transaction.setType(type);
    transaction.setStatus(TransactionStatuses.PENDING);
    transaction.setTotalAmount(BigDecimal.valueOf(1000));
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setTotalInstallment(1);
    transaction.setDebtDate(current.atDay(1));

    return transactionRepository.save(transaction);
  }

  private Installment createInstallment(Transaction transaction, LocalDate debtDate, String amount, LocalDate paidDate, InstallmentStatuses status) {
    Installment installment = new Installment();
    installment.setTransaction(transaction);
    installment.setInstallmentNumber(1);
    installment.setAmount(new BigDecimal(amount));
    installment.setPaid(paidDate != null);
    installment.setPaidDate(paidDate);
    installment.setDebtDate(debtDate);
    installment.setStatus(status);

    return installmentRepository.save(installment);
  }
}
