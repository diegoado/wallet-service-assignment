package com.assignment.wallet.steps

import com.assignment.wallet.testsuite.error.ErrorResponse
import io.cucumber.java.en.Then
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommonSteps(
  private val stepsContext: StepsContext
) {
  @Then("The response status should be {int}")
  fun theResponseStatusShouldBe(status: Int) {
    assertEquals(status, stepsContext.getResponseStatus())
  }

  @Then("The error response should have field error on {string} with code {string} and message {string}")
  fun theErrorResponseShouldHaveFieldErrorWithMessage(
    property: String,
    code: String,
    message: String
  ) {
    val body = stepsContext.getResponseBody<ErrorResponse>()

    requireNotNull(body.fieldErrors)
    val fieldError = body.fieldErrors.find { it.property == property }

    assertNotNull(fieldError, "Expected field error on '$property' but found none")
    assertEquals(code, fieldError.code)
    assertEquals(message, fieldError.message)
  }

  @Then("The error response message should contain {string}")
  fun theErrorResponseMessageShouldContain(expected: String) {
    val body = stepsContext.getResponseBody<ErrorResponse>()

    assertNotNull(body.message)
    assertTrue(body.message.contains(expected), "Expected message to contain '$expected' but was '${body.message}'")
  }
}
