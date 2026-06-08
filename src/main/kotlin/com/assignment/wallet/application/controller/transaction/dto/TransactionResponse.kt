package com.assignment.wallet.application.controller.transaction.dto

import com.assignment.wallet.domain.model.wallet.transaction.TransactionType
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class TransactionResponse(
  @JsonProperty("wallet_id")
  val walletId: UUID,
  val currency: String,
  val amount: BigDecimal,
  val type: TransactionType,
  val balance: BigDecimal,
  @JsonProperty("created_at")
  val createdAt: ZonedDateTime
)
