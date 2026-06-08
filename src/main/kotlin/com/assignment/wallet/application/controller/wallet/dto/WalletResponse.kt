package com.assignment.wallet.application.controller.wallet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime
import java.util.UUID

data class WalletResponse(
  val id: UUID,
  val document: String,
  @JsonProperty("created_at")
  val createdAt: ZonedDateTime
)
