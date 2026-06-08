package com.assignment.wallet.infrastructure.lock

import com.assignment.wallet.infrastructure.lock.exception.LockAcquisitionException
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisLockService(
  private val redisson: RedissonClient
) {
  fun <T> executeWithLock(
    key: String,
    action: () -> T
  ): T {
    val lock = redisson.getLock(key)
    if (!lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS)) {
      throw LockAcquisitionException(key)
    }
    try {
      return action()
    } finally {
      lock.unlock()
    }
  }

  fun <T> executeWithLocks(
    keys: List<String>,
    action: () -> T
  ): T {
    val locks = keys.map { redisson.getLock(it) }

    locks.forEachIndexed { index, lock ->
      if (!lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS)) {
        locks.take(index).reversed().forEach { it.unlock() }
        throw LockAcquisitionException(keys[index])
      }
    }
    try {
      return action()
    } finally {
      locks.reversed().forEach { it.unlock() }
    }
  }

  private companion object {
    const val WAIT_TIME_SECONDS = 5L

    const val LEASE_TIME_SECONDS = 10L
  }
}
