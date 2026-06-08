package com.assignment.wallet.infrastructure.persistence.entity

import com.assignment.wallet.domain.model.wallet.transaction.TransactionType
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "transaction_ledger")
data class TransactionLedgerDbEntity(
  @Id
  val id: UUID = UuidCreator.getTimeOrderedEpoch()
) {
  @Column(name = "wallet_id", nullable = false, updatable = false)
  lateinit var walletId: UUID
    private set

  @Column(nullable = false, length = 3, updatable = false)
  lateinit var currency: String
    private set

  @Column(nullable = false, precision = 19, scale = 2, updatable = false)
  lateinit var amount: BigDecimal
    private set

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, updatable = false)
  lateinit var type: TransactionType
    private set

  @Column(name = "correlation_id", updatable = false)
  var correlationId: UUID? = null
    private set

  @Column(name = "idempotency_key", nullable = false, updatable = false)
  lateinit var idempotencyKey: String
    private set

  @Column(name = "idempotency_date", nullable = false, updatable = false)
  lateinit var idempotencyDate: LocalDate
    private set

  @Column(updatable = false)
  var description: String? = null
    private set

  @Column(name = "created_at", nullable = false, updatable = false)
  var createdAt: ZonedDateTime = ZonedDateTime.now()
    private set

  constructor(
    id: UUID = UuidCreator.getTimeOrderedEpoch(),
    walletId: UUID,
    currency: String,
    amount: BigDecimal,
    type: TransactionType,
    correlationId: UUID? = null,
    idempotencyKey: String,
    idempotencyDate: LocalDate = LocalDate.now(),
    description: String? = null
  ) : this(id) {
    this.walletId = walletId
    this.currency = currency
    this.amount = amount
    this.type = type
    this.correlationId = correlationId
    this.idempotencyKey = idempotencyKey
    this.idempotencyDate = idempotencyDate
    this.description = description
  }
}
