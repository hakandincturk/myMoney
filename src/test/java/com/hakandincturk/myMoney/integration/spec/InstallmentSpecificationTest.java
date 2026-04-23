package com.hakandincturk.myMoney.integration.spec;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.specs.FilterListMyInstallmentSpecification;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;

class InstallmentSpecificationTest extends BaseSpecificationTest {

  @Test
  @DisplayName("Ay ve yıla göre filtreleme")
  void shouldFilterByMonthAndYear() {
    Transaction transaction = createTransaction("Borç", TransactionTypes.DEBT);
    createInstallment(transaction, 1, LocalDate.of(2025, 6, 15));
    createInstallment(transaction, 2, LocalDate.of(2025, 7, 15));

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);

    Page<Installment> result = installmentRepository.findAll(FilterListMyInstallmentSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().get(0).getInstallmentNumber());
  }

  @Test
  @DisplayName("Ödeme durumuna göre filtreleme")
  void shouldFilterByPaidStatus() {
    Transaction transaction = createTransaction("Borç", TransactionTypes.DEBT);
    Installment paid = createInstallment(transaction, 1, LocalDate.of(2025, 6, 1));
    paid.setPaid(true);
    installmentRepository.save(paid);
    createInstallment(transaction, 2, LocalDate.of(2025, 6, 15));

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);
    filter.setIsPaid(List.of(false));

    Page<Installment> result = installmentRepository.findAll(FilterListMyInstallmentSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertFalse(result.getContent().get(0).isPaid());
  }

  @Test
  @DisplayName("Transaction ismine göre filtreleme")
  void shouldFilterByTransactionName() {
    createInstallment(createTransaction("Market", TransactionTypes.DEBT), 1, LocalDate.of(2025, 6, 1));
    createInstallment(createTransaction("Kira", TransactionTypes.PAYMENT), 1, LocalDate.of(2025, 6, 15));

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);
    filter.setTransactionName("market");

    Page<Installment> result = installmentRepository.findAll(FilterListMyInstallmentSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Açıklamaya göre filtreleme")
  void shouldFilterByDescription() {
    Transaction transaction = createTransaction("Borç", TransactionTypes.DEBT);

    Installment i1 = createInstallment(transaction, 1, LocalDate.of(2025, 6, 1));
    i1.setDescription("Haziran ödemesi");
    installmentRepository.save(i1);

    Installment i2 = createInstallment(transaction, 2, LocalDate.of(2025, 6, 15));
    i2.setDescription("Temmuz ödemesi");
    installmentRepository.save(i2);

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);
    filter.setDescription("haziran");

    Specification<Installment> spec = FilterListMyInstallmentSpecification.filter(testUser.getId(), filter);
    Page<Installment> result = installmentRepository.findAll(spec, PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Sadece minTotalAmount ile filtreleme")
  void shouldFilterByMinAmountOnly() {
    Transaction transaction = createTransaction("Borç", TransactionTypes.DEBT);
    Installment small = createInstallment(transaction, 1, LocalDate.of(2025, 6, 1));
    small.setAmount(BigDecimal.valueOf(100));
    installmentRepository.save(small);
    Installment large = createInstallment(transaction, 2, LocalDate.of(2025, 6, 15));
    large.setAmount(BigDecimal.valueOf(5000));
    installmentRepository.save(large);

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);
    filter.setMinTotalAmount(BigDecimal.valueOf(1000));

    Page<Installment> result = installmentRepository.findAll(FilterListMyInstallmentSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals(0, BigDecimal.valueOf(5000).compareTo(result.getContent().get(0).getAmount()));
  }

  @Test
  @DisplayName("Sadece maxTotalAmount ile filtreleme")
  void shouldFilterByMaxAmountOnly() {
    Transaction transaction = createTransaction("Borç", TransactionTypes.DEBT);
    Installment small = createInstallment(transaction, 1, LocalDate.of(2025, 6, 1));
    small.setAmount(BigDecimal.valueOf(100));
    installmentRepository.save(small);
    Installment large = createInstallment(transaction, 2, LocalDate.of(2025, 6, 15));
    large.setAmount(BigDecimal.valueOf(5000));
    installmentRepository.save(large);

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);
    filter.setMaxTotalAmount(BigDecimal.valueOf(500));

    Page<Installment> result = installmentRepository.findAll(FilterListMyInstallmentSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals(0, BigDecimal.valueOf(100).compareTo(result.getContent().get(0).getAmount()));
  }

  @Test
  @DisplayName("Tutar aralığına göre filtreleme")
  void shouldFilterByAmountRange() {
    Transaction transaction = createTransaction("Borç", TransactionTypes.DEBT);
    Installment small = createInstallment(transaction, 1, LocalDate.of(2025, 6, 1));
    small.setAmount(BigDecimal.valueOf(100));
    installmentRepository.save(small);
    Installment large = createInstallment(transaction, 2, LocalDate.of(2025, 6, 15));
    large.setAmount(BigDecimal.valueOf(5000));
    installmentRepository.save(large);

    FilterListMyInstallmentRequestDto filter = new FilterListMyInstallmentRequestDto();
    filter.setMonth(6);
    filter.setYear(2025);
    filter.setMinTotalAmount(BigDecimal.valueOf(1000));
    filter.setMaxTotalAmount(BigDecimal.valueOf(10000));

    Page<Installment> result = installmentRepository.findAll(FilterListMyInstallmentSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals(0, BigDecimal.valueOf(5000).compareTo(result.getContent().get(0).getAmount()));
  }
}
