package com.assignment.wallet.testsuite.helper

import com.assignment.wallet.testsuite.JsonUtils
import com.assignment.wallet.testsuite.error.ErrorResponse
import feign.FeignException
import org.springframework.http.ResponseEntity

object ResponseHelper {
  fun wrap(action: () -> ResponseEntity<*>): ResponseEntity<*> {
    return try {
      action()
    } catch (e: FeignException) {
      return ResponseEntity.status(e.status()).body(JsonUtils.toJson(e.contentUTF8(), ErrorResponse::class.java))
    }
  }
}
