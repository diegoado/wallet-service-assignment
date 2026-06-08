package com.assignment.wallet.testsuite.api

import com.assignment.wallet.application.controller.transfer.dto.TransferRequest
import com.assignment.wallet.application.controller.transfer.dto.TransferResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

interface TransferClient {
  @PostMapping(
    "/transfers",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun transfer(
    @RequestHeader("Idempotency-Key") idempotencyKey: String,
    @RequestBody request: TransferRequest
  ): ResponseEntity<TransferResponse>

  companion object {
    const val NAME = "application-transfer-client"
  }
}
