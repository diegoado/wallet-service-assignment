package com.assignment.wallet.application.controller.transfer.dto

import com.assignment.wallet.domain.shared.Patterns
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class TransferRequest(
  @JsonProperty("source_wallet_id")
  val sourceWalletId: UUID,
  @JsonProperty("target_wallet_id")
  val targetWalletId: UUID,
  @field:NotBlank
  @field:Pattern(regexp = Patterns.CURRENCY, message = "must be a valid ISO 4217 currency code")
  val currency: String,
  @field:DecimalMin("0.01", message = "must be greater than or equal to 0.01")
  val amount: BigDecimal,
  @field:Size(max = 255)
  val description: String? = null
)
