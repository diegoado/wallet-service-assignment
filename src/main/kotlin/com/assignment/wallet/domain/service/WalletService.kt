package com.assignment.wallet.domain.service

import com.assignment.wallet.Loggable
import com.assignment.wallet.asKeyValue
import com.assignment.wallet.domain.dto.NewDeposit
import com.assignment.wallet.domain.dto.NewWithdrawal
import com.assignment.wallet.domain.model.wallet.Wallet
import com.assignment.wallet.domain.model.wallet.WalletFactory
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

@Service
class WalletService : Loggable {
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun create(document: String): Wallet {
    var wallet = WalletFactory.findByDocument(document)

    if (wallet != null) {
      logger.warn("Wallet already exists for document, skipping creation", asKeyValue("document", document))
      return wallet
    }

    logger.info("Creating wallet for document", asKeyValue("document", document))
    wallet = WalletFactory.createNew(document)
    wallet.saveIfNeeded()

    logger.info("Wallet created id={}", wallet.id, asKeyValue("walletId", wallet.id))
    return wallet
  }

  fun getBalance(
    walletId: UUID,
    currency: String
  ): BigDecimal {
    logger.info("Getting balance for wallet={} currency={}", walletId, currency, asKeyValue("walletId", walletId))

    val wallet = WalletFactory.requireById(walletId)
    return wallet.getBalance(currency)
  }

  fun getHistoricalBalance(
    walletId: UUID,
    currency: String,
    at: ZonedDateTime
  ): BigDecimal {
    logger.info(
      "Getting historical balance for wallet={} currency={} at={}",
      walletId,
      currency,
      at,
      asKeyValue("walletId", walletId)
    )

    val wallet = WalletFactory.requireById(walletId)
    return wallet.getHistoricalBalance(currency, at)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun deposit(
    walletId: UUID,
    newDeposit: NewDeposit
  ): Transaction {
    logger.info(
      "Depositing amount={} currency={} to wallet={}",
      newDeposit.amount,
      newDeposit.currency,
      walletId,
      asKeyValue("walletId", walletId)
    )

    val wallet = WalletFactory.requireById(walletId)
    return wallet.deposit(newDeposit)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun withdraw(
    walletId: UUID,
    newWithdrawal: NewWithdrawal
  ): Transaction {
    logger.info(
      "Withdrawing amount={} currency={} from wallet={}",
      newWithdrawal.amount,
      newWithdrawal.currency,
      walletId,
      asKeyValue("walletId", walletId)
    )

    val wallet = WalletFactory.requireById(walletId)
    return wallet.withdraw(newWithdrawal)
  }
}
