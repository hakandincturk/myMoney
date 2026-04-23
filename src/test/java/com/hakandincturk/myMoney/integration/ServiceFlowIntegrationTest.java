package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.CreateTransactionTagDetail;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
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
import com.hakandincturk.services.abstracts.AccountService;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.services.abstracts.ContactService;
import com.hakandincturk.services.abstracts.TransactionService;

import org.springframework.transaction.annotation.Transactional;

@Transactional
class ServiceFlowIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private AuthService authService;
  @Autowired
  private AccountService accountService;
  @Autowired
  private ContactService contactService;
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
    testUser.setFullName("Flow Test User");
    testUser.setEmail("flow@test.com");
    testUser.setPassword("encoded-pass");
    testUser.setPhone("5559999999");
    testUser = userRepository.save(testUser);
  }

  // --- Auth Flow ---

  @Test
  @DisplayName("Kayıt akışı - kullanıcı başarıyla kayıt olmalı")
  void registerFlow_shouldCreateUser() {
    RegisterRequestDto body = new RegisterRequestDto("Yeni Kullanıcı", "yeni@test.com", "password123", "5551111111");

    authService.register(body);

    assertTrue(userRepository.findByEmailAndIsRemovedFalse("yeni@test.com").isPresent());
  }

  @Test
  @DisplayName("Kayıt akışı - aynı email ile tekrar kayıt ConflictException fırlatmalı")
  void registerFlow_duplicateEmail_shouldThrowConflict() {
    RegisterRequestDto body = new RegisterRequestDto("Test", "flow@test.com", "password123", "555");

    assertThrows(ConflictException.class, () -> authService.register(body));
  }

  // --- Account Flow ---

  @Test
  @DisplayName("Hesap oluşturma ve listeleme akışı")
  void accountFlow_createAndList() {
    CreateAccountRequestDto body = new CreateAccountRequestDto("Akbank", AccountTypes.BANK, CurrencyTypes.TL, BigDecimal.valueOf(15000));

    accountService.createAccount(body, testUser.getId());

    Page<ListMyAccountsResponseDto> accounts = accountService.listMyActiveAccounts(testUser.getId(), PageRequest.of(0, 10));

    assertEquals(1, accounts.getTotalElements());
    assertEquals("Akbank", accounts.getContent().get(0).getName());
    assertEquals(0, BigDecimal.valueOf(15000).compareTo(accounts.getContent().get(0).getBalance()));
  }

  @Test
  @DisplayName("Hesap güncelleme akışı - bakiye doğru hesaplanmalı")
  void accountFlow_updateShouldRecalculateBalance() {
    Account account = new Account();
    account.setName("Test");
    account.setType(AccountTypes.BANK);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(BigDecimal.valueOf(10000));
    account.setBalance(BigDecimal.valueOf(8000));
    account.setUser(testUser);
    account = accountRepository.save(account);

    UpdateAccountRequestDto body = new UpdateAccountRequestDto("Test Updated", BigDecimal.valueOf(12000));

    accountService.updateMyAccount(testUser.getId(), account.getId(), body);

    Account updated = accountRepository.findById(account.getId()).get();
    assertEquals("Test Updated", updated.getName());
    assertEquals(0, BigDecimal.valueOf(12000).compareTo(updated.getTotalBalance()));
    assertEquals(0, BigDecimal.valueOf(10000).compareTo(updated.getBalance()));
  }

  // --- Contact Flow ---

  @Test
  @DisplayName("Kişi oluşturma, güncelleme ve silme akışı")
  void contactFlow_createUpdateDelete() {
    contactService.createAccount(testUser.getId(), new CreateContactRequestDto("Ali Veli", "İş arkadaşı"));

    var contacts = contactRepository.findByUserIdAndIsRemovedFalse(testUser.getId(), PageRequest.of(0, 10));
    assertEquals(1, contacts.getTotalElements());
    Long contactId = contacts.getContent().get(0).getId();

    contactService.updateMyContact(testUser.getId(), contactId, new UpdateMyContactRequestDto("Ali Yılmaz", "Eski iş arkadaşı"));

    var updated = contactRepository.findByIdAndUserIdAndIsRemovedFalse(contactId, testUser.getId()).get();
    assertEquals("Ali Yılmaz", updated.getFullName());

    contactService.deleteContact(testUser.getId(), contactId);

    assertTrue(contactRepository.findByIdAndUserIdAndIsRemovedFalse(contactId, testUser.getId()).isEmpty());
  }

  // --- Transaction Flow ---

  @Test
  @DisplayName("Transaction oluşturma - taksitler otomatik oluşmalı")
  void transactionFlow_shouldCreateWithInstallments() {
    Account account = new Account();
    account.setName("Hesap");
    account.setType(AccountTypes.BANK);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(BigDecimal.valueOf(50000));
    account.setBalance(BigDecimal.valueOf(50000));
    account.setUser(testUser);
    account = accountRepository.save(account);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setName("Laptop");
    body.setAccountId(account.getId());
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(30000));
    body.setTotalInstallment(6);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    transactionService.createTransaction(testUser.getId(), body);

    List<Transaction> transactions = transactionRepository.findTop10ByUserIdAndIsRemovedFalseOrderByIdDesc(testUser.getId());
    assertEquals(1, transactions.size());

    Transaction created = transactions.get(0);
    assertEquals("Laptop", created.getName());
    assertEquals(TransactionStatuses.PENDING, created.getStatus());
    assertEquals(0, BigDecimal.ZERO.compareTo(created.getPaidAmount()));

    List<ListInstallments> installments = transactionService.listTransactionInstallments(testUser.getId(), created.getId());
    assertEquals(6, installments.size());
  }

  @Test
  @DisplayName("Transaction oluşturma - yeni tag'ler de kaydedilmeli")
  void transactionFlow_shouldCreateWithNewTags() {
    Account account = new Account();
    account.setName("Hesap");
    account.setType(AccountTypes.BANK);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(BigDecimal.valueOf(50000));
    account.setBalance(BigDecimal.valueOf(50000));
    account.setUser(testUser);
    account = accountRepository.save(account);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setName("Market");
    body.setAccountId(account.getId());
    body.setType(TransactionTypes.PAYMENT);
    body.setTotalAmount(BigDecimal.valueOf(500));
    body.setTotalInstallment(1);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of("Yemek", "Market")));

    transactionService.createTransaction(testUser.getId(), body);

    assertEquals(2, tagRepository.findAll().size());
  }

  @Test
  @DisplayName("Transaction silme - soft delete ve taksitler de silinmeli")
  void transactionFlow_deleteShouldSoftDeleteWithInstallments() {
    Account account = new Account();
    account.setName("Hesap");
    account.setType(AccountTypes.BANK);
    account.setCurrency(CurrencyTypes.TL);
    account.setTotalBalance(BigDecimal.valueOf(50000));
    account.setBalance(BigDecimal.valueOf(50000));
    account.setUser(testUser);
    account = accountRepository.save(account);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setName("Silinecek");
    body.setAccountId(account.getId());
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(3000));
    body.setTotalInstallment(3);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    transactionService.createTransaction(testUser.getId(), body);

    Transaction created = transactionRepository.findTop10ByUserIdAndIsRemovedFalseOrderByIdDesc(testUser.getId()).get(0);

    transactionService.deleteMyTransaction(testUser.getId(), created.getId());

    assertTrue(transactionRepository.findByIdAndUserIdAndIsRemovedFalse(created.getId(), testUser.getId()).isEmpty());
  }

  @Test
  @DisplayName("Var olmayan transaction silinmeye çalışıldığında NotFoundException fırlatılmalı")
  void transactionFlow_deleteNonExistent_shouldThrowNotFound() {
    assertThrows(NotFoundException.class,
        () -> transactionService.deleteMyTransaction(testUser.getId(), 999999L));
  }
}
