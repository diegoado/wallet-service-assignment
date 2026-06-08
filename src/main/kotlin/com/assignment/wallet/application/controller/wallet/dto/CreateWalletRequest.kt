package com.assignment.wallet.application.controller.wallet.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateWalletRequest(
  @field:NotBlank
  @field:Pattern(regexp = "^\\d{11}(\\d{3})?$", message = "must be a valid CPF (11 digits) or CNPJ (14 digits)")
  val document: String
)
