package com.assignment.wallet.infrastructure.repository.impl

import com.assignment.wallet.Loggable
import com.assignment.wallet.asKeyValue
import com.assignment.wallet.domain.model.wallet.Wallet
import com.assignment.wallet.domain.model.wallet.WalletProviderFacade
import com.assignment.wallet.domain.model.wallet.WalletRepository
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.assignment.wallet.infrastructure.persistence.entity.TransactionLedgerDbEntity
import com.assignment.wallet.infrastructure.persistence.entity.WalletDbEntity
import com.assignment.wallet.infrastructure.persistence.repository.TransactionLedgerDbRepository
import com.assignment.wallet.infrastructure.persistence.repository.WalletDbRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

@Component
class WalletRepositoryImpl(
  private val walletDbRepository: WalletDbRepository,
  private val transactionLedgerDbRepository: TransactionLedgerDbRepository,
  private val walletProvider: WalletProviderFacade
) : WalletRepository,
  Loggable {
  override fun getById(walletId: UUID): Wallet? =
    walletDbRepository.findById(walletId).map { it.toDomain() }.orElse(null)

  override fun getByDocumentOrNull(document: String): Wallet? {
    logger.info("Looking up wallet by document", asKeyValue("document", document))
    return walletDbRepository.findByDocument(document)?.toDomain()
  }

  override fun save(wallet: Wallet): Wallet {
    logger.info("Persisting wallet id={}", wallet.id, asKeyValue("walletId", wallet.id))

    val entity =
      WalletDbEntity(
        id = wallet.id,
        document = wallet.document,
        createdAt = wallet.createdAt
      )
    walletDbRepository.save(entity)
    return wallet
  }

  override fun computeBalance(
    walletId: UUID,
    currency: String
  ): BigDecimal {
    logger.info(
      "Computing wallet balance for currency={}",
      currency,
      asKeyValue("walletId", walletId),
      asKeyValue("currency", currency)
    )
    return transactionLedgerDbRepository.computeBalance(walletId, currency)
  }

  override fun computeBalanceAt(
    walletId: UUID,
    currency: String,
    at: ZonedDateTime
  ): BigDecimal {
    logger.info(
      "Computing historical balance for currency={} at={}",
      currency,
      at,
      asKeyValue("walletId", walletId),
      asKeyValue("currency", currency)
    )
    return transactionLedgerDbRepository.computeBalanceAt(walletId, currency, at)
  }

  override fun getTransactionOrNull(
    walletId: UUID,
    idempotencyKey: String,
    date: LocalDate
  ): Transaction? {
    logger.info("Looking up transaction for idempotency check", asKeyValue("walletId", walletId))

    return transactionLedgerDbRepository
      .findByWalletIdAndIdempotencyKeyAndIdempotencyDate(walletId, idempotencyKey, date)
      ?.toTransaction()
  }

  override fun newTransaction(
    walletId: UUID,
    transaction: Transaction
  ): Transaction {
    logger.info(
      "Persisting transaction type={} currency={}",
      transaction.type,
      transaction.currency,
      asKeyValue("walletId", walletId)
    )

    val entity =
      TransactionLedgerDbEntity(
        walletId = walletId,
        currency = transaction.currency,
        amount = transaction.amount,
        type = transaction.type,
        correlationId = transaction.correlationId,
        idempotencyKey = transaction.idempotencyKey,
        idempotencyDate = transaction.idempotencyDate,
        description = transaction.description
      )

    transactionLedgerDbRepository.save(entity)
    return transaction
  }

  private fun WalletDbEntity.toDomain() =
    Wallet
      .Builder()
      .withId(id)
      .withDocument(document)
      .withCreatedAt(createdAt)
      .withRepository(this@WalletRepositoryImpl)
      .withProvider(walletProvider)
      .build()

  private fun TransactionLedgerDbEntity.toTransaction() =
    Transaction(
      walletId = walletId,
      currency = currency,
      amount = amount,
      type = type,
      correlationId = correlationId,
      idempotencyKey = idempotencyKey,
      idempotencyDate = idempotencyDate,
      description = description,
      createdAt = createdAt
    )
}
