package com.assignment.wallet.steps.transaction

import com.assignment.wallet.application.controller.transaction.dto.TransactionRequest
import com.assignment.wallet.steps.StepsContext
import com.assignment.wallet.testsuite.api.TransactionClient
import com.assignment.wallet.testsuite.helper.ResponseHelper
import io.cucumber.java.en.When
import java.math.BigDecimal
import java.util.UUID

class WithdrawSteps(
  private val transactionClient: TransactionClient,
  private val stepsContext: StepsContext
) {
  @When("I withdraw {string} {string} with idempotency key {string}")
  fun iWithdraw(
    amount: String,
    currency: String,
    idempotencyKey: String
  ) {
    val walletId = stepsContext.getWalletId()!!

    stepsContext.setResponse {
      ResponseHelper.wrap {
        transactionClient.withdraw(walletId, idempotencyKey, TransactionRequest(currency, BigDecimal(amount)))
      }
    }
  }

  @When("I withdraw {string} {string} from wallet {string} with idempotency key {string}")
  fun iWithdrawFromWallet(
    amount: String,
    currency: String,
    walletId: String,
    idempotencyKey: String
  ) {
    stepsContext.setResponse {
      ResponseHelper.wrap {
        transactionClient.withdraw(
          UUID.fromString(walletId),
          idempotencyKey,
          TransactionRequest(currency, BigDecimal(amount))
        )
      }
    }
  }

  @When("I withdraw {string} {string} without idempotency key")
  fun iWithdrawWithoutIdempotencyKey(
    amount: String,
    currency: String
  ) {
    val walletId = stepsContext.getWalletId()!!

    stepsContext.setResponse {
      ResponseHelper.wrap {
        transactionClient.withdraw(walletId, null, TransactionRequest(currency, BigDecimal(amount)))
      }
    }
  }
}
