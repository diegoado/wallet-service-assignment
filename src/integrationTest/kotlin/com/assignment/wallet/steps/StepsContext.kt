package com.assignment.wallet.steps

import com.assignment.wallet.infrastructure.persistence.entity.WalletDbEntity
import io.cucumber.java.Before
import io.cucumber.spring.ScenarioScope
import org.springframework.http.ResponseEntity
import java.util.UUID

@Suppress("UNCHECKED_CAST")
@ScenarioScope
class StepsContext {
  private val contexts: MutableMap<Context, Any> = mutableMapOf()

  @Before
  fun init() {}

  fun setResponse(factory: () -> ResponseEntity<*>) {
    contexts[Context.RESPONSE] = factory()
  }

  fun getResponseStatus(): Int {
    val response = contexts.getValue(Context.RESPONSE) as ResponseEntity<*>
    return response.statusCode.value()
  }

  fun <T> getResponseBody(): T {
    val response = contexts.getValue(Context.RESPONSE) as ResponseEntity<*>
    return response.body as T
  }

  fun setWallet(wallet: WalletDbEntity) {
    contexts[Context.WALLET] = wallet
  }

  fun getWalletId(): UUID? = (contexts[Context.WALLET] as? WalletDbEntity)?.id

  private enum class Context {
    RESPONSE,
    WALLET
  }
}
