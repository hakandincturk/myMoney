package com.hakandincturk.myMoney.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.hakandincturk.core.enums.sort.BaseSortColumn;
import com.hakandincturk.core.enums.sort.TransactionSortColumn;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.PageRequstParams;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.utils.PaginationUtils;

class PaginationUtilsTest {

  @Test
  @DisplayName("PageRequstParams ile Pageable oluşturulmalı")
  void toPageable_withPageRequestParams_shouldCreatePageable() {
    PageRequstParams params = new PageRequstParams();
    params.setPageNumber(2);
    params.setPageSize(20);

    Pageable result = PaginationUtils.toPageable(params);

    assertEquals(2, result.getPageNumber());
    assertEquals(20, result.getPageSize());
  }

  @Test
  @DisplayName("SortablePageRequest sıralama olmadan Pageable oluşturulmalı")
  void toPageable_withSortableRequest_noSort_shouldCreatePageable() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(0);
    request.setPageSize(10);

    Pageable result = PaginationUtils.toPageable(request);

    assertEquals(0, result.getPageNumber());
    assertEquals(10, result.getPageSize());
    assertFalse(result.getSort().isSorted());
  }

  @Test
  @DisplayName("SortablePageRequest sıralama ile Pageable oluşturulmalı")
  void toPageable_withSortableRequest_withSort_shouldCreateSortedPageable() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setColumnName("name");
    request.setAsc(true);

    Pageable result = PaginationUtils.toPageable(request);

    assertTrue(result.getSort().isSorted());
    assertEquals("name", result.getSort().iterator().next().getProperty());
    assertTrue(result.getSort().iterator().next().isAscending());
  }

  @Test
  @DisplayName("SortablePageRequest DESC sıralama ile")
  void toPageable_withSortableRequest_descSort_shouldCreateDescPageable() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setColumnName("createdAt");
    request.setAsc(false);

    Pageable result = PaginationUtils.toPageable(request);

    assertTrue(result.getSort().isSorted());
    assertTrue(result.getSort().iterator().next().isDescending());
  }

  @Test
  @DisplayName("Enum sort column ile Pageable oluşturulmalı")
  void toPageable_withEnumSortColumn_shouldMapColumnCorrectly() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setColumnName("name");
    request.setAsc(true);

    Pageable result = PaginationUtils.toPageable(request, TransactionSortColumn.class);

    assertTrue(result.getSort().isSorted());
  }

  @Test
  @DisplayName("Geçersiz enum sort column kullanıldığında fallback yapılmalı")
  void toPageable_withInvalidEnumColumn_shouldFallbackToOriginal() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setColumnName("nonExistentColumn");
    request.setAsc(true);

    Pageable result = PaginationUtils.toPageable(request, TransactionSortColumn.class);

    assertTrue(result.getSort().isSorted());
    assertEquals("nonExistentColumn", result.getSort().iterator().next().getProperty());
  }

  @Test
  @DisplayName("Enum sort column olmadan boş columnName ile")
  void toPageable_withEnumSortColumn_noColumnName_shouldNotSort() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(1);
    request.setPageSize(25);

    Pageable result = PaginationUtils.toPageable(request, TransactionSortColumn.class);

    assertFalse(result.getSort().isSorted());
    assertEquals(1, result.getPageNumber());
    assertEquals(25, result.getPageSize());
  }

  @Test
  @DisplayName("Page'den PagedResponse'a dönüşüm")
  void toPagedResponse_shouldMapCorrectly() {
    List<String> content = List.of("item1", "item2");
    Page<String> page = new PageImpl<>(content);

    PagedResponse<String> result = PaginationUtils.toPagedResponse(page);

    assertEquals(2, result.getContent().size());
    assertEquals(0, result.getPageNumber());
    assertEquals(2, result.getTotalElements());
    assertTrue(result.isFirst());
    assertTrue(result.isLast());
  }

  @Test
  @DisplayName("Boş sayfa için PagedResponse")
  void toPagedResponse_emptyPage_shouldMapCorrectly() {
    Page<String> page = new PageImpl<>(List.of());

    PagedResponse<String> result = PaginationUtils.toPagedResponse(page);

    assertTrue(result.getContent().isEmpty());
    assertEquals(0, result.getTotalElements());
    assertTrue(result.isFirst());
    assertTrue(result.isLast());
  }

  @Test
  @DisplayName("ColumnName boş string olduğunda sıralama yapılmamalı")
  void toPageable_withEmptyColumnName_shouldNotSort() {
    SortablePageRequest request = new SortablePageRequest();
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setColumnName("   ");

    Pageable result = PaginationUtils.toPageable(request);

    assertFalse(result.getSort().isSorted());
  }
}
