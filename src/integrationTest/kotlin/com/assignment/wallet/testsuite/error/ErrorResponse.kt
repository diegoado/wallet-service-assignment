package com.assignment.wallet.testsuite.error

import com.assignment.wallet.testsuite.error.Error.FieldError
import com.assignment.wallet.testsuite.error.Error.GlobalError
import com.assignment.wallet.testsuite.error.Error.ParameterError
import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
  val status: Int,
  val code: String,
  val message: String,
  @JsonProperty("field_errors")
  val fieldErrors: List<FieldError>? = null,
  @JsonProperty("global_errors")
  val globalErrors: List<GlobalError>? = null,
  @JsonProperty("parameter_errors")
  val parameterErrors: List<ParameterError>? = null
)
