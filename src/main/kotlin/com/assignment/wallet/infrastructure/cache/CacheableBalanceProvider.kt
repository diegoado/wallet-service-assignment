package com.assignment.wallet.infrastructure.cache

import com.assignment.wallet.domain.model.wallet.balance.BalanceKey
import com.assignment.wallet.domain.model.wallet.balance.BalanceProvider
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CacheableBalanceProvider(
  private val redisCacheService: RedisCacheService
) : BalanceProvider {
  override fun get(key: BalanceKey): BigDecimal? = redisCacheService.get<BigDecimal>(cacheKey(key))

  override fun set(
    key: BalanceKey,
    amount: BigDecimal
  ) {
    redisCacheService.set(cacheKey(key), amount)
  }

  override fun removeKey(key: BalanceKey) {
    redisCacheService.evict(cacheKey(key))
  }

  private fun cacheKey(key: BalanceKey) = "wallet:${key.walletId}:balance:${key.currency}"
}
