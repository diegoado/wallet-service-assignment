package com.assignment.wallet.steps.transaction

import com.assignment.wallet.application.controller.transaction.dto.TransactionResponse
import com.assignment.wallet.steps.StepsContext
import com.assignment.wallet.testsuite.helper.TransactionHelper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransactionSteps(
  private val stepsContext: StepsContext
) {
  @Given("A deposit of {string} {string} exists with idempotency key {string}")
  fun aDepositExists(
    amount: String,
    currency: String,
    idempotencyKey: String
  ) {
    val walletId = stepsContext.getWalletId()!!
    TransactionHelper.persistDeposit(walletId, currency, BigDecimal(amount), idempotencyKey)
  }

  @Then("The transaction response should have type {string} and amount {string}")
  fun theTransactionResponseShouldHave(
    type: String,
    amount: String
  ) {
    val body = stepsContext.getResponseBody<TransactionResponse>()

    assertEquals(type, body.type.name)
    assertEquals(BigDecimal(amount), body.amount)
  }

  @Then("A transaction with idempotency key {string} should exist in the db with type {string} and amount {string}")
  fun aTransactionWithIdempotencyKeyShouldExist(
    idempotencyKey: String,
    type: String,
    amount: String
  ) {
    val walletId = stepsContext.getWalletId()!!

    val transaction = TransactionHelper.findByWalletIdAndIdempotencyKey(walletId, idempotencyKey)

    assertNotNull(transaction, "Expected transaction with idempotency key '$idempotencyKey'")
    assertEquals(type, transaction.type.name)
    assertEquals(BigDecimal(amount), transaction.amount)
  }
}
