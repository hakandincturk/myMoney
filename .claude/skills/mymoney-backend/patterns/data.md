# Pattern: Repository, Specification, Sort Enum, Pagination

## Repository

```java
package com.hakandincturk.repositories;

public interface DomainRepository extends JpaRepository<Domain, Long>, JpaSpecificationExecutor<Domain> {

    // Simple finder — Spring Data generates query
    Optional<Domain> findByIdAndUserIdAndIsRemovedFalse(Long id, Long userId);

    // Paginated finder
    Page<Domain> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageable);

    // Custom JPQL query
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Domain d WHERE d.user.id = :userId AND d.isRemoved = false")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);
}
```

### Repository Rules
- Always extend `JpaRepository<Entity, Long>`
- Add `JpaSpecificationExecutor<Entity>` if filtering is needed
- All queries must include `isRemovedFalse` (soft delete)
- All user-specific queries must include `userId`
- Use `Optional<Entity>` for single-result, `Page<Entity>` for paginated
- Use `@Query` for complex aggregations

---

## Specification (Dynamic Filtering)

```java
package com.hakandincturk.core.specs;

public class DomainSpecification {

    public static Specification<Domain> filterDomains(Long userId, DomainFilterRequestDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always required: user isolation + soft delete
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.equal(root.get("isRemoved"), false));

            // Optional text filter (case-insensitive)
            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + filter.getName().toLowerCase() + "%"));
            }

            // Optional enum list filter
            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(filter.getTypes()));
            }

            // Optional date range
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("debtDate"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("debtDate"), filter.getEndDate()));
            }

            // Optional amount range
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinAmount()));
            }

            // Optional relationship filter (list of IDs)
            if (filter.getAccountIds() != null && !filter.getAccountIds().isEmpty()) {
                predicates.add(root.get("account").get("id").in(filter.getAccountIds()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### Specification Rules
- Always a static method returning `Specification<Entity>`
- First two predicates ALWAYS: `userId` + `isRemoved = false`
- Null/empty checks before adding optional predicates
- Text search: `cb.like(cb.lower(...))` with `%` wildcards
- Combine with `cb.and()`

---

## Sort Column Enum

```java
package com.hakandincturk.core.enums.sort;

@Getter
public enum DomainSortColumn implements BaseSortColumn {
    DOMAIN_NAME("name", "Domain Name"),
    DOMAIN_AMOUNT("totalAmount", "Amount"),
    ACCOUNT_NAME("account.name", "Account Name"),
    CREATED_DATE("createdAt", "Created Date");

    private final String entityProperty;
    private final String displayName;

    DomainSortColumn(String entityProperty, String displayName) {
        this.entityProperty = entityProperty;
        this.displayName = displayName;
    }
}
```

### Sort Enum Rules
- Implements `BaseSortColumn` interface
- Enum constant name = what frontend sends as `columnName`
- `entityProperty` = JPA entity field path for `Sort.by()`
- Used via `PaginationUtils.toPageable(pageData, DomainSortColumn.class)`

---

## Pagination Utilities Usage

```java
// Basic pagination (no sorting)
Pageable pageable = PaginationUtils.toPageable(pageRequestParams);

// With enum-based sort column mapping
Pageable pageable = PaginationUtils.toPageable(sortablePageRequest, DomainSortColumn.class);

// Convert Spring Page to custom PagedResponse
PagedResponse<T> response = PaginationUtils.toPagedResponse(page);
```
