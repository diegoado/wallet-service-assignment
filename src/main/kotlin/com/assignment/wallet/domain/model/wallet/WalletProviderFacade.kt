package com.assignment.wallet.domain.model.wallet

import com.assignment.wallet.domain.model.wallet.balance.BalanceProvider
import com.assignment.wallet.domain.model.wallet.lock.LockProvider

class WalletProviderFacade(
  val balance: BalanceProvider,
  val lock: LockProvider
)
