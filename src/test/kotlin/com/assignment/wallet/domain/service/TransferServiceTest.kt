package com.assignment.wallet.domain.service

import com.assignment.wallet.domain.dto.NewTransfer
import com.assignment.wallet.domain.exception.SameWalletTransferException
import com.assignment.wallet.domain.exception.WalletNotFoundException
import com.assignment.wallet.domain.model.wallet.Wallet
import com.assignment.wallet.domain.model.wallet.WalletFactory
import com.assignment.wallet.domain.model.wallet.WalletProviderFacade
import com.assignment.wallet.domain.model.wallet.balance.BalanceProvider
import com.assignment.wallet.domain.model.wallet.lock.LockProvider
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.UUID

class TransferServiceTest {
  private val lockProvider = mockk<LockProvider>()

  private val provider = WalletProviderFacade(balance = mockk<BalanceProvider>(relaxed = true), lock = lockProvider)

  private val suite = TransferService(provider)

  @BeforeEach
  fun setUp() {
    mockkObject(WalletFactory.Companion)
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(WalletFactory.Companion)
  }

  @Nested
  inner class Transfer {
    private val sourceWalletId = UUID.randomUUID()

    private val targetWalletId = UUID.randomUUID()

    private val newTransfer =
      NewTransfer(
        sourceWalletId = sourceWalletId,
        targetWalletId = targetWalletId,
        currency = "BRL",
        amount = BigDecimal("100.00"),
        idempotencyKey = "tx-001",
        description = "payment"
      )

    @Test
    fun `should transfer funds between wallets`() {
      val sourceWallet = mockk<Wallet>()
      val targetWallet = mockk<Wallet>()
      val debit = mockk<Transaction>()
      val credit = mockk<Transaction>()

      every {
        WalletFactory.requireById(sourceWalletId)
      } returns sourceWallet
      every {
        WalletFactory.requireById(targetWalletId)
      } returns targetWallet
      every {
        lockProvider.withLocks(any(), eq("BRL"), any<() -> Any>())
      } answers { thirdArg<() -> Any>().invoke() }
      every {
        sourceWallet.withdraw(any(), any())
      } returns debit
      every {
        targetWallet.deposit(any(), any())
      } returns credit

      val (resultDebit, resultCredit) = suite.transfer(newTransfer)

      assertEquals(debit, resultDebit)
      assertEquals(credit, resultCredit)

      verify { sourceWallet.withdraw(any(), any()) }
      verify { targetWallet.deposit(any(), any()) }
    }

    @Test
    fun `should throw when source equals target`() {
      val sameId = UUID.randomUUID()

      val transfer = newTransfer.copy(sourceWalletId = sameId, targetWalletId = sameId)

      assertThrows<SameWalletTransferException> {
        suite.transfer(transfer)
      }
    }

    @Test
    fun `should throw when source wallet not found`() {
      every { WalletFactory.requireById(sourceWalletId) } throws WalletNotFoundException(sourceWalletId)

      assertThrows<WalletNotFoundException> {
        suite.transfer(newTransfer)
      }
    }

    @Test
    fun `should throw when target wallet not found`() {
      val sourceWallet = mockk<Wallet>()

      every { WalletFactory.requireById(sourceWalletId) } returns sourceWallet
      every { WalletFactory.requireById(targetWalletId) } throws WalletNotFoundException(targetWalletId)

      assertThrows<WalletNotFoundException> {
        suite.transfer(newTransfer)
      }
    }
  }
}
