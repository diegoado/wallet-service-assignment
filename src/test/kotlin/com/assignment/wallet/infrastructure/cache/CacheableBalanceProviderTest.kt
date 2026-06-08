package com.assignment.wallet.infrastructure.cache

import com.assignment.wallet.domain.model.wallet.balance.BalanceKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class CacheableBalanceProviderTest {
  private val walletId = UUID.randomUUID()

  private val key = BalanceKey(walletId, "BRL")

  private val cacheKey = "wallet:$walletId:balance:BRL"

  private val redisCacheService = mockk<RedisCacheService>(relaxed = true)

  private val suite = CacheableBalanceProvider(redisCacheService)

  @Nested
  inner class Get {
    @Test
    fun `should return balance from cache when present`() {
      every { redisCacheService.get<BigDecimal>(cacheKey) } returns BigDecimal("100.00")

      val result = suite.get(key)

      assertEquals(BigDecimal("100.00"), result)
    }

    @Test
    fun `should return null when cache miss`() {
      every { redisCacheService.get<BigDecimal>(cacheKey) } returns null

      val result = suite.get(key)

      assertNull(result)
    }
  }

  @Nested
  inner class Set {
    @Test
    fun `should store balance in cache with correct key`() {
      suite.set(key, BigDecimal("200.00"))

      verify { redisCacheService.set(cacheKey, BigDecimal("200.00")) }
    }
  }

  @Nested
  inner class RemoveKey {
    @Test
    fun `should evict balance from cache with correct key`() {
      suite.removeKey(key)

      verify { redisCacheService.evict(cacheKey) }
    }
  }
}
