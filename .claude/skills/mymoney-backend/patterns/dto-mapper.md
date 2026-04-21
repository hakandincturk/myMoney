# Pattern: DTO, Mapper

## Request DTO (Create/Update)

```java
package com.hakandincturk.dtos.domain.request;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDomainRequestDto {

    @NotBlank(message = "Isim alani bos birakilamaz")
    @Size(min = 2, message = "Isim en az 2 karakter olmalidir")
    private String name;

    @NotNull(message = "Tur alani bos birakilamaz")
    private DomainType type;

    @NotNull(message = "Tutar alani bos birakilamaz")
    private BigDecimal amount;

    @NotNull(message = "Tarih alani bos birakilamaz")
    private LocalDate date;

    private String description;  // Optional — no validation
}
```

## Filter/Pagination DTO

```java
package com.hakandincturk.dtos.domain.request;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DomainFilterRequestDto extends SortablePageRequest {

    private String name;
    private List<Long> accountIds;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DomainType> types;
}
```

## Response DTO

```java
package com.hakandincturk.dtos.domain.response;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListMyDomainsResponseDto {

    private Long id;
    private String name;
    private BigDecimal amount;
    private DomainType type;
    private LocalDateTime createdAt;
}
```

### DTO Rules
- Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Validation messages in **Turkish**
- `BigDecimal` for money — never `double` or `float`
- One DTO per endpoint — never reuse across endpoints with different shapes
- Package: `dtos/{domain}/request/` or `dtos/{domain}/response/`
- Filter DTOs extend `SortablePageRequest`

---

## Mapper (MapStruct)

```java
package com.hakandincturk.mappers;

@Mapper(componentModel = "spring")
public interface DomainMapper {

    // Simple mapping (same field names)
    ListMyDomainsResponseDto toListMyDomainsResponseDto(Domain domain);

    // Mapping with nested fields
    @Mapping(target = "accountName", source = "account.name")
    @Mapping(target = "contactName", source = "contact.fullName")
    DetailDomainResponseDto toDetailDomainResponseDto(Domain domain);
}
```

### Mapper Rules
- `@Mapper(componentModel = "spring")` — always Spring-managed
- `@Mapping` for non-trivial field paths (nested objects, name differences)
- `uses = {OtherMapper.class}` to reference other mappers
- Method naming: `to{DtoName}(Entity entity)`
