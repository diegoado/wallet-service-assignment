package com.assignment.wallet.domain.dto

import java.math.BigDecimal

data class NewWithdrawal(
  val currency: String,
  val amount: BigDecimal,
  val idempotencyKey: String,
  val description: String? = null
)
