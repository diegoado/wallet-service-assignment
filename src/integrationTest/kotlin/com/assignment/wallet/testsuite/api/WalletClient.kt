package com.assignment.wallet.testsuite.api

import com.assignment.wallet.application.controller.wallet.dto.BalanceResponse
import com.assignment.wallet.application.controller.wallet.dto.CreateWalletRequest
import com.assignment.wallet.application.controller.wallet.dto.HistoricalBalanceResponse
import com.assignment.wallet.application.controller.wallet.dto.WalletResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface WalletClient {
  @PostMapping("/wallets", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun createWallet(
    @RequestBody request: CreateWalletRequest
  ): ResponseEntity<WalletResponse>

  @GetMapping("/wallets/{id}/balances", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getBalance(
    @PathVariable id: String,
    @RequestParam currency: String
  ): ResponseEntity<BalanceResponse>

  @GetMapping("/wallets/{id}/balances/history", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getHistoricalBalance(
    @PathVariable id: String,
    @RequestParam currency: String,
    @RequestParam at: String
  ): ResponseEntity<HistoricalBalanceResponse>

  companion object {
    const val NAME = "application-wallet-client"
  }
}
