# Spec: POST /wallets/{walletId}/withdrawals — Withdraw Funds

## Endpoint

`POST /wallets/{walletId}/withdrawals`

## Description

Withdraws funds from a wallet for a specific currency. Fails if insufficient balance.

## Request

### Headers

| Header          | Required | Description                |
|-----------------|----------|----------------------------|
| Content-Type    | Yes      | `application/json`         |
| Idempotency-Key | Yes      | Client-provided unique key |

### Path Parameters

| Param    | Type | Description |
|----------|------|-------------|
| walletId | UUID | Wallet ID   |

### Body

```json
{
  "currency": "BRL",
  "amount": "50.00",
  "description": "ATM withdrawal"
}
```

| Field       | Type       | Validation                             |
|-------------|------------|----------------------------------------|
| currency    | String     | Required. ISO 4217 (3 uppercase chars) |
| amount      | BigDecimal | Required. minimum 0.01                 |
| description | String     | Optional                               |

## Response

### 200 OK

```json
{
  "wallet_id": "550e8400-e29b-41d4-a716-446655440000",
  "currency": "BRL",
  "amount": "-50.00",
  "type": "WITHDRAW",
  "balance": "1551.25",
  "created_at": "2026-06-01T10:10:00.000Z"
}
```

### 400 Bad Request

- Insufficient funds
- Missing or invalid amount (< 0.01)
- Missing/invalid currency
- Missing Idempotency-Key header

### 404 Not Found

- Wallet does not exist

### 409 Conflict

- Idempotent request already processed

## Behavior

1. Map request to `NewWithdrawal` domain DTO
2. `WalletService.withdraw(walletId, newWithdrawal)`
3. `WalletFactory.requireById(walletId)` → throws 404 if not found
4. `Wallet.withdraw(newWithdrawal)`:
   - Check idempotency → throws 409 if duplicate
   - Acquire distributed lock on wallet+currency
   - Compute current balance from ledger
   - If balance < amount → throw `InsufficientFundsException` (400)
   - Create Transaction VO (type=WITHDRAW, negative amount)
   - Persist via `repository.newTransaction`
   - Evict balance cache
   - Release lock
5. Fetch updated balance
6. Return response

## Domain DTO

```kotlin
data class NewWithdrawal(
  val currency: String,
  val amount: BigDecimal,
  val idempotencyKey: String,
  val description: String? = null,
)
```

## Notes

- Amount is stored as negative in the ledger
- Balance check happens inside the lock to prevent race conditions
- No transaction ID exposed in response
