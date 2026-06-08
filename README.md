# Wallet Service

A mission-critical wallet service that manages users' money, supporting deposit, withdraw, and transfer operations with full audit traceability.

## Technology Stack

| Component      | Technology                         |
|----------------|------------------------------------|
| Language       | Kotlin (JVM 25)                    |
| Framework      | Spring Boot 4.0.6                  |
| Build          | Gradle (Kotlin DSL)                |
| Database       | PostgreSQL 16 (ACID)               |
| Cache/Lock     | Redis 7 via Redisson 4.4.0         |
| Migrations     | Flyway                             |
| API Docs       | SpringDoc OpenAPI 3.0.3            |
| Error Handling | error-handling-spring-boot-starter |
| Testing        | JUnit 5 + MockK + Cucumber         |
| Deployment     | Docker Compose                     |

## Project Architecture

```
com.assignment.wallet
├── application.controller.{resource}.dto  — REST controllers + DTOs per resource
├── domain.dto                             — Domain input DTOs (NewDeposit, NewWithdrawal, NewTransfer)
├── domain.exception                       — Domain exceptions
├── domain.model.wallet                    — Aggregate root + ports
├── domain.model.wallet.balance            — Balance cache port
├── domain.model.wallet.lock               — Distributed lock port
├── domain.model.wallet.transaction        — Transaction VO + enum
├── domain.service                         — Domain services (WalletService, TransferService)
├── domain.shared                          — Constants, Patterns, Utilities
├── infrastructure.cache                   — Redis implementations
├── infrastructure.configuration           — Spring beans
├── infrastructure.lock                    — Redisson lock implementations
├── infrastructure.persistence.entity      — JPA entities
├── infrastructure.persistence.repository  — Spring Data JPA repositories
└── infrastructure.repository.impl         — Domain repository implementations
```

## Key Concepts

- **Immutable Ledger** — every operation creates a transaction entry that is never updated or deleted
- **Lazy Balance** — current balance is cached in Redis; evicted on mutation, recomputed from ledger on next read
- **Distributed Lock** — Redisson reentrant lock per wallet+currency prevents double-spending
- **Lock Ordering** — transfers sort wallet IDs before acquiring locks to prevent deadlocks
- **Idempotency** — scoped to `(wallet_id, idempotency_key, date)`; same key reusable next day
- **No FX** — transfers only between wallets in the same currency

## DDD Model

```
┌─────────────────────────────────────────────┐
│              Wallet (Aggregate Root)        │
│                                             │
│  - saveIfNeeded()                           │
│  - getBalance(currency)                     │
│  - getHistoricalBalance(currency, at)       │
│  - deposit(NewDeposit, correlationId?)      │
│  - withdraw(NewWithdrawal, correlationId?)  │
│                                             │
│  Contains: Transaction (Value Object)       │
│  Uses: WalletRepository (Port)              │
│  Uses: WalletProviderFacade                 │
│         ├── BalanceProvider (Port)          │
│         └── LockProvider (Port)             │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│         TransferService (Domain Service)    │
│                                             │
│  Coordinates two Wallet aggregates:         │
│  - Acquires locks on both (sorted order)    │
│  - Delegates withdraw to source wallet      │
│  - Delegates deposit to target wallet       │
│  - Linked by correlationId                  │
└─────────────────────────────────────────────┘
```

**Patterns Used:**
- Aggregate Root (`Wallet`)
- Value Object (`Transaction`)
- Domain Port/Adapter (`WalletRepository`, `BalanceProvider`, `LockProvider`)
- Factory (`WalletFactory`)
- Facade (`WalletProviderFacade`)
- Domain Service (`TransferService`)

## Useful Commands

```bash
make help              # Show all available commands
make local-up          # Start postgres + redis
make local-down        # Stop local infrastructure
make format            # Format code with ktlint
make lint              # Run ktlint check
make test              # Run unit tests
make coverage          # Tests + coverage report
make coverage-check    # Verify coverage meets minimum thresholds
make mutation-test     # Pitest mutation testing
make integration-test  # Cucumber integration tests
make test-all          # Unit + integration tests
make ci                # Full CI pipeline (lint + test + coverage)
make otel-up           # Start OpenTelemetry stack
make otel-down         # Stop OpenTelemetry stack
```

**Run locally:**
```bash
make local-up
```

## Swagger

Once the application is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI spec**: http://localhost:8080/api-docs

## API Endpoints

| Method | Path                                | Description              |
|--------|-------------------------------------|--------------------------|
| POST   | `/wallets`                          | Create a wallet          |
| GET    | `/wallets/{id}/balances`            | Get current balance      |
| GET    | `/wallets/{id}/balances/history`    | Get historical balance   |
| POST   | `/wallets/{walletId}/deposits`      | Deposit funds            |
| POST   | `/wallets/{walletId}/withdrawals`   | Withdraw funds           |
| POST   | `/transfers`                        | Transfer between wallets |

## Specs

Detailed specifications for each component:

- [Data Model (PostgreSQL + JPA)](.kiro/specs/data-model.md)
- [Redis Infrastructure (Cache + Lock)](.kiro/specs/redisson-infrastructure.md)
- [POST /wallets — Create Wallet](.kiro/specs/endpoint-create-wallet.md)
- [GET /wallets/{id}/balances — Get Balance](.kiro/specs/endpoint-get-balance.md)
- [GET /wallets/{id}/balances/history — Historical Balance](.kiro/specs/endpoint-historical-balance.md)
- [POST /wallets/{walletId}/deposits — Deposit](.kiro/specs/endpoint-deposit.md)
- [POST /wallets/{walletId}/withdrawals — Withdraw](.kiro/specs/endpoint-withdraw.md)
- [POST /transfers — Transfer](.kiro/specs/endpoint-transfer.md)

## Additional Tips

### Balance Checkpoints

As a wallet accumulates thousands of transactions, computing the balance by scanning the entire ledger (`SUM(amount)`) becomes expensive. A **checkpoint** mechanism solves this:

**Concept:** Periodically persist a snapshot of the balance at a known point in time, then only sum transactions created *after* that checkpoint.

```sql
CREATE TABLE balance_checkpoint (
    wallet_id  UUID        NOT NULL,
    currency   VARCHAR(3)  NOT NULL,
    balance    DECIMAL(19,2) NOT NULL,
    checked_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (wallet_id, currency)
);
```

**Query with checkpoint:**
```sql
SELECT cp.balance + COALESCE(SUM(t.amount), 0)
FROM balance_checkpoint cp
LEFT JOIN transaction_ledger t
  ON t.wallet_id = cp.wallet_id
  AND t.currency = cp.currency
  AND t.created_at > cp.checked_at
WHERE cp.wallet_id = :walletId AND cp.currency = :currency
```

**When to create checkpoints:**
- Scheduled job (e.g., nightly) that computes full balance and persists the checkpoint
- After N transactions since the last checkpoint (e.g., every 1000 operations)
- On cache eviction as a background task

**Benefits:**
- Balance computation scans only recent transactions instead of the full history
- Historical balance queries can also leverage the nearest checkpoint before the target timestamp
- No impact on the immutability guarantee — checkpoints are derived data, not source of truth
