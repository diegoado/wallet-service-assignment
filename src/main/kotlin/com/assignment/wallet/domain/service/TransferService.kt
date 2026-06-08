package com.assignment.wallet.domain.service

import com.assignment.wallet.Loggable
import com.assignment.wallet.asKeyValue
import com.assignment.wallet.domain.dto.NewDeposit
import com.assignment.wallet.domain.dto.NewTransfer
import com.assignment.wallet.domain.dto.NewWithdrawal
import com.assignment.wallet.domain.exception.SameWalletTransferException
import com.assignment.wallet.domain.model.wallet.WalletFactory
import com.assignment.wallet.domain.model.wallet.WalletProviderFacade
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TransferService(
  private val provider: WalletProviderFacade
) : Loggable {
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun transfer(newTransfer: NewTransfer): Pair<Transaction, Transaction> {
    if (newTransfer.sourceWalletId == newTransfer.targetWalletId) {
      throw SameWalletTransferException()
    }

    logger.info(
      "Transferring amount={} currency={} from={} to={}",
      newTransfer.amount,
      newTransfer.currency,
      newTransfer.sourceWalletId,
      newTransfer.targetWalletId,
      asKeyValue("sourceWalletId", newTransfer.sourceWalletId),
      asKeyValue("targetWalletId", newTransfer.targetWalletId)
    )

    val sourceWallet = WalletFactory.requireById(newTransfer.sourceWalletId)
    val targetWallet = WalletFactory.requireById(newTransfer.targetWalletId)

    val correlationId = UuidCreator.getTimeOrderedEpoch()

    return provider.lock.withLocks(
      listOf(newTransfer.sourceWalletId, newTransfer.targetWalletId),
      newTransfer.currency
    ) {
      val debit =
        sourceWallet.withdraw(
          newWithdrawal =
            NewWithdrawal(
              currency = newTransfer.currency,
              amount = newTransfer.amount,
              idempotencyKey = newTransfer.idempotencyKey,
              description = newTransfer.description
            ),
          correlationId = correlationId
        )

      val credit =
        targetWallet.deposit(
          newDeposit =
            NewDeposit(
              currency = newTransfer.currency,
              amount = newTransfer.amount,
              idempotencyKey = newTransfer.idempotencyKey,
              description = newTransfer.description
            ),
          correlationId = correlationId
        )

      debit to credit
    }
  }
}
