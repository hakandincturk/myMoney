# myMoney Backend — Project Instructions

## Overview
Personal finance management REST API built with Java 21, Spring Boot 3.5.3, PostgreSQL, and JWT authentication.

## Quick Start
```bash
./mvnw spring-boot:run          # Start the application
./mvnw clean verify              # Build + run tests
./mvnw test                      # Run tests only
```

## Architecture
This project uses N-layered architecture. For development, use the `/mymoney-dev` skill which enforces all patterns and conventions automatically.

## Key Rules
- **Language:** Code and comments in English, error messages in Turkish
- **Responses:** Always in Turkish (as per global CLAUDE.md)
- **Architecture:** Never bypass layers — controller → service → repository
- **DTOs:** Every endpoint gets its own request/response DTOs. Never expose entities.
- **Soft delete:** Use `isRemoved = true`, never hard delete
- **User isolation:** All queries must be scoped to the authenticated user's ID
- **Transactions:** `@Transactional` on all write operations
- **Events:** Use Spring events for side effects (monthly summary recalculation)
- **Migrations:** Flyway only. Never modify existing migrations. Always create new ones.
- **Enums:** Use enums with numeric values and Turkish/English comments for all fixed value sets
- **Money:** Always `BigDecimal`, never `double` or `float`
- **MapStruct:** All entity-to-DTO conversions via MapStruct mappers

## Development Skill
Use `/mymoney-dev [action] [domain]` for guided development. Examples:
- `/mymoney-dev add-endpoint transaction` — Add a new transaction endpoint
- `/mymoney-dev create-service budget` — Create a new budget service
- `/mymoney-dev add-entity recurring-transaction` — Add a new entity with full stack
