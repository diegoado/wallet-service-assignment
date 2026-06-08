package com.assignment.wallet.domain.dto

import java.math.BigDecimal
import java.util.UUID

data class NewTransfer(
  val sourceWalletId: UUID,
  val targetWalletId: UUID,
  val currency: String,
  val amount: BigDecimal,
  val idempotencyKey: String,
  val description: String? = null
)
