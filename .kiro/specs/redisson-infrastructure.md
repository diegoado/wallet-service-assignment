# Spec: Redis Infrastructure (Cache + Distributed Lock)

## Overview

Redis via Redisson 4.4.0 for two concerns:
1. **Balance caching** — current balance per (wallet, currency) as key-value
2. **Distributed locking** — prevent concurrent mutations on the same wallet+currency

## Redis Key Schema

| Purpose | Key Pattern                            | Value Type               | TTL                              |
|---------|----------------------------------------|--------------------------|----------------------------------|
| Balance | `wallet:{walletId}:balance:{currency}` | String (BigDecimal text) | No expiry (evicted on mutation)  |
| Lock    | `lock:wallet:{walletId}:{currency}`    | Managed by Redisson      | Auto-release after 10s           |

## Implementation

### RedisCacheService (`infrastructure.cache`)
Generic typed cache with `get<T>`, `set<T>`, and `evict` methods. Graceful fallback on Redis failure.

### CacheableBalanceProvider (`infrastructure.cache`)
Implements domain port `BalanceProvider`. Maps `BalanceKey(walletId, currency)` to Redis key. Operations: `get`, `set`, `removeKey`.

### RedisLockService (`infrastructure.lock`)
Generic lock service. Takes string keys, acquires/releases locks. No domain awareness — no sorting.
- `executeWithLock(key, action)` — single lock
- `executeWithLocks(keys, action)` — multiple locks in given order

### DistributedLockProvider (`infrastructure.lock`)
Implements domain port `LockProvider`. Domain-aware:
- `withLock(walletId, currency, action)` — builds key, delegates to `RedisLockService`
- `withLocks(walletIds, currency, action)` — **sorts walletIds** for deadlock prevention, builds keys, delegates

### WalletProviderFacade (`domain.model.wallet`)
Bundles `BalanceProvider` + `LockProvider` for injection into Wallet aggregate. Configured via `WalletConfiguration` bean.

## Cache Strategy

- **Read** (`Wallet.getBalance`): Check cache → on miss, compute from ledger, store in cache
- **Mutation** (`Wallet.deposit/withdraw`): Evict cache key (lazy recomputation on next read)
- **Fallback**: If Redis is unavailable during read, compute from ledger directly

## Lock Strategy

- **Single operations** (deposit/withdraw): `LockProvider.withLock(walletId, currency)`
- **Transfer**: `LockProvider.withLocks([sourceId, targetId], currency)` — sorted in `DistributedLockProvider`
- **Reentrant**: Redisson `RLock` is reentrant; transfer acquires outer locks, then wallet methods re-acquire same locks (same thread)

## Lock Parameters

| Parameter  | Value | Rationale                             |
|------------|-------|---------------------------------------|
| Wait time  | 5s    | Max time to wait for lock acquisition |
| Lease time | 10s   | Auto-release if holder crashes        |

## Acceptance Criteria

- [x] RedisCacheService get/set/evict works with generic types
- [x] CacheableBalanceProvider maps BalanceKey to correct Redis key format
- [x] On cache miss, balance is computed from ledger and stored
- [x] On Redis failure during read, returns null (graceful fallback)
- [x] RedisLockService acquires locks in given order (no sorting)
- [x] DistributedLockProvider sorts walletIds before delegating
- [x] Lock acquisition timeout throws LockAcquisitionException
- [x] Reentrant locks work for transfer flow (outer + inner)
- [x] Lock is always released (even on exception) via try/finally
