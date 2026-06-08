package com.assignment.wallet.testsuite

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class TestContext : ApplicationContextAware {
  override fun setApplicationContext(context: ApplicationContext) {
    applicationContext = context
  }

  companion object {
    private lateinit var applicationContext: ApplicationContext

    fun <T : Any> getBean(beanClass: Class<T>): T = applicationContext.getBean(beanClass)
  }
}
