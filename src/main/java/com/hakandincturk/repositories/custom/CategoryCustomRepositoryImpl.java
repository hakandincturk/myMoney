package com.hakandincturk.repositories.custom;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.hakandincturk.dtos.category.response.ListUserCategoriesWithTransactionCountDto;
import com.hakandincturk.models.Category;
import com.hakandincturk.models.TransactionCategory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryCustomRepositoryImpl implements CategoryCustomRepository {

  private final EntityManager entityManager;

  @Override
  public Page<ListUserCategoriesWithTransactionCountDto> findWithTransactionCount(Specification<Category> specs, Pageable pageable) {

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ListUserCategoriesWithTransactionCountDto> criteriaQuery = criteriaBuilder.createQuery(ListUserCategoriesWithTransactionCountDto.class);

    Root<Category> root = criteriaQuery.from(Category.class);
    Join<Category, TransactionCategory> transactionCategory = root.join("categoryTransactions", JoinType.LEFT);

    Predicate predicate = specs.toPredicate(root, criteriaQuery, criteriaBuilder);

    Expression<Long> transactionCountExpression =  criteriaBuilder.countDistinct(transactionCategory.get("transaction").get("id"));

    criteriaQuery.select(criteriaBuilder.construct(
        ListUserCategoriesWithTransactionCountDto.class,
        root.get("id"),
        root.get("name"),
        root.get("createdAt"),
        transactionCountExpression
      ))
      .where(predicate)
      .groupBy(
        root.get("id"),
        root.get("name"),
        root.get("createdAt")
      );

    // --- SIRALAMA (SORTING) BURADA EKLENİYOR ---
    if (pageable.getSort().isSorted()) {
        List<Order> orders = new ArrayList<>();
        pageable.getSort().forEach(sortOrder -> {
          // Eğer sort "transactionCount" alanına göre istenirse (DTO'daki isim)
          if (sortOrder.getProperty().equals("transactionCount")) {
            orders.add(sortOrder.isAscending() ? criteriaBuilder.asc(transactionCountExpression) : criteriaBuilder.desc(transactionCountExpression));
          } else {
            // Diğer root alanları için (id, name, createdAt vb.)
            orders.add(sortOrder.isAscending() ? criteriaBuilder.asc(root.get(sortOrder.getProperty())) : criteriaBuilder.desc(root.get(sortOrder.getProperty())));
          }
        });
        criteriaQuery.orderBy(orders);
    }
    // ------------------------------------------

    TypedQuery<ListUserCategoriesWithTransactionCountDto> query = entityManager.createQuery(criteriaQuery);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());

    List<ListUserCategoriesWithTransactionCountDto> content = query.getResultList();

    // for totalElements
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<Category> countRoot = countQuery.from(Category.class);
    Predicate countPredicate = specs.toPredicate(countRoot, countQuery, criteriaBuilder);

    countQuery.select(criteriaBuilder.countDistinct(countRoot.get("id")))
      .where(countPredicate);


    long totalCount = entityManager.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(content, pageable, totalCount);
  }
  
}
