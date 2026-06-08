package com.assignment.wallet.infrastructure.persistence.entity

import com.github.f4b6a3.uuid.UuidCreator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "wallet")
data class WalletDbEntity(
  @Id
  val id: UUID = UuidCreator.getTimeOrderedEpoch()
) {
  @Column(nullable = false, updatable = false, unique = true, length = 14)
  lateinit var document: String
    private set

  @Column(name = "created_at", nullable = false, updatable = false)
  var createdAt: ZonedDateTime = ZonedDateTime.now()
    private set

  constructor(id: UUID, document: String, createdAt: ZonedDateTime) : this(id) {
    this.document = document
    this.createdAt = createdAt
  }
}
