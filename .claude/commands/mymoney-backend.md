---
description: myMoney backend development - enforces N-layered architecture, coding conventions, DTO patterns, specification filters, event system, and all project standards
argument-hint: [action] [domain] - e.g. "add-endpoint transaction" or "create-service budget"
allowed-tools: Read Grep Glob Bash(mvn *) Bash(./mvnw *) Bash(git *)
---

# myMoney Backend Development Skill

You are developing on the **myMoney-backend** project — a personal finance management application built with **Java 21** and **Spring Boot 3.5.3**. The project uses an **N-layered architecture** with strict separation of concerns.

> **Architecture reference:** See .claude/skills/mymoney-dev/architecture.md
> **Code patterns:** Loaded on demand from `.claude/skills/mymoney-dev/patterns/` — each step below references the relevant pattern file

---

## Core Principles

1. **Architecture integrity is non-negotiable.** Every layer has a clear responsibility. Never bypass layers (e.g., calling a repository from a controller).
2. **Every endpoint gets its own DTO.** No entity exposure, no reusing DTOs across endpoints unless semantically identical.
3. **Enums everywhere.** Use enums for type safety on any fixed set of values. Each enum has a numeric `value` field and Turkish comments explaining its purpose.
4. **Comments matter.** Write English code comments that explain "why", not "what". A new developer must understand intent without asking.
5. **Soft delete only.** Never use hard deletes. Set `isRemoved = true` and filter with `isRemovedFalse` in queries.
6. **User isolation.** Every query must be scoped to the authenticated user's ID. No cross-user data leaks.
7. **Event-driven side effects.** Side effects (e.g., recalculating monthly summaries) must use Spring events, not direct calls in the main transaction.
8. **Code style consistency.** All generated code MUST match the existing codebase's formatting exactly. Before writing any new file or editing an existing one, read a nearby file in the same package/layer to observe and replicate: whitespace, blank lines between methods, blank lines between fields, brace placement, import ordering, annotation line breaks, indentation (spaces vs tabs), and line break patterns. Never introduce your own formatting preferences — mirror what already exists.

---

## Task: $ARGUMENTS

Analyze the request and follow the appropriate workflow below.

---

## Workflow: Adding a New Endpoint

When adding a new endpoint, create ALL required files in this exact order:

### Step 1: Define the DTOs → Read `.claude/skills/mymoney-dev/patterns/dto-mapper.md`
- **Request DTO** in `dtos/{domain}/request/`, **Response DTO** in `dtos/{domain}/response/`
- If paginated/filtered, extend `SortablePageRequest` for the filter DTO
- Add Jakarta Validation annotations with **Turkish** error messages
- Use `BigDecimal` for money, `LocalDate`/`LocalDateTime` for dates, enums for fixed types

### Step 2: Add Sort Column Enum (if paginated) → Read `.claude/skills/mymoney-dev/patterns/data.md`
- Create in `core/enums/sort/`, implement `BaseSortColumn` interface
- Map DTO column names to actual JPA entity field paths

### Step 3: Add Specification (if filtered) → Read `.claude/skills/mymoney-dev/patterns/data.md`
- Create in `core/specs/`, build dynamic `Specification<Entity>` with predicates
- Always include `userId` and `isRemoved = false` as base predicates

### Step 4: Add Repository Method (if new query needed) → Read `.claude/skills/mymoney-dev/patterns/data.md`
- Add to existing repository in `repositories/`
- Simple queries: Spring Data naming. Complex: `@Query`. Dynamic: `JpaSpecificationExecutor`

### Step 5: Add/Update Mapper → Read `.claude/skills/mymoney-dev/patterns/dto-mapper.md`
- Add conversion method in `mappers/`, use `@Mapper(componentModel = "spring")`
- Use `@Mapping` for non-trivial field paths

### Step 6: Add Business Rules (if validation needed) → Read `.claude/skills/mymoney-dev/patterns/service.md`
- Add methods in `services/rules/{Domain}Rules.java`
- Pattern: `checkUser{Entity}ExistAndGet(Long userId, Long entityId)` returns entity or throws

### Step 7: Add Factory Methods (if complex creation needed) → Read `.claude/skills/mymoney-dev/patterns/service.md`
- Add to `factories/`, encapsulate creation logic, return entities (not DTOs)

### Step 8: Implement Service → Read `.claude/skills/mymoney-dev/patterns/service.md`
- **Interface** in `services/abstracts/`, **Implementation** in `services/impl/`
- `@Transactional` on writes, publish events for side effects, return DTOs

### Step 9: Implement Controller → Read `.claude/skills/mymoney-dev/patterns/controller.md`
- **Interface** in `webapi/controllers/abstracts/`, **Impl** in `webapi/controllers/impl/`
- Extend `BaseController`, extract `userId` from `JwtAuthentication`
- Swagger annotations on interface, `@Valid` on request body

### Step 10: Add Flyway Migration (if schema change) → Read `.claude/skills/mymoney-dev/patterns/infra.md`
- Create in `resources/db/migration/V{next_version}__{description}.sql`
- Check existing migrations for next version number. Never modify existing ones.

### Step 11: Add Event + Listener (if side effects needed) → Read `.claude/skills/mymoney-dev/patterns/event.md`
- Event class in `core/events/`, Listener in `eventListeners/`
- Use `AFTER_COMMIT` phase + `REQUIRES_NEW` propagation

---

## Workflow: Modifying an Existing Feature

1. **Read the existing code first** — understand the full chain from controller to repository
2. **Check for side effects** — which events fire, which summaries get recalculated
3. **Maintain backward compatibility** of existing endpoints unless explicitly asked to break them
4. **Update DTOs** if the response shape changes — never modify entity serialization
5. **Update specifications** if filter criteria change
6. **Update mappers** if field mappings change
7. **Update tests** if they exist for the modified code

---

## Workflow: Writing Tests → Read `.claude/skills/mymoney-dev/patterns/infra.md`

- Test location: `src/test/java/com/hakandincturk/myMoney/service/`
- JUnit 5 with `@ExtendWith(MockitoExtension.class)`, mock all dependencies
- Test both success and error paths, `@DisplayName` in Turkish

---

## Workflow: Adding a New Entity

1. Create entity in `src/main/java/com/hakandincturk/models/` extending `BaseEntitiy`
2. Add JPA annotations: `@Entity`, `@Table(name = "...")`, relationships
3. Use `@Enumerated(EnumType.STRING)` for enum fields
4. Use `BigDecimal` for money, `LocalDate`/`LocalDateTime` for dates
5. Add `@JsonIgnore` on `@ManyToOne` fields to prevent circular serialization
6. Use `FetchType.LAZY` on all relationships
7. Create Flyway migration for the new table
8. Create repository extending `JpaRepository<Entity, Long>`
9. Add `JpaSpecificationExecutor<Entity>` if filtering will be needed

---

## Pre-Implementation Checklist

Before writing code, always verify:

- [ ] Read an existing file in the same package to match its formatting (whitespace, blank lines, brace style, import order)
- [ ] Which layer does this change belong to?
- [ ] Does a similar pattern already exist in the codebase? (Follow it)
- [ ] Are all DTOs specific to this endpoint?
- [ ] Is the query scoped to the authenticated user?
- [ ] Does this need a new Flyway migration?
- [ ] Are there side effects that should use events?
- [ ] Will existing endpoints or monthly summary calculations be affected?

---

## Post-Implementation Checklist

After writing code, verify:

- [ ] No entity is exposed directly in API responses
- [ ] All write operations are `@Transactional`
- [ ] Soft delete is used (never `deleteById`)
- [ ] User isolation is enforced in all queries
- [ ] Validation annotations are present on request DTOs
- [ ] MapStruct mapper handles all field conversions
- [ ] Swagger annotations are present on controller interface
- [ ] Flyway migration version number is correct and sequential
