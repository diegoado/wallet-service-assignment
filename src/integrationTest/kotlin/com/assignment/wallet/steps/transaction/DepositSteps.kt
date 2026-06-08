package com.assignment.wallet.steps.transaction

import com.assignment.wallet.application.controller.transaction.dto.TransactionRequest
import com.assignment.wallet.steps.StepsContext
import com.assignment.wallet.testsuite.api.TransactionClient
import com.assignment.wallet.testsuite.helper.ResponseHelper
import io.cucumber.java.en.When
import java.math.BigDecimal
import java.util.UUID

class DepositSteps(
  private val transactionClient: TransactionClient,
  private val stepsContext: StepsContext
) {
  @When("I deposit {string} {string} with idempotency key {string}")
  fun iDeposit(
    amount: String,
    currency: String,
    idempotencyKey: String
  ) {
    val walletId = stepsContext.getWalletId()!!

    stepsContext.setResponse {
      ResponseHelper.wrap {
        transactionClient.deposit(walletId, idempotencyKey, TransactionRequest(currency, BigDecimal(amount)))
      }
    }
  }

  @When("I deposit {string} {string} to wallet {string} with idempotency key {string}")
  fun iDepositToWallet(
    amount: String,
    currency: String,
    walletId: String,
    idempotencyKey: String
  ) {
    stepsContext.setResponse {
      ResponseHelper.wrap {
        transactionClient.deposit(
          UUID.fromString(walletId),
          idempotencyKey,
          TransactionRequest(currency, BigDecimal(amount))
        )
      }
    }
  }

  @When("I deposit {string} {string} without idempotency key")
  fun iDepositWithoutIdempotencyKey(
    amount: String,
    currency: String
  ) {
    val walletId = stepsContext.getWalletId()!!

    stepsContext.setResponse {
      ResponseHelper.wrap {
        transactionClient.deposit(walletId, null, TransactionRequest(currency, BigDecimal(amount)))
      }
    }
  }
}
