package com.assignment.wallet.application.controller.wallet

import com.assignment.wallet.application.controller.wallet.dto.BalanceResponse
import com.assignment.wallet.application.controller.wallet.dto.CreateWalletRequest
import com.assignment.wallet.application.controller.wallet.dto.HistoricalBalanceResponse
import com.assignment.wallet.application.controller.wallet.dto.WalletResponse
import com.assignment.wallet.domain.model.wallet.Wallet
import com.assignment.wallet.domain.service.WalletService
import com.assignment.wallet.domain.shared.Constants
import com.assignment.wallet.domain.shared.Patterns
import com.assignment.wallet.domain.shared.asZonedDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@Validated
@RestController
@RequestMapping(WalletController.RESOURCE_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
class WalletController(
  private val walletService: WalletService
) {
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun create(
    @Valid @RequestBody request: CreateWalletRequest
  ): ResponseEntity<WalletResponse> {
    val createdWallet = walletService.create(request.document)

    val walletLocPath =
      ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(createdWallet.id)
        .toUri()

    return ResponseEntity.created(walletLocPath).body(toResponse(createdWallet))
  }

  @GetMapping("/{id}/balances")
  fun getBalance(
    @PathVariable id: UUID,
    @RequestParam
    @Pattern(regexp = Patterns.CURRENCY, message = "must be a valid ISO 4217 currency code")
    currency: String
  ): ResponseEntity<BalanceResponse> {
    val balance = walletService.getBalance(id, currency)
    return ResponseEntity.ok(BalanceResponse(walletId = id, currency = currency, balance = balance))
  }

  @GetMapping("/{id}/balances/history")
  fun getHistoricalBalance(
    @PathVariable id: UUID,
    @RequestParam
    @Pattern(regexp = Patterns.CURRENCY, message = "must be a valid ISO 4217 currency code")
    currency: String,
    @RequestParam
    @Pattern(regexp = Patterns.DATETIME, message = "must match pattern ${Constants.DATETIME_FORMAT}")
    at: String
  ): ResponseEntity<HistoricalBalanceResponse> {
    val epochAt = at.asZonedDateTime()
    val balance = walletService.getHistoricalBalance(id, currency, epochAt)

    return ResponseEntity.ok(
      HistoricalBalanceResponse(walletId = id, currency = currency, balance = balance, at = epochAt)
    )
  }

  private fun toResponse(wallet: Wallet): WalletResponse =
    WalletResponse(
      id = wallet.id,
      document = wallet.document,
      createdAt = wallet.createdAt
    )

  companion object {
    const val RESOURCE_PATH = "/wallets"
  }
}
