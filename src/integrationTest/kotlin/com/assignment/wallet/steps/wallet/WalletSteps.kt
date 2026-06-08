package com.assignment.wallet.steps.wallet

import com.assignment.wallet.steps.StepsContext
import com.assignment.wallet.testsuite.helper.WalletHelper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WalletSteps(
  private val stepsContext: StepsContext
) {
  @Given("A wallet exists with document {string}")
  fun aWalletExistsWithDocument(document: String) {
    stepsContext.setWallet(WalletHelper.persistWallet(document))
  }

  @Then("The wallet should exist in the database with document {string}")
  fun theWalletShouldExistInDatabase(document: String) {
    val wallet = WalletHelper.findByDocument(document)

    assertNotNull(wallet, "Expected wallet with document '$document' in database")
    assertEquals(document, wallet.document)
  }
}
