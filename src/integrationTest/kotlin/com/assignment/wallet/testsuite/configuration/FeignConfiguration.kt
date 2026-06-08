package com.assignment.wallet.testsuite.configuration

import com.assignment.wallet.testsuite.api.TransactionClient
import com.assignment.wallet.testsuite.api.TransferClient
import com.assignment.wallet.testsuite.api.WalletClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClientBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class FeignConfiguration : ApplicationContextAware {
  private lateinit var feignClientBuilder: FeignClientBuilder

  override fun setApplicationContext(applicationContext: ApplicationContext) {
    feignClientBuilder = FeignClientBuilder(applicationContext)
  }

  @Lazy
  @Bean
  fun walletClient(
    @Value("\${local.server.port}") port: Int
  ): WalletClient =
    feignClientBuilder.forType(WalletClient::class.java, WalletClient.NAME).url("http://localhost:$port").build()

  @Lazy
  @Bean
  fun transactionClient(
    @Value("\${local.server.port}") port: Int
  ): TransactionClient =
    feignClientBuilder
      .forType(
        TransactionClient::class.java,
        TransactionClient.NAME
      ).url("http://localhost:$port")
      .build()

  @Lazy
  @Bean
  fun transferClient(
    @Value("\${local.server.port}") port: Int
  ): TransferClient =
    feignClientBuilder.forType(TransferClient::class.java, TransferClient.NAME).url("http://localhost:$port").build()
}
