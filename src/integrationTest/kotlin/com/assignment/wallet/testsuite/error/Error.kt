package com.assignment.wallet.testsuite.error

import com.fasterxml.jackson.annotation.JsonProperty

object Error {
  data class FieldError(
    val code: String,
    val property: String,
    val message: String,
    @JsonProperty("rejected_value")
    val rejectedValue: Any? = null
  )

  data class GlobalError(
    val code: String,
    val message: String
  )

  data class ParameterError(
    val code: String,
    val parameter: String,
    val message: String,
    @JsonProperty("rejected_value")
    val rejectedValue: Any? = null
  )
}
