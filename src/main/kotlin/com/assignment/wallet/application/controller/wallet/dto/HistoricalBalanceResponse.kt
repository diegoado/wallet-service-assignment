package com.assignment.wallet.application.controller.wallet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

data class HistoricalBalanceResponse(
  @JsonProperty("wallet_id")
  val walletId: UUID,
  val currency: String,
  val balance: BigDecimal,
  val at: ZonedDateTime
)
