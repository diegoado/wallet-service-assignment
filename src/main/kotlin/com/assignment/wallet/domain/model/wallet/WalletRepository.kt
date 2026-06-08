package com.assignment.wallet.domain.model.wallet

import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

interface WalletRepository {
  fun getById(walletId: UUID): Wallet?

  fun getByDocumentOrNull(document: String): Wallet?

  fun save(wallet: Wallet): Wallet

  fun computeBalance(
    walletId: UUID,
    currency: String
  ): BigDecimal

  fun computeBalanceAt(
    walletId: UUID,
    currency: String,
    at: ZonedDateTime
  ): BigDecimal

  fun getTransactionOrNull(
    walletId: UUID,
    idempotencyKey: String,
    date: LocalDate
  ): Transaction?

  fun newTransaction(
    walletId: UUID,
    transaction: Transaction
  ): Transaction
}
