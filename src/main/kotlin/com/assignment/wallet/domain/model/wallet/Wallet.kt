package com.assignment.wallet.domain.model.wallet

import com.assignment.wallet.domain.dto.NewDeposit
import com.assignment.wallet.domain.dto.NewWithdrawal
import com.assignment.wallet.domain.exception.IdempotentRequestException
import com.assignment.wallet.domain.exception.InsufficientFundsException
import com.assignment.wallet.domain.exception.WalletAlreadyExistsException
import com.assignment.wallet.domain.model.wallet.balance.BalanceKey
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.assignment.wallet.domain.model.wallet.transaction.TransactionType
import com.github.f4b6a3.uuid.UuidCreator
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class Wallet private constructor(
  val id: UUID,
  val document: String,
  val createdAt: ZonedDateTime,
  private val repository: WalletRepository,
  private val provider: WalletProviderFacade
) {
  fun saveIfNeeded() {
    repository.getByDocumentOrNull(document)?.let { throw WalletAlreadyExistsException(document) }
    repository.save(this)
  }

  fun getBalance(currency: String): BigDecimal {
    val cacheKey = BalanceKey(id, currency)

    provider.balance.get(cacheKey)?.let { return it }

    val balance = repository.computeBalance(id, currency)

    provider.balance.set(cacheKey, balance)
    return balance
  }

  fun getHistoricalBalance(
    currency: String,
    at: ZonedDateTime
  ): BigDecimal = repository.computeBalanceAt(id, currency, at)

  fun deposit(
    newDeposit: NewDeposit,
    correlationId: UUID? = null
  ): Transaction {
    checkIdempotency(newDeposit.idempotencyKey)

    val transaction =
      Transaction(
        walletId = id,
        currency = newDeposit.currency,
        amount = newDeposit.amount,
        type = TransactionType.DEPOSIT,
        correlationId = correlationId,
        idempotencyKey = newDeposit.idempotencyKey,
        description = newDeposit.description
      )

    provider.lock.withLock(id, newDeposit.currency) {
      repository.newTransaction(id, transaction)
      evictBalance(newDeposit.currency)
      transaction
    }
    return transaction
  }

  fun withdraw(
    newWithdrawal: NewWithdrawal,
    correlationId: UUID? = null
  ): Transaction {
    checkIdempotency(newWithdrawal.idempotencyKey)

    val transaction =
      Transaction(
        walletId = id,
        currency = newWithdrawal.currency,
        amount = newWithdrawal.amount.negate(),
        type = TransactionType.WITHDRAW,
        correlationId = correlationId,
        idempotencyKey = newWithdrawal.idempotencyKey,
        description = newWithdrawal.description
      )

    provider.lock.withLock(id, newWithdrawal.currency) {
      val balance = repository.computeBalance(id, newWithdrawal.currency)
      if (balance < newWithdrawal.amount) {
        throw InsufficientFundsException(id, newWithdrawal.currency)
      }

      repository.newTransaction(id, transaction)
      evictBalance(newWithdrawal.currency)
    }
    return transaction
  }

  private fun checkIdempotency(idempotencyKey: String) {
    repository.getTransactionOrNull(id, idempotencyKey, LocalDate.now())?.let {
      throw IdempotentRequestException(idempotencyKey)
    }
  }

  private fun evictBalance(currency: String) {
    provider.balance.removeKey(BalanceKey(id, currency))
  }

  class Builder {
    private var id: UUID = UuidCreator.getTimeOrderedEpoch()

    private lateinit var document: String

    private var createdAt: ZonedDateTime = ZonedDateTime.now()

    private lateinit var repository: WalletRepository

    private lateinit var provider: WalletProviderFacade

    fun withId(id: UUID): Builder = apply { this.id = id }

    fun withDocument(document: String): Builder = apply { this.document = document }

    fun withCreatedAt(createdAt: ZonedDateTime): Builder = apply { this.createdAt = createdAt }

    fun withRepository(repository: WalletRepository): Builder = apply { this.repository = repository }

    fun withProvider(provider: WalletProviderFacade): Builder = apply { this.provider = provider }

    fun build(): Wallet =
      Wallet(
        id = id,
        document = document,
        createdAt = createdAt,
        repository = repository,
        provider = provider
      )
  }
}
