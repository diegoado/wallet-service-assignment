package com.assignment.wallet.application.controller.transfer

import com.assignment.wallet.application.controller.transaction.dto.TransactionResponse
import com.assignment.wallet.application.controller.transfer.dto.TransferRequest
import com.assignment.wallet.application.controller.transfer.dto.TransferResponse
import com.assignment.wallet.domain.dto.NewTransfer
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.assignment.wallet.domain.service.TransferService
import com.assignment.wallet.domain.service.WalletService
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@Validated
@RestController
@RequestMapping(TransferController.RESOURCE_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
class TransferController(
  private val transferService: TransferService,
  private val walletService: WalletService
) {
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun transfer(
    @RequestHeader("Idempotency-Key") @Size(min = 1, max = 255) idempotencyKey: String,
    @Valid @RequestBody request: TransferRequest
  ): ResponseEntity<TransferResponse> {
    val newTransfer =
      NewTransfer(
        sourceWalletId = request.sourceWalletId,
        targetWalletId = request.targetWalletId,
        currency = request.currency,
        amount = request.amount,
        idempotencyKey = idempotencyKey,
        description = request.description
      )

    val (debit, credit) = transferService.transfer(newTransfer)

    val sourceBalance = walletService.getBalance(request.sourceWalletId, request.currency)
    val targetBalance = walletService.getBalance(request.targetWalletId, request.currency)

    return ResponseEntity.ok(
      TransferResponse(
        correlationId = debit.correlationId!!,
        sourceTransaction = toTransactionResponse(debit, sourceBalance),
        targetTransaction = toTransactionResponse(credit, targetBalance),
        createdAt = debit.createdAt
      )
    )
  }

  private fun toTransactionResponse(
    transaction: Transaction,
    balance: BigDecimal
  ) = TransactionResponse(
    walletId = transaction.walletId,
    currency = transaction.currency,
    amount = transaction.amount,
    type = transaction.type,
    balance = balance,
    createdAt = transaction.createdAt
  )

  companion object {
    const val RESOURCE_PATH = "/transfers"
  }
}
