# Spec: GET /wallets/{id}/balances — Retrieve Balance

## Endpoint

`GET /wallets/{id}/balances?currency={currency}`

## Description

Returns the current balance for a wallet in a specific currency.

## Request

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| id    | UUID | Wallet ID   |

### Query Parameters

| Param    | Type   | Required | Validation                       |
|----------|--------|----------|----------------------------------|
| currency | String | Yes      | @Pattern `^[A-Z]{3}$` (ISO 4217) |

## Response

### 200 OK

```json
{
  "wallet_id": "550e8400-e29b-41d4-a716-446655440000",
  "currency": "BRL",
  "balance": "1500.75"
}
```

### 404 Not Found

- Wallet does not exist

## Behavior

1. Validate currency format (@Pattern)
2. `WalletService.getBalance(id, currency)`
3. `WalletFactory.requireById(id)` → throws 404 if not found
4. `Wallet.getBalance(currency)`:
   - Check cache (`BalanceProvider.get`) → return on hit
   - On miss: compute from ledger (`repository.computeBalance`)
   - Store in cache (`BalanceProvider.set`)
   - Return balance
5. Return 200 with balance response

## Notes

- If no transactions exist for the currency, balance is `0.00`
- Redis failure is transparent to client: falls back to ledger computation
