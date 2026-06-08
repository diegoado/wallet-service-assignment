package com.assignment.wallet.infrastructure.cache

import com.assignment.wallet.Loggable
import com.assignment.wallet.asKeyValue
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service

@Service
class RedisCacheService(
  private val redisson: RedissonClient
) : Loggable {
  fun <T> get(key: String): T? =
    try {
      redisson.getBucket<T>(key).get()
    } catch (e: Exception) {
      logger.error("Redis GET failed for key={}", key, asKeyValue("key", key), e)
      null
    }

  fun <T> set(
    key: String,
    value: T
  ) {
    try {
      redisson.getBucket<T>(key).set(value)
    } catch (e: Exception) {
      logger.error("Redis SET failed for key={}", key, asKeyValue("key", key), e)
    }
  }

  fun evict(key: String) {
    try {
      redisson.getBucket<Any>(key).delete()
    } catch (e: Exception) {
      logger.error("Redis DEL failed for key={}", key, asKeyValue("key", key), e)
    }
  }
}
