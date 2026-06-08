package com.assignment.wallet.infrastructure.persistence.repository

import com.assignment.wallet.infrastructure.persistence.entity.WalletDbEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WalletDbRepository : JpaRepository<WalletDbEntity, UUID> {
  fun findByDocument(document: String): WalletDbEntity?
}
