package com.assignment.wallet.infrastructure.persistence.repository

import com.assignment.wallet.infrastructure.persistence.entity.TransactionLedgerDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

interface TransactionLedgerDbRepository : JpaRepository<TransactionLedgerDbEntity, UUID> {
  @Query(
    value = """
      SELECT
        COALESCE(SUM(t.amount), 0)
        FROM TransactionLedgerDbEntity t
        WHERE t.walletId = :walletId AND t.currency = :currency
    """
  )
  fun computeBalance(
    walletId: UUID,
    currency: String
  ): BigDecimal

  @Query(
    value = """
      SELECT
        COALESCE(SUM(t.amount), 0)
      FROM TransactionLedgerDbEntity t
      WHERE t.walletId = :walletId AND t.currency = :currency AND t.createdAt <= :at
    """
  )
  fun computeBalanceAt(
    walletId: UUID,
    currency: String,
    at: ZonedDateTime
  ): BigDecimal

  fun findByWalletIdAndIdempotencyKeyAndIdempotencyDate(
    walletId: UUID,
    idempotencyKey: String,
    idempotencyDate: LocalDate
  ): TransactionLedgerDbEntity?
}
