package com.assignment.wallet.application.controller.transfer.dto

import com.assignment.wallet.application.controller.transaction.dto.TransactionResponse
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime
import java.util.UUID

data class TransferResponse(
  @JsonProperty("correlation_id")
  val correlationId: UUID,
  @JsonProperty("source_transaction")
  val sourceTransaction: TransactionResponse,
  @JsonProperty("target_transaction")
  val targetTransaction: TransactionResponse,
  @JsonProperty("created_at")
  val createdAt: ZonedDateTime
)
