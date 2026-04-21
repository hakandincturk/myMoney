# Pattern: Service, Rules, Factory

## Service Interface

```java
package com.hakandincturk.services.abstracts;

public interface DomainService {
    void createDomain(Long userId, CreateDomainRequestDto body);
    Page<ListMyDomainsResponseDto> listMyActiveDomains(Long userId, DomainFilterRequestDto pageData);
    void updateDomain(Long userId, Long domainId, UpdateDomainRequestDto body);
    void deleteDomain(Long userId, Long domainId);
}
```

## Service Implementation

```java
package com.hakandincturk.services.impl;

@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;
    private final DomainRules domainRules;
    private final UserRules userRules;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void createDomain(Long userId, CreateDomainRequestDto body) {
        Users user = userRules.checkUserExistAndGet(userId);
        // Business logic here
        domainRepository.save(entity);
        // Publish event if side effects needed
        eventPublisher.publishEvent(new DomainCreatedEvent(entity));
    }

    @Override
    public Page<ListMyDomainsResponseDto> listMyActiveDomains(Long userId, DomainFilterRequestDto pageData) {
        Pageable pageable = PaginationUtils.toPageable(pageData, DomainSortColumn.class);
        Specification<Domain> spec = DomainSpecification.filterDomains(userId, pageData);
        return domainRepository.findAll(spec, pageable)
                .map(domainMapper::toListMyDomainsResponseDto);
    }

    @Override
    @Transactional
    public void deleteDomain(Long userId, Long domainId) {
        Domain domain = domainRules.checkUserDomainExistAndGet(userId, domainId);
        domain.setRemoved(true);  // Soft delete
        domainRepository.save(domain);
    }
}
```

### Service Rules
- `@RequiredArgsConstructor` for dependency injection (no `@Autowired`)
- `@Transactional` on all write methods
- Read methods do NOT need `@Transactional`
- Validate with Rules classes before any mutation
- Map entity → DTO before returning
- Publish events for cross-domain side effects

---

## Business Rules

```java
package com.hakandincturk.services.rules;

@Component
@RequiredArgsConstructor
public class DomainRules {

    private final DomainRepository domainRepository;

    // Returns entity if found, throws if not
    public Domain checkUserDomainExistAndGet(Long userId, Long domainId) {
        Optional<Domain> domain = domainRepository.findByIdAndUserIdAndIsRemovedFalse(domainId, userId);
        if (domain.isEmpty()) {
            throw new NotFoundException("Kayit bulunamadi");
        }
        return domain.get();
    }

    // Void check — throws on violation
    public void checkDomainNameNotDuplicate(Long userId, String name) {
        boolean exists = domainRepository.existsByUserIdAndNameAndIsRemovedFalse(userId, name);
        if (exists) {
            throw new ConflictException("Bu isimde kayit zaten mevcut");
        }
    }
}
```

### Rules Conventions
- Method naming: `check{Condition}` or `checkUser{Entity}ExistAndGet`
- Return entity when the caller needs it (avoids duplicate queries)
- Throw: `NotFoundException`, `ConflictException`, `ValidationException`
- Error messages in **Turkish**
- One Rules class per domain entity

---

## Factory

```java
package com.hakandincturk.factories;

@Component
@RequiredArgsConstructor
public class DomainFactory {

    // Complex creation with business logic
    public Domain createDomain(CreateDomainRequestDto body, Users user, Account account) {
        Domain domain = new Domain();
        domain.setName(body.getName());
        domain.setUser(user);
        domain.setAccount(account);
        domain.setType(body.getType());
        domain.setTotalAmount(body.getTotalAmount());
        domain.setStatus(TransactionStatuses.PENDING);
        domain.setPaidAmount(BigDecimal.ZERO);
        return domain;
    }
}
```

### Factory Conventions
- `@Component` — Spring-managed
- Encapsulates complex creation logic that doesn't belong in services
- Returns entities (not DTOs) — the service decides when to save
