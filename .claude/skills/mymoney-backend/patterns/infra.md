# Pattern: Exception, Migration, Test, Naming

## Exception Usage

```java
throw new NotFoundException("Hesap bulunamadi");        // 404
throw new ConflictException("Bu kayit zaten mevcut");    // 409
throw new ValidationException("Gecersiz tutar");         // 400
throw new BusinessException("Islem gerceklestirilemedi"); // 500
throw new UnauthorizedException("Yetkisiz islem");       // 401
```

- All exception messages in **Turkish**
- All handled by `GlobalExceptionHandler` → returns `ApiResponse` with correct HTTP status

---

## Flyway Migration

```sql
-- V{N}__{descriptive_name}.sql

-- New table template
CREATE TABLE new_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 2) DEFAULT 0,
    is_removed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- File naming: `V{N}__{description}.sql` (two underscores)
- Sequential version numbers — check existing migrations first
- PostgreSQL syntax
- Always include `is_removed` + `created_at` on new tables
- `BIGSERIAL` for IDs, `NUMERIC(19, 2)` for money
- Never modify existing migration files

---

## Test

```java
package com.hakandincturk.myMoney.service;

@ExtendWith(MockitoExtension.class)
class DomainServiceTest {

    @InjectMocks
    private DomainServiceImpl domainService;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private DomainMapper domainMapper;

    @Mock
    private DomainRules domainRules;

    @Test
    @DisplayName("Basarili domain olusturma")
    void createDomain_shouldCreateSuccessfully() {
        Long userId = 1L;
        CreateDomainRequestDto body = new CreateDomainRequestDto();
        body.setName("Test");

        Users user = new Users();
        user.setId(userId);

        when(userRules.checkUserExistAndGet(userId)).thenReturn(user);

        domainService.createDomain(userId, body);

        verify(domainRepository).save(any(Domain.class));
    }

    @Test
    @DisplayName("Bulunamayan domain icin NotFoundException firlatmali")
    void deleteDomain_shouldThrowNotFoundException() {
        Long userId = 1L;
        Long domainId = 99L;

        when(domainRules.checkUserDomainExistAndGet(userId, domainId))
                .thenThrow(new NotFoundException("Kayit bulunamadi"));

        assertThrows(NotFoundException.class,
                () -> domainService.deleteDomain(userId, domainId));
    }
}
```

- `@ExtendWith(MockitoExtension.class)` — no Spring context
- `@DisplayName` in **Turkish**
- Test both success and failure paths
- Package: `com.hakandincturk.myMoney.service`

---

## File Naming Conventions

| Type                  | Location                               | Pattern                              |
|:----------------------|:---------------------------------------|:-------------------------------------|
| Controller Interface  | `webapi/controllers/abstracts/`        | `{Domain}Controller.java`            |
| Controller Impl       | `webapi/controllers/impl/`            | `{Domain}ControllerImpl.java`        |
| Service Interface     | `services/abstracts/`                  | `{Domain}Service.java`               |
| Service Impl          | `services/impl/`                       | `{Domain}ServiceImpl.java`           |
| Business Rules        | `services/rules/`                      | `{Domain}Rules.java`                 |
| Repository            | `repositories/`                        | `{Domain}Repository.java`            |
| Entity                | `models/`                              | `{Domain}.java`                      |
| Request DTO           | `dtos/{domain}/request/`              | `{Action}{Domain}RequestDto.java`    |
| Response DTO          | `dtos/{domain}/response/`             | `{Action}{Domain}ResponseDto.java`   |
| Filter DTO            | `dtos/{domain}/`                       | `{Domain}FilterRequestDto.java`      |
| Mapper                | `mappers/`                             | `{Domain}Mapper.java`                |
| Factory               | `factories/`                           | `{Domain}Factory.java`               |
| Specification         | `core/specs/`                          | `{Domain}Specification.java`         |
| Sort Enum             | `core/enums/sort/`                     | `{Domain}SortColumn.java`            |
| Event                 | `core/events/`                         | `{Domain}{Action}Event.java`         |
| Event Listener        | `eventListeners/`                      | `{Domain}EventListener.java`         |
| Flyway Migration      | `resources/db/migration/`              | `V{N}__{description}.sql`            |
| Test                  | `test/.../myMoney/service/`            | `{Domain}ServiceTest.java`           |
