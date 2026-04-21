# myMoney Backend — Architecture Reference

## Technology Stack

| Component        | Technology             | Version   |
|:-----------------|:-----------------------|:----------|
| Language         | Java                   | 21        |
| Framework        | Spring Boot            | 3.5.3     |
| Database         | PostgreSQL             | Latest    |
| ORM              | Hibernate / JPA        | (Boot)    |
| Mapping          | MapStruct              | 1.6.3     |
| Annotations      | Lombok                 | 1.18.34   |
| Auth             | JWT (JJWT)             | 0.11.5    |
| Migrations       | Flyway                 | (Boot)    |
| API Docs         | SpringDoc OpenAPI      | 2.8.9     |
| Testing          | JUnit 5, Mockito       | (Boot)    |

---

## Package Structure

```
com.hakandincturk
├── starter/                          # @SpringBootApplication entry point
│
├── webapi/controllers/
│   ├── abstracts/                    # Controller interfaces (@Tag, @Operation, mappings)
│   ├── impl/                         # Controller implementations (extend BaseController)
│   └── BaseController.java           # success(), successPaged(), error() helpers
│
├── services/
│   ├── abstracts/                    # Service interfaces (contracts)
│   ├── impl/                         # Service implementations (@Service, @Transactional)
│   └── rules/                        # Business rule validators (check + throw pattern)
│
├── repositories/
│   ├── {Domain}Repository.java       # JpaRepository + JpaSpecificationExecutor
│   └── custom/                       # Custom repository interfaces + implementations
│
├── models/
│   ├── BaseEntitiy.java              # @MappedSuperclass: id, isRemoved, createdAt
│   ├── Users.java                    # Implements UserDetails
│   ├── Account.java                  # CREDIT_CARD, CASH, BANK
│   ├── Transaction.java              # DEBT, CREDIT, PAYMENT, COLLECTION
│   ├── TransactionCategory.java      # Many-to-Many join entity (Transaction ↔ Category)
│   ├── Category.java                 # User-defined categories
│   ├── Installment.java              # Payment installments per transaction
│   ├── Contact.java                  # Creditors / debtors
│   └── MonthlySummary.java           # Pre-calculated monthly aggregations
│
├── dtos/
│   └── {domain}/
│       ├── request/                  # Endpoint-specific request DTOs
│       └── response/                 # Endpoint-specific response DTOs
│
├── mappers/                          # MapStruct interfaces (@Mapper componentModel="spring")
├── factories/                        # Complex object creation (balance calc, installment gen)
│
├── core/
│   ├── config/                       # CorsConfig, SwaggerConfig
│   ├── enums/                        # Domain enums (AccountTypes, TransactionTypes, etc.)
│   │   └── sort/                     # Sort column enums implementing BaseSortColumn
│   ├── events/                       # Event record classes (TransactionCreatedEvent, etc.)
│   ├── exception/                    # Custom exceptions (Business, NotFound, Conflict, etc.)
│   ├── handler/                      # GlobalExceptionHandler, AuthEntryPoint
│   ├── payload/                      # ApiResponse<T>, PagedResponse<T>
│   └── specs/                        # JPA Specification builders for dynamic filtering
│
├── security/
│   ├── config/                       # SecurityConfig, AppConfig (BCrypt, AuthProvider)
│   ├── services/                     # JwtService (token generation/validation)
│   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter for Bearer tokens
│   └── JwtAuthentication.java        # Custom Authentication token with userId
│
├── eventListeners/                   # @TransactionalEventListener handlers
├── factories/                        # AccountFactory, TransactionFactory, etc.
├── jobs/                             # @Scheduled tasks (MonthlySummeryScheduler)
└── utils/                            # PaginationUtils (Pageable conversion, PagedResponse)
```

---

## Layer Interaction Flow

```
HTTP Request (JSON)
       │
       ▼
┌─────────────────────────────────────┐
│  CONTROLLER (webapi/controllers/)   │  ← Extracts userId from JwtAuthentication
│  - Routes request to service        │  ← @Valid on @RequestBody
│  - Wraps response in ApiResponse    │  ← Returns via success()/successPaged()
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  SERVICE (services/impl/)           │  ← Business orchestration
│  - Calls Rules for validation       │  ← @Transactional on writes
│  - Calls Factory for creation       │  ← Publishes events for side effects
│  - Calls Repository for data        │
│  - Calls Mapper for DTO conversion  │
└──────┬──────┬──────┬──────┬─────────┘
       │      │      │      │
       ▼      ▼      ▼      ▼
   ┌──────┐ ┌────┐ ┌────┐ ┌──────┐
   │Rules │ │Repo│ │Map │ │Fctry │
   └──────┘ └────┘ └────┘ └──────┘
       │      │
       │      ▼
       │  ┌──────────────────────┐
       │  │  ENTITY (models/)    │  ← JPA entities with soft delete
       │  └──────────────────────┘
       │
       ▼
   Throws NotFoundException / ValidationException
       │
       ▼
   GlobalExceptionHandler → ApiResponse (error)
```

### Event-Driven Side Effects

```
Service action (@Transactional)
       │
       ▼
ApplicationEventPublisher.publishEvent(event)
       │
       ▼  (AFTER_COMMIT)
@TransactionalEventListener (REQUIRES_NEW)
       │
       ▼
RecalculateMonthlySummaryService
       │
       ▼
Deletes old summaries → publishes InstallmentPaidEvent
       │
       ▼  (AFTER_COMMIT)
MonthlySummaryEventListener (REQUIRES_NEW)
       │
       ▼
Recalculates and saves new MonthlySummary
```

---

## Entity Relationship Diagram

```
Users ─────────────┬──── 1:N ────→ Account
(id, fullName,     │                (id, name, type, currency,
 email, password,  │                 totalBalance, balance)
 phone)            │
                   ├──── 1:N ────→ Category
                   │                (id, name)
                   │                      │
                   │                      └──── 1:N ──→ TransactionCategory
                   │                                    (id, transaction, category)
                   │                                          │
                   ├──── 1:N ────→ Transaction ──── 1:N ─────┘
                   │                (id, name, type, status,
                   │                 totalAmount, paidAmount,
                   │                 totalInstallment, description,
                   │                 debtDate, account, contact)
                   │                      │
                   │                      └──── 1:N ──→ Installment
                   │                                    (id, installmentNumber,
                   │                                     amount, isPaid, paidDate,
                   │                                     debtDate, descripton)
                   │
                   ├──── 1:N ────→ Contact
                   │                (id, fullName, note)
                   │
                   └──── 1:N ────→ MonthlySummary
                                   (id, year, month, type,
                                    totalIncome, totalExpense,
                                    totalWaitingIncome,
                                    totalWaitingExpense,
                                    summaryDate)
```

**All entities inherit from `BaseEntitiy`:** `id` (Long, auto-increment), `isRemoved` (boolean), `createdAt` (LocalDateTime)

---

## Authentication & Security

- **Mechanism:** JWT Bearer tokens (HS256, 24h expiry)
- **User ID extraction pattern in controllers:**
  ```java
  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  if (auth instanceof JwtAuthentication jwtAuth) {
      Long userId = jwtAuth.getUserId();
  }
  ```
- **Public endpoints:** `/auth/login`, `/auth/register`, Swagger UI
- **Session policy:** STATELESS — no server-side sessions
- **Password encoding:** BCrypt
- **Future:** Role and permission architecture planned (not yet implemented)

---

## API Response Structure

### Success
```json
{
  "type": true,
  "message": "Islem basarili",
  "timestamp": "2026-04-20T10:30:00",
  "data": { ... }
}
```

### Paginated Success
```json
{
  "type": true,
  "message": "Veriler getirildi",
  "timestamp": "2026-04-20T10:30:00",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

### Error
```json
{
  "type": false,
  "message": "Hesap bulunamadi",
  "timestamp": "2026-04-20T10:30:00",
  "data": null
}
```

---

## Domain Enums

All enums are in `src/main/java/com/hakandincturk/core/enums/`. Read them directly from code when needed:
- `AccountTypes` — CREDIT_CARD, CASH, BANK
- `TransactionTypes` — DEBT, CREDIT, PAYMENT, COLLECTION
- `TransactionStatuses` — PENDING, PARTIAL, PAID
- `CurrencyTypes` — TL, USD, EUR
- `MonthlySummeryTypes` — TRANSACTION, PAYMENT
- `DashboardCategorySummaryTypes` — MONTHLY, YEARLY
- `DashboardCategorySummarySumMode` — DOUBLE_COUNT, DISTRIBUTED
- Sort enums in `core/enums/sort/` implementing `BaseSortColumn`

---

## Business Logic: Balance Calculation

### On Transaction Create (DEBT type only)
```
if (CREDIT_CARD account && DEBT transaction):
    account.balance -= totalAmount
```

### On Installment Payment
```
CREDIT_CARD account:
    DEBT/CREDIT    → balance += amount  (paying off debt)
    PAYMENT/COLLECTION → balance -= amount  (spending)

BANK/CASH account:
    DEBT/PAYMENT      → balance -= amount  (paying out)
    CREDIT/COLLECTION → balance += amount  (receiving)
```

### Transaction Status Update
```
After installment payment:
    if (totalAmount == paidAmount) → PAID
    else → PARTIAL
```

---

## Business Logic: Installment Generation

### Equal Sharing Mode (default)
```
amountPerInstallment = totalAmount / totalInstallments
firstInstallmentDate = debtDate
nthInstallmentDate = debtDate + (n-1) months
```

### Fixed Amount Mode
```
amountPerInstallment = totalAmount (as provided)
transactionTotalAmount = totalAmount * totalInstallments
```

Rounding: `HALF_UP`, 2 decimal places

---

## Business Logic: Monthly Summary

Two calculation types run independently:

### TRANSACTION Type (by debt date)
- Collects all installments with `debtDate` in the target month
- Income = CREDIT + COLLECTION types
- Expense = DEBT + PAYMENT types
- Separates paid vs waiting (unpaid)
- Also includes next month's PAYMENT/CREDIT for waiting income/expense

### PAYMENT Type (by paid date)
- Collects only paid installments with `paidDate` in the target month
- Same income/expense classification

### Recalculation Triggers
- **Transaction created** → deletes + recalculates summaries for all affected months
- **Installment paid** → deletes + recalculates summaries for debt month + paid month
- All via event system (AFTER_COMMIT, REQUIRES_NEW)
