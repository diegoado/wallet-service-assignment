package com.assignment.wallet.domain.model.wallet

import com.assignment.wallet.domain.exception.WalletNotFoundException
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class WalletFactory : ApplicationContextAware {
  override fun setApplicationContext(applicationContext: ApplicationContext) {
    WalletFactory.applicationContext = applicationContext
  }

  companion object {
    private lateinit var applicationContext: ApplicationContext

    fun createNew(document: String): Wallet =
      Wallet
        .Builder()
        .withDocument(document)
        .withRepository(applicationContext.getBean<WalletRepository>())
        .withProvider(applicationContext.getBean<WalletProviderFacade>())
        .build()

    fun requireById(walletId: UUID): Wallet {
      val repository = applicationContext.getBean<WalletRepository>()
      return repository.getById(walletId) ?: throw WalletNotFoundException(walletId)
    }

    fun findByDocument(document: String): Wallet? =
      applicationContext.getBean<WalletRepository>().getByDocumentOrNull(document)
  }
}
