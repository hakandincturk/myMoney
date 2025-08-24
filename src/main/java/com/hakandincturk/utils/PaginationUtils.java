package com.hakandincturk.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.hakandincturk.core.enums.sort.BaseSortColumn;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.PageRequstParams;
import com.hakandincturk.dtos.SortablePageRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PaginationUtils {

  public static Pageable toPageable(PageRequstParams request) {
    return PageRequest.of(request.getPageNumber(), request.getPageSize());
  }
  
  public static Pageable toPageable(SortablePageRequest request) {
    if (!isNullOrEmpty(request.getColumnName())) {
      Direction sortDirection = request.isAsc() ? Direction.ASC : Direction.DESC;
      Sort sortBy = Sort.by(sortDirection, request.getColumnName());
      return PageRequest.of(request.getPageNumber(), request.getPageSize(), sortBy);
    }
    
    return PageRequest.of(request.getPageNumber(), request.getPageSize());
  }
  
  // Generic method for all sort columns
  public static <T extends Enum<T> & BaseSortColumn> Pageable toPageable(SortablePageRequest request, Class<T> sortColumnClass) {
    if (!isNullOrEmpty(request.getColumnName())) {
      Direction sortDirection = request.isAsc() ? Direction.ASC : Direction.DESC;
      
      try {
        T sortColumn = BaseSortColumn.fromString(sortColumnClass, request.getColumnName());
        Sort sortBy = Sort.by(sortDirection, sortColumn.getEntityProperty());
        return PageRequest.of(request.getPageNumber(), request.getPageSize(), sortBy);
      } catch (IllegalArgumentException e) {
        // Fallback to original column name
        Sort sortBy = Sort.by(sortDirection, request.getColumnName());
        return PageRequest.of(request.getPageNumber(), request.getPageSize(), sortBy);
      }
    }
    
    return PageRequest.of(request.getPageNumber(), request.getPageSize());
  }

  public static <T> PagedResponse<T> toPagedResponse(Page<T> page) {
    return new PagedResponse<>(
      page.getContent(),
      page.getNumber(),
      page.getSize(),
      page.getTotalElements(),
      page.getTotalPages(),
      page.isFirst(),
      page.isLast()
    );
  }
  
  private static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }
}
