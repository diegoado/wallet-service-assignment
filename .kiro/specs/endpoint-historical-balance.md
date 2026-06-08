# Spec: GET /wallets/{id}/balances/history — Historical Balance

## Endpoint

`GET /wallets/{id}/balances/history?currency={currency}&at={timestamp}`

## Description

Returns the balance of a wallet at a specific point in time.

## Request

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| id    | UUID | Wallet ID   |

### Query Parameters

| Param    | Type   | Required | Validation                                                                     |
|----------|--------|----------|--------------------------------------------------------------------------------|
| currency | String | Yes      | @Pattern `^[A-Z]{3}$` (ISO 4217)                                               |
| at       | String | Yes      | @Pattern matching `Constants.DATETIME_FORMAT` (`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`) |

## Response

### 200 OK

```json
{
  "wallet_id": "550e8400-e29b-41d4-a716-446655440000",
  "currency": "BRL",
  "balance": "1200.00",
  "at": "2026-01-15T10:30:00.000Z"
}
```

### 400 Bad Request

- Invalid timestamp format
- Invalid currency format

### 404 Not Found

- Wallet does not exist

## Behavior

1. Validate currency and timestamp format (@Pattern)
2. Parse `at` string to `ZonedDateTime` using `Constants.DATETIME_FORMAT`
3. `WalletService.getHistoricalBalance(id, currency, zonedDateTime)`
4. `WalletFactory.requireById(id)` → throws 404 if not found
5. `Wallet.getHistoricalBalance(currency, at)` → delegates to `repository.computeBalanceAt`
6. Return 200 with historical balance response

## Notes

- Millisecond precision on the timestamp
- Always computed from ledger (not cached) — historical queries are inherently point-in-time
- If no transactions exist before the timestamp, balance is `0.00`
