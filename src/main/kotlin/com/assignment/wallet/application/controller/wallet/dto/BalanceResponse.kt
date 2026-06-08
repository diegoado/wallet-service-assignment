package com.assignment.wallet.application.controller.wallet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.UUID

data class BalanceResponse(
  @JsonProperty("wallet_id")
  val walletId: UUID,
  val currency: String,
  val balance: BigDecimal
)
