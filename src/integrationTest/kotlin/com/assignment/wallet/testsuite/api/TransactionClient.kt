package com.assignment.wallet.testsuite.api

import com.assignment.wallet.application.controller.transaction.dto.TransactionRequest
import com.assignment.wallet.application.controller.transaction.dto.TransactionResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.util.UUID

interface TransactionClient {
  @PostMapping(
    "/wallets/{walletId}/deposits",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun deposit(
    @PathVariable walletId: UUID,
    @RequestHeader("Idempotency-Key") idempotencyKey: String?,
    @RequestBody request: TransactionRequest
  ): ResponseEntity<TransactionResponse>

  @PostMapping(
    "/wallets/{walletId}/withdrawals",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun withdraw(
    @PathVariable walletId: UUID,
    @RequestHeader("Idempotency-Key") idempotencyKey: String?,
    @RequestBody request: TransactionRequest
  ): ResponseEntity<TransactionResponse>

  companion object {
    const val NAME = "application-transaction-client"
  }
}
