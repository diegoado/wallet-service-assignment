# Spec: Data Model (PostgreSQL + Spring JPA)

## Overview

Define the persistence layer using Spring Data JPA with Flyway migrations on PostgreSQL.

## Database Tables

### Table: `wallet`

| Column     | Type         | Constraints            |
|------------|--------------|------------------------|
| id         | UUID         | PK, generated          |
| document   | VARCHAR(14)  | NOT NULL, UNIQUE       |
| created_at | TIMESTAMP(3) | NOT NULL, default now  |

### Table: `transaction_ledger`

| Column           | Type          | Constraints                                             |
|------------------|---------------|---------------------------------------------------------|
| id               | UUID          | PK, generated                                           |
| wallet_id        | UUID          | NOT NULL, FK → wallet.id                                |
| currency         | VARCHAR(3)    | NOT NULL, ISO 4217                                      |
| amount           | DECIMAL(19,2) | NOT NULL                                                |
| type             | VARCHAR(20)   | NOT NULL (DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT) |
| correlation_id   | UUID          | NULL (populated only for transfers)                     |
| idempotency_key  | VARCHAR(255)  | NOT NULL                                                |
| idempotency_date | DATE          | NOT NULL                                                |
| description      | TEXT          | NULL                                                    |
| created_at       | TIMESTAMP(3)  | NOT NULL, default now                                   |

**Unique constraint**: `uk_ledger_idempotency (wallet_id, idempotency_key, idempotency_date)`

**Index**: `idx_ledger_wallet_currency_created (wallet_id, currency, created_at)` — for balance computation queries

## JPA Entities

### WalletDbEntity (`infrastructure.persistence.entity`)

- data class with secondary constructor
- UUID generated via `UuidCreator.getTimeOrderedEpoch()`
- All columns `updatable = false` (immutable once created)

### TransactionLedgerDbEntity (`infrastructure.persistence.entity`)

- data class with secondary constructor for field initialization
- UUID generated via `UuidCreator.getTimeOrderedEpoch()`
- All columns `updatable = false` (immutable ledger)
- `type` references `TransactionType` enum from `domain.model.wallet.transaction`

## Domain Model Mapping

- `WalletDbEntity` ↔ `Wallet` (via `WalletRepositoryImpl.toDomain()`)
- `TransactionLedgerDbEntity` ↔ `Transaction` (via `WalletRepositoryImpl.toTransaction()`)

## Flyway Migration

- Location: `src/main/resources/db/migration/`
- File: `V1__create_wallet_and_ledger.sql`

## Repositories

### WalletDbRepository (Spring Data JPA)
- `findByDocument(document): WalletDbEntity?`

### TransactionLedgerDbRepository (Spring Data JPA)
- `computeBalance(walletId, currency): BigDecimal`
- `computeBalanceAt(walletId, currency, at): BigDecimal`
- `findByWalletIdAndIdempotencyKeyAndIdempotencyDate(...): TransactionLedgerDbEntity?`

### WalletRepositoryImpl (Domain port implementation)
- Implements `WalletRepository` domain interface
- Delegates to `WalletDbRepository` and `TransactionLedgerDbRepository`
- Maps between JPA entities and domain objects

## Acceptance Criteria

- [x] Flyway migration creates both tables with constraints and index
- [x] WalletDbEntity persists with UuidCreator-generated ID
- [x] TransactionLedgerDbEntity persists with all required fields via secondary constructor
- [x] Unique constraint on (wallet_id, idempotency_key, idempotency_date) prevents duplicates
- [x] Balance computation query returns correct sum
- [x] Historical balance query filters by ZonedDateTime correctly
- [x] WalletRepositoryImpl maps entities to domain objects correctly
