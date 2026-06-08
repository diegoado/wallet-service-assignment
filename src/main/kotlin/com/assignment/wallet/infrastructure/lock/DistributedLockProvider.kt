package com.assignment.wallet.infrastructure.lock

import com.assignment.wallet.domain.model.wallet.lock.LockProvider
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DistributedLockProvider(
  private val redisLockService: RedisLockService
) : LockProvider {
  override fun <T> withLock(
    walletId: UUID,
    currency: String,
    action: () -> T
  ): T = redisLockService.executeWithLock(lockKey(walletId, currency), action)

  override fun <T> withLocks(
    walletIds: List<UUID>,
    currency: String,
    action: () -> T
  ): T {
    val keys = walletIds.sorted().map { lockKey(it, currency) }
    return redisLockService.executeWithLocks(keys, action)
  }

  private fun lockKey(
    walletId: UUID,
    currency: String
  ) = "lock:wallet:$walletId:$currency"
}
