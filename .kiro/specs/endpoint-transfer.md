# Spec: POST /transfers — Transfer Funds

## Endpoint

`POST /transfers`

## Description

Transfers funds between two wallets. Both must use the same currency. Strictly atomic.

## Request

### Headers

| Header          | Required | Description                |
|-----------------|----------|----------------------------|
| Content-Type    | Yes      | `application/json`         |
| Idempotency-Key | Yes      | Client-provided unique key |

### Body

```json
{
  "source_wallet_id": "550e8400-e29b-41d4-a716-446655440000",
  "target_wallet_id": "550e8400-e29b-41d4-a716-446655440099",
  "currency": "BRL",
  "amount": "200.00",
  "description": "Payment for services"
}
```

| Field            | Type       | Validation                             |
|------------------|------------|----------------------------------------|
| source_wallet_id | UUID       | Required. Must exist                   |
| target_wallet_id | UUID       | Required. Must exist. ≠ source         |
| currency         | String     | Required. ISO 4217 (3 uppercase chars) |
| amount           | BigDecimal | Required. minimum 0.01                 |
| description      | String     | Optional                               |

## Response

### 200 OK

```json
{
  "correlation_id": "880e8400-e29b-41d4-a716-446655440003",
  "source_transaction": {
    "wallet_id": "550e8400-e29b-41d4-a716-446655440000",
    "currency": "BRL",
    "amount": "-200.00",
    "type": "WITHDRAW",
    "balance": "1351.25",
    "created_at": "2026-06-01T10:15:00.000Z"
  },
  "target_transaction": {
    "wallet_id": "550e8400-e29b-41d4-a716-446655440099",
    "currency": "BRL",
    "amount": "200.00",
    "type": "DEPOSIT",
    "balance": "200.00",
    "created_at": "2026-06-01T10:15:00.000Z"
  },
  "created_at": "2026-06-01T10:15:00.000Z"
}
```

### 400 Bad Request

- Insufficient funds in source wallet
- Source and target are the same wallet
- Missing or invalid amount
- Missing Idempotency-Key header

### 404 Not Found

- Source or target wallet does not exist

### 409 Conflict

- Idempotent request already processed

## Behavior

1. Map request to `NewTransfer` domain DTO
2. Validate source ≠ target → throw `SameWalletTransferException` (400)
3. `WalletFactory.requireById` for both wallets → throws 404 if not found
4. Generate correlation ID
5. Acquire distributed locks on both wallets (sorted by walletId — deadlock prevention)
6. Delegate to `sourceWallet.withdraw(NewWithdrawal, correlationId)`:
   - Idempotency check, balance check, persist, evict cache
7. Delegate to `targetWallet.deposit(NewDeposit, correlationId)`:
   - Idempotency check, persist, evict cache
8. Release locks
9. Fetch updated balances
10. Return response with correlation ID and both transactions

## Domain DTO

```kotlin
data class NewTransfer(
  val sourceWalletId: UUID,
  val targetWalletId: UUID,
  val currency: String,
  val amount: BigDecimal,
  val idempotencyKey: String,
  val description: String? = null,
)
```

## Design Decisions

- **No TRANSFER_IN/TRANSFER_OUT types** — transfer uses WITHDRAW + DEPOSIT, linked by correlationId
- **Reentrant locks** — `TransferService.withLocks` acquires outer locks; `wallet.withdraw`/`deposit` re-acquire same locks (reentrant via Redisson RLock)
- **Delegation** — transfer delegates business logic to Wallet aggregate methods (idempotency, balance check, persistence, cache eviction)
- **Atomicity** — both transactions in a single DB transaction; if either fails, both roll back
- **Lock ordering** — sorting happens in `DistributedLockProvider`, not in the generic `RedisLockService`

## Idempotency

- Scoped to source wallet: `(source_wallet_id, idempotency_key, current_date)`

## Notes

- No transaction IDs exposed in response
- Transfer is a domain service (`TransferService`) coordinating two Wallet aggregates
