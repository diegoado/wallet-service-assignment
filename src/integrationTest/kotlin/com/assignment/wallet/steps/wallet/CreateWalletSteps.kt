package com.assignment.wallet.steps.wallet

import com.assignment.wallet.application.controller.wallet.dto.CreateWalletRequest
import com.assignment.wallet.application.controller.wallet.dto.WalletResponse
import com.assignment.wallet.steps.StepsContext
import com.assignment.wallet.testsuite.api.WalletClient
import com.assignment.wallet.testsuite.helper.ResponseHelper
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.util.UUID
import kotlin.test.assertEquals

class CreateWalletSteps(
  private val walletClient: WalletClient,
  private val stepsContext: StepsContext
) {
  private var walletId: UUID? = null

  @When("I create a wallet with document {string}")
  fun iCreateAWalletWithDocument(document: String) {
    stepsContext.setResponse {
      ResponseHelper.wrap { walletClient.createWallet(CreateWalletRequest(document)) }
    }
  }

  @Then("The response should contain wallet id and document {string}")
  fun theResponseShouldContainWalletIdAndDocument(document: String) {
    val body = stepsContext.getResponseBody<WalletResponse>()

    assertEquals(document, body.document)

    walletId?.let { assertEquals(it, body.id) }
  }
}
