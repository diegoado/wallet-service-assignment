package com.assignment.wallet.domain.model.wallet.balance

import java.util.UUID

data class BalanceKey(
  val walletId: UUID,
  val currency: String
)
