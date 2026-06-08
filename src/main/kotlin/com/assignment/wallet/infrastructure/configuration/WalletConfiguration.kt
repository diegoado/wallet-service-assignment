package com.assignment.wallet.infrastructure.configuration

import com.assignment.wallet.domain.model.wallet.WalletProviderFacade
import com.assignment.wallet.infrastructure.cache.CacheableBalanceProvider
import com.assignment.wallet.infrastructure.lock.DistributedLockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WalletConfiguration {
  @Bean
  fun walletProvider(
    balance: CacheableBalanceProvider,
    lock: DistributedLockProvider
  ): WalletProviderFacade = WalletProviderFacade(balance, lock)
}
