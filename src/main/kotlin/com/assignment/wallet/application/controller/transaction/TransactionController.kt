package com.assignment.wallet.application.controller.transaction

import com.assignment.wallet.application.controller.transaction.dto.TransactionRequest
import com.assignment.wallet.application.controller.transaction.dto.TransactionResponse
import com.assignment.wallet.domain.dto.NewDeposit
import com.assignment.wallet.domain.dto.NewWithdrawal
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.assignment.wallet.domain.service.WalletService
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping(TransactionController.RESOURCE_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
class TransactionController(
  private val walletService: WalletService
) {
  @PostMapping("/deposits", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun deposit(
    @PathVariable walletId: UUID,
    @RequestHeader("Idempotency-Key") @Size(min = 1, max = 255) idempotencyKey: String,
    @Valid @RequestBody request: TransactionRequest
  ): ResponseEntity<TransactionResponse> {
    val deposit =
      NewDeposit(
        currency = request.currency,
        amount = request.amount,
        idempotencyKey = idempotencyKey,
        description = request.description
      )

    val transaction = walletService.deposit(walletId, deposit)
    return ResponseEntity.ok(toResponse(transaction, walletService.getBalance(walletId, request.currency)))
  }

  @PostMapping("/withdrawals", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun withdraw(
    @PathVariable walletId: UUID,
    @RequestHeader("Idempotency-Key") @Size(min = 1, max = 255) idempotencyKey: String,
    @Valid @RequestBody request: TransactionRequest
  ): ResponseEntity<TransactionResponse> {
    val withdrawal =
      NewWithdrawal(
        currency = request.currency,
        amount = request.amount,
        idempotencyKey = idempotencyKey,
        description = request.description
      )

    val transaction = walletService.withdraw(walletId, withdrawal)
    return ResponseEntity.ok(toResponse(transaction, walletService.getBalance(walletId, request.currency)))
  }

  private fun toResponse(
    transaction: Transaction,
    balance: java.math.BigDecimal
  ) = TransactionResponse(
    walletId = transaction.walletId,
    currency = transaction.currency,
    amount = transaction.amount,
    type = transaction.type,
    balance = balance,
    createdAt = transaction.createdAt
  )

  companion object {
    const val RESOURCE_PATH = "/wallets/{walletId}"
  }
}
