package com.hakandincturk.myMoney.integration.spec;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Users;
import com.hakandincturk.myMoney.integration.BaseIntegrationTest;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.UserRepository;

public abstract class BaseSpecificationTest extends BaseIntegrationTest {

  @Autowired
  protected UserRepository userRepository;
  @Autowired
  protected AccountRepository accountRepository;
  @Autowired
  protected ContactRepository contactRepository;
  @Autowired
  protected TagRepository tagRepository;
  @Autowired
  protected TransactionRepository transactionRepository;
  @Autowired
  protected InstallmentRepository installmentRepository;
  @Autowired
  protected TransactionTagRepository transactionTagRepository;
  @Autowired
  protected MonthlySummaryRepository monthlySummaryRepository;

  protected Users testUser;
  protected Account testAccount;

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

    testUser = new Users();
    testUser.setFullName("Test User");
    testUser.setEmail("spec-test@test.com");
    testUser.setPassword("pass");
    testUser.setPhone("555");
    testUser = userRepository.save(testUser);

    testAccount = new Account();
    testAccount.setName("Test Hesap");
    testAccount.setType(AccountTypes.BANK);
    testAccount.setCurrency(CurrencyTypes.TL);
    testAccount.setTotalBalance(BigDecimal.valueOf(10000));
    testAccount.setBalance(BigDecimal.valueOf(10000));
    testAccount.setUser(testUser);
    testAccount = accountRepository.save(testAccount);
  }

  protected Contact createContact(String fullName) {
    Contact contact = new Contact();
    contact.setFullName(fullName);
    contact.setUser(testUser);
    return contactRepository.save(contact);
  }

  protected Tag createTag(String name) {
    Tag tag = new Tag();
    tag.setName(name);
    tag.setUser(testUser);
    return tagRepository.save(tag);
  }

  protected Transaction createTransaction(String name, TransactionTypes type) {
    Transaction transaction = new Transaction();
    transaction.setName(name);
    transaction.setUser(testUser);
    transaction.setAccount(testAccount);
    transaction.setType(type);
    transaction.setStatus(TransactionStatuses.PENDING);
    transaction.setTotalAmount(BigDecimal.valueOf(1000));
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setTotalInstallment(1);
    transaction.setDebtDate(LocalDate.now());
    return transactionRepository.save(transaction);
  }

  protected Transaction createTransactionWithAmount(String name, BigDecimal amount) {
    Transaction transaction = new Transaction();
    transaction.setName(name);
    transaction.setUser(testUser);
    transaction.setAccount(testAccount);
    transaction.setType(TransactionTypes.DEBT);
    transaction.setStatus(TransactionStatuses.PENDING);
    transaction.setTotalAmount(amount);
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setTotalInstallment(1);
    transaction.setDebtDate(LocalDate.now());
    return transactionRepository.save(transaction);
  }

  protected Installment createInstallment(Transaction transaction, int number, LocalDate debtDate) {
    Installment installment = new Installment();
    installment.setTransaction(transaction);
    installment.setInstallmentNumber(number);
    installment.setAmount(BigDecimal.valueOf(333));
    installment.setPaid(false);
    installment.setDebtDate(debtDate);
    return installmentRepository.save(installment);
  }

  protected Account createSecondAccount(String name) {
    Account account = new Account();
    account.setName(name);
    account.setType(AccountTypes.BANK);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(BigDecimal.valueOf(5000));
    account.setBalance(BigDecimal.valueOf(5000));
    account.setUser(testUser);
    return accountRepository.save(account);
  }

  protected TransactionTag createTransactionTag(Transaction transaction, Tag tag) {
    TransactionTag tt = new TransactionTag();
    tt.setTransaction(transaction);
    tt.setTag(tag);
    return transactionTagRepository.save(tt);
  }
}
