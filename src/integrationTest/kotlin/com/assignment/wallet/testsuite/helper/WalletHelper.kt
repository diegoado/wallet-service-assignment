package com.assignment.wallet.testsuite.helper

import com.assignment.wallet.infrastructure.persistence.entity.WalletDbEntity
import com.assignment.wallet.infrastructure.persistence.repository.WalletDbRepository
import com.assignment.wallet.testsuite.TestContext
import com.github.f4b6a3.uuid.UuidCreator
import java.time.ZonedDateTime

object WalletHelper {
  private val walletDbRepository by lazy { TestContext.getBean(WalletDbRepository::class.java) }

  fun persistWallet(document: String): WalletDbEntity =
    walletDbRepository.save(
      WalletDbEntity(id = UuidCreator.getTimeOrderedEpoch(), document = document, createdAt = ZonedDateTime.now())
    )

  fun findByDocument(document: String): WalletDbEntity? = walletDbRepository.findByDocument(document)
}
