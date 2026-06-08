# Spec: POST /wallets — Create Wallet

## Endpoint

`POST /wallets`

## Description

Creates a new wallet for a user identified by their document (CPF/CNPJ).

## Request

### Headers

| Header       | Required | Description        |
|--------------|----------|--------------------|
| Content-Type | Yes      | `application/json` |

### Body

```json
{
  "document": "12345678901"
}
```

| Field    | Type   | Validation                                                   |
|----------|--------|--------------------------------------------------------------|
| document | String | Required. 11 digits (CPF) or 14 digits (CNPJ). Numeric only. |

## Response

### 201 Created

Headers: `Location: /wallets/{id}`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "document": "12345678901",
  "created_at": "2026-06-01T10:00:00.000Z"
}
```

### 400 Bad Request

- Invalid document format

## Behavior

1. Validate document format (regex: `^\d{11}(\d{3})?$`)
2. `WalletService.create(document)`:
   - `WalletFactory.findByDocument(document)` → if exists, return it (idempotent)
   - `WalletFactory.createNew(document)` → build new Wallet aggregate
   - `wallet.saveIfNeeded()` → checks uniqueness, persists
3. Return 201 with Location header + wallet response

## Notes

- Creation is naturally idempotent by document uniqueness — returns existing wallet if found
- No idempotency key header required
- No `WalletAlreadyExistsException` exposed to client (service returns existing wallet)
- Location header: `/wallets/{id}`
