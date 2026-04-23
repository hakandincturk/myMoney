package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.UserRepository;

class RepositoryIntegrationTest extends BaseIntegrationTest {

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

  private Users testUser;

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
    testUser.setEmail("test@test.com");
    testUser.setPassword("encoded-password");
    testUser.setPhone("5551234567");
    testUser = userRepository.save(testUser);
  }

  // --- Account Repository ---

  @Test
  @DisplayName("Aktif hesaplar kullanıcıya göre listelenmeli")
  void findByUserIdAndIsRemovedFalse_shouldReturnActiveAccounts() {
    Account active = createAccount("Aktif Hesap", false);
    Account removed = createAccount("Silinmiş Hesap", true);

    Page<Account> result = accountRepository.findByUserIdAndIsRemovedFalse(testUser.getId(), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("Aktif Hesap", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Hesap ID ve kullanıcı ID ile bulunmalı")
  void findByIdAndUserIdAndIsRemovedFalse_shouldReturnAccount() {
    Account account = createAccount("Test Hesap", false);

    Optional<Account> result = accountRepository.findByIdAndUserIdAndIsRemovedFalse(account.getId(), testUser.getId());

    assertTrue(result.isPresent());
    assertEquals("Test Hesap", result.get().getName());
  }

  @Test
  @DisplayName("Silinmiş hesap bulunamaz")
  void findByIdAndUserIdAndIsRemovedFalse_shouldNotReturnRemovedAccount() {
    Account removed = createAccount("Silinmiş", true);

    Optional<Account> result = accountRepository.findByIdAndUserIdAndIsRemovedFalse(removed.getId(), testUser.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Hesap bakiye toplamı tiplere göre hesaplanmalı")
  void sumBalanceByUserIdAndTypes_shouldSumCorrectly() {
    createAccountWithBalance("Banka", AccountTypes.BANK, BigDecimal.valueOf(5000));
    createAccountWithBalance("Nakit", AccountTypes.CASH, BigDecimal.valueOf(3000));
    createAccountWithBalance("Kredi Kartı", AccountTypes.CREDIT_CARD, BigDecimal.valueOf(10000));

    BigDecimal result = accountRepository.sumBalanceByUserIdAndTypes(
        testUser.getId(), List.of(AccountTypes.BANK, AccountTypes.CASH));

    assertEquals(0, BigDecimal.valueOf(8000).compareTo(result));
  }

  // --- Contact Repository ---

  @Test
  @DisplayName("Aktif kişiler kullanıcıya göre listelenmeli")
  void contact_findByUserIdAndIsRemovedFalse_shouldReturnActiveContacts() {
    createContact("Ali Veli", false);
    createContact("Silinmiş Kişi", true);

    Page<Contact> result = contactRepository.findByUserIdAndIsRemovedFalse(testUser.getId(), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
  }

  // --- Tag Repository ---

  @Test
  @DisplayName("Etiket ID ve kullanıcı ID ile bulunmalı")
  void tag_findByIdAndUserIdAndIsRemovedFalse_shouldReturnTag() {
    Tag tag = createTag("Yemek");

    Optional<Tag> result = tagRepository.findByIdAndUserIdAndIsRemovedFalse(tag.getId(), testUser.getId());

    assertTrue(result.isPresent());
    assertEquals("Yemek", result.get().getName());
  }

  @Test
  @DisplayName("Birden fazla etiket ID ile toplu getirilmeli")
  void tag_findAllByIdInAndIsRemovedFalse_shouldReturnAllTags() {
    Tag tag1 = createTag("Yemek");
    Tag tag2 = createTag("Ulaşım");
    Tag removed = createTag("Silinmiş");
    removed.setRemoved(true);
    tagRepository.save(removed);

    List<Tag> result = tagRepository.findAllByIdInAndIsRemovedFalse(List.of(tag1.getId(), tag2.getId(), removed.getId()));

    assertEquals(2, result.size());
  }

  // --- User Repository ---

  @Test
  @DisplayName("Email ile kullanıcı bulunmalı")
  void user_findByEmailAndIsRemovedFalse_shouldReturnUser() {
    Optional<Users> result = userRepository.findByEmailAndIsRemovedFalse("test@test.com");

    assertTrue(result.isPresent());
    assertEquals("Test User", result.get().getFullName());
  }

  @Test
  @DisplayName("Silinmiş kullanıcı email ile bulunamaz")
  void user_findByEmailAndIsRemovedFalse_shouldNotReturnRemovedUser() {
    Users removed = new Users();
    removed.setFullName("Removed");
    removed.setEmail("removed@test.com");
    removed.setPassword("pass");
    removed.setPhone("123");
    removed.setRemoved(true);
    userRepository.save(removed);

    Optional<Users> result = userRepository.findByEmailAndIsRemovedFalse("removed@test.com");

    assertTrue(result.isEmpty());
  }

  // --- Transaction Repository ---

  @Test
  @DisplayName("Son 10 transaction ID'ye göre sıralı getirilmeli")
  void transaction_findTop10_shouldReturnOrderedByIdDesc() {
    Account account = createAccount("Hesap", false);

    for (int i = 0; i < 12; i++) {
      createTransaction("İşlem " + i, account, TransactionTypes.DEBT);
    }

    List<Transaction> result = transactionRepository.findTop10ByUserIdAndIsRemovedFalseOrderByIdDesc(testUser.getId());

    assertEquals(10, result.size());
    assertTrue(result.get(0).getId() > result.get(9).getId());
  }

  // --- Installment Repository ---

  @Test
  @DisplayName("Tarih aralığına göre taksitler getirilmeli")
  void installment_findByDateRange_shouldReturnCorrectInstallments() {
    Account account = createAccount("Hesap", false);
    Transaction transaction = createTransaction("Borç", account, TransactionTypes.DEBT);

    Installment i1 = createInstallment(transaction, 1, LocalDate.of(2025, 6, 1));
    Installment i2 = createInstallment(transaction, 2, LocalDate.of(2025, 7, 1));
    Installment i3 = createInstallment(transaction, 3, LocalDate.of(2025, 8, 1));

    List<Installment> result = installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(
        testUser.getId(), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));

    assertEquals(1, result.size());
    assertEquals(1, result.get(0).getInstallmentNumber());
  }

  // --- Helper Methods ---

  private Account createAccount(String name, boolean removed) {
    return createAccountWithBalance(name, AccountTypes.BANK, BigDecimal.valueOf(1000), removed);
  }

  private Account createAccountWithBalance(String name, AccountTypes type, BigDecimal balance) {
    return createAccountWithBalance(name, type, balance, false);
  }

  private Account createAccountWithBalance(String name, AccountTypes type, BigDecimal balance, boolean removed) {
    Account account = new Account();
    account.setName(name);
    account.setType(type);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(balance);
    account.setBalance(balance);
    account.setUser(testUser);
    account.setRemoved(removed);
    return accountRepository.save(account);
  }

  private Contact createContact(String fullName, boolean removed) {
    Contact contact = new Contact();
    contact.setFullName(fullName);
    contact.setNote("Test note");
    contact.setUser(testUser);
    contact.setRemoved(removed);
    return contactRepository.save(contact);
  }

  private Tag createTag(String name) {
    Tag tag = new Tag();
    tag.setName(name);
    tag.setUser(testUser);
    return tagRepository.save(tag);
  }

  private Transaction createTransaction(String name, Account account, TransactionTypes type) {
    Transaction transaction = new Transaction();
    transaction.setName(name);
    transaction.setUser(testUser);
    transaction.setAccount(account);
    transaction.setType(type);
    transaction.setStatus(TransactionStatuses.PENDING);
    transaction.setTotalAmount(BigDecimal.valueOf(1000));
    transaction.setPaidAmount(BigDecimal.ZERO);
    transaction.setTotalInstallment(3);
    transaction.setDebtDate(LocalDate.now());
    return transactionRepository.save(transaction);
  }

  private Installment createInstallment(Transaction transaction, int number, LocalDate debtDate) {
    Installment installment = new Installment();
    installment.setTransaction(transaction);
    installment.setInstallmentNumber(number);
    installment.setAmount(BigDecimal.valueOf(333));
    installment.setPaid(false);
    installment.setDebtDate(debtDate);
    return installmentRepository.save(installment);
  }
}
