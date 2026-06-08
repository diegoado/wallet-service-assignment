package com.assignment.wallet.testsuite

import tools.jackson.databind.ObjectMapper

object JsonUtils {
  private val objectMapper by lazy { TestContext.getBean(ObjectMapper::class.java) }

  fun <T> toJson(
    json: String,
    clazz: Class<T>
  ): T = objectMapper.readValue(json, clazz)
}
