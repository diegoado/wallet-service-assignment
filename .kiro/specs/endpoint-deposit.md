# Spec: POST /wallets/{walletId}/deposits — Deposit Funds

## Endpoint

`POST /wallets/{walletId}/deposits`

## Description

Deposits funds into a wallet for a specific currency.

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
  "amount": "100.50",
  "description": "Monthly salary"
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
  "amount": "100.50",
  "type": "DEPOSIT",
  "balance": "1601.25",
  "created_at": "2026-06-01T10:05:00.000Z"
}
```

### 400 Bad Request

- Missing or invalid amount (< 0.01)
- Missing/invalid currency
- Missing Idempotency-Key header

### 404 Not Found

- Wallet does not exist

### 409 Conflict

- Idempotent request already processed

## Behavior

1. Map request to `NewDeposit` domain DTO
2. `WalletService.deposit(walletId, newDeposit)`
3. `WalletFactory.requireById(walletId)` → throws 404 if not found
4. `Wallet.deposit(newDeposit)`:
   - Check idempotency (wallet_id, key, today) → throws 409 if duplicate
   - Acquire distributed lock on wallet+currency
   - Create Transaction VO (type=DEPOSIT, positive amount)
   - Persist via `repository.newTransaction`
   - Evict balance cache
   - Release lock
5. Fetch updated balance via `walletService.getBalance`
6. Return response with transaction details + new balance

## Domain DTO

```kotlin
data class NewDeposit(
  val currency: String,
  val amount: BigDecimal,
  val idempotencyKey: String,
  val description: String? = null,
)
```

## Notes

- No transaction ID exposed in response
- Balance cache is evicted (not recomputed) — lazy recomputation on next read
- Lock is inside the Wallet aggregate, enforced by domain `LockProvider` port
