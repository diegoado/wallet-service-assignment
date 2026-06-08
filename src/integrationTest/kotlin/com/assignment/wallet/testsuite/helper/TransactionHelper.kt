package com.assignment.wallet.testsuite.helper

import com.assignment.wallet.domain.model.wallet.transaction.TransactionType
import com.assignment.wallet.infrastructure.persistence.entity.TransactionLedgerDbEntity
import com.assignment.wallet.infrastructure.persistence.repository.TransactionLedgerDbRepository
import com.assignment.wallet.testsuite.TestContext
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

object TransactionHelper {
  private val repository by lazy { TestContext.getBean(TransactionLedgerDbRepository::class.java) }

  fun persistDeposit(
    walletId: UUID,
    currency: String,
    amount: BigDecimal,
    idempotencyKey: String
  ) {
    repository.save(
      TransactionLedgerDbEntity(
        walletId = walletId,
        currency = currency,
        amount = amount,
        type = TransactionType.DEPOSIT,
        idempotencyKey = idempotencyKey
      )
    )
  }

  fun findByWalletId(walletId: UUID): List<TransactionLedgerDbEntity> =
    repository.findAll().filter { it.walletId == walletId }

  fun findByWalletIdAndIdempotencyKey(
    walletId: UUID,
    idempotencyKey: String
  ): TransactionLedgerDbEntity? =
    repository.findByWalletIdAndIdempotencyKeyAndIdempotencyDate(walletId, idempotencyKey, LocalDate.now())
}
