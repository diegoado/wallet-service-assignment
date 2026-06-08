package com.assignment.wallet.domain.model.wallet.transaction

import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class Transaction(
  val walletId: UUID,
  val currency: String,
  val amount: BigDecimal,
  val type: TransactionType,
  val correlationId: UUID? = null,
  val idempotencyKey: String,
  val idempotencyDate: LocalDate = LocalDate.now(),
  val description: String? = null,
  val createdAt: ZonedDateTime = ZonedDateTime.now()
)
