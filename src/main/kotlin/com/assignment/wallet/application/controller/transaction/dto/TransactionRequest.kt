package com.assignment.wallet.application.controller.transaction.dto

import com.assignment.wallet.domain.shared.Patterns
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class TransactionRequest(
  @field:NotBlank
  @field:Pattern(regexp = Patterns.CURRENCY, message = "must be a valid ISO 4217 currency code")
  val currency: String,
  @field:DecimalMin("0.01", message = "must be greater than or equal to 0.01")
  val amount: BigDecimal,
  @field:Size(max = 255)
  val description: String? = null
)
