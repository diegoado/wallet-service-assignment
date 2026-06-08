# Wallet Service - Architecture & Conventions

## Technology Stack

- Kotlin on JVM 25 with Spring Boot 4.0.6
- Gradle (Kotlin DSL) build
- PostgreSQL for persistence (ACID)
- Redis via Redisson 4.4.0 for caching and distributed locks
- Flyway for database migrations
- SpringDoc OpenAPI 3.0.3 for Swagger docs
- `error-handling-spring-boot-starter` 5.1.1 for consistent error responses
- JUnit 5 + MockK + Cucumber for testing
- Docker Compose deployment (app + postgres + redis)

## Architecture Decisions

- DDD (Domain-Driven Design) with Aggregate Root pattern
- Wallet is the Aggregate Root; Transaction is a Value Object within the Wallet aggregate
- One wallet per user, identified by document (CPF/CNPJ)
- One wallet supports multiple currencies (ISO 4217)
- Balance precision: BigDecimal scale 2, DECIMAL(19,2) in DB
- Ledger is immutable — no UPDATE/DELETE; rollbacks use compensating entries
- Current balance cached in Redis, computed lazily from ledger on miss, evicted on mutation
- Distributed lock via Redisson per wallet+currency for all mutations
- Lock ordering for transfers: acquire by wallet ID ascending to prevent deadlocks
- Idempotency scope: (wallet_id, idempotency_key, date) — keys reusable next day
- Transfers only between same-currency wallets (no FX)
- Negative balances not allowed
- Minimum transaction: 0.01; no maximum
- No authentication (out of scope)
- Redis is best-effort: on failure, balance falls back to ledger computation

## Package Structure

```
com.assignment.wallet
├── application.controller.{resource}.dto  — REST controllers + request/response DTOs per resource
├── domain.dto                             — Domain DTOs (NewDeposit, etc.)
├── domain.exception                       — Domain exceptions (each in own file)
├── domain.model.wallet                    — Aggregate root (Wallet, Builder, Factory, Repository port)
├── domain.model.wallet.balance            — Balance port (BalanceProvider, BalanceKey)
├── domain.model.wallet.lock               — Lock port (LockProvider)
├── domain.model.wallet.transaction        — Transaction VO + TransactionType enum
├── domain.service                         — Application services (WalletService)
├── domain.shared                          — Constants, Patterns, Utilities
├── infrastructure.cache                   — Redis cache (RedisCacheService, CacheableBalanceProvider)
├── infrastructure.configuration           — Spring config beans (WalletConfiguration)
├── infrastructure.lock                    — Distributed lock (DistributedLockService, DistributedLockProvider)
├── infrastructure.persistence.entity      — JPA entities (WalletDbEntity, TransactionLedgerDbEntity)
├── infrastructure.persistence.repository  — Spring Data JPA repositories
└── infrastructure.repository.impl         — Domain repository implementations (WalletRepositoryImpl)
```

## DDD Patterns Used

- **Aggregate Root**: `Wallet` — enforces invariants (balance, idempotency, document uniqueness)
- **Value Object**: `Transaction` — immutable, identity-less within wallet context
- **Domain Port (Interface)**: `WalletRepository`, `BalanceProvider`, `LockProvider`
- **Infrastructure Adapter**: `WalletRepositoryImpl`, `CacheableBalanceProvider`, `DistributedLockProvider`
- **Factory**: `WalletFactory` — creates/retrieves Wallet aggregates with injected providers
- **Facade**: `WalletProviderFacade` — bundles BalanceProvider + LockProvider for Wallet injection

## Coding Conventions

- One class per file
- `Loggable` interface with structured logging (logstash-encoder)
- `suite` as test subject name in unit tests
- Test methods start with `should`
- `@Nested` inner classes per method group in tests
- Domain layer never imports from infrastructure
- snake_case for JSON field names via @JsonProperty
- Validation patterns in `domain.shared.Patterns`
