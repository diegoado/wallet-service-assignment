package com.assignment.wallet.domain.model.wallet.balance

import java.math.BigDecimal

interface BalanceProvider {
  fun get(key: BalanceKey): BigDecimal?

  fun set(
    key: BalanceKey,
    amount: BigDecimal
  )

  fun removeKey(key: BalanceKey)
}
