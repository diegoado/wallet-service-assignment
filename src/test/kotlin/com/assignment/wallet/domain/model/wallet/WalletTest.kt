package com.assignment.wallet.domain.model.wallet

import com.assignment.wallet.domain.dto.NewDeposit
import com.assignment.wallet.domain.dto.NewWithdrawal
import com.assignment.wallet.domain.exception.IdempotentRequestException
import com.assignment.wallet.domain.exception.InsufficientFundsException
import com.assignment.wallet.domain.model.wallet.balance.BalanceKey
import com.assignment.wallet.domain.model.wallet.balance.BalanceProvider
import com.assignment.wallet.domain.model.wallet.lock.LockProvider
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.assignment.wallet.domain.model.wallet.transaction.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID

class WalletTest {
  private val walletId = UUID.randomUUID()

  private val walletRepository = mockk<WalletRepository>()

  private val balanceProvider = mockk<BalanceProvider>(relaxed = true)

  private val lockProvider = mockk<LockProvider>()

  private val provider = WalletProviderFacade(balance = balanceProvider, lock = lockProvider)

  private val suite =
    Wallet
      .Builder()
      .withId(walletId)
      .withDocument("12345678901")
      .withRepository(walletRepository)
      .withProvider(provider)
      .build()

  @Nested
  inner class GetBalance {
    @Test
    fun `should return cached balance on cache hit`() {
      every { balanceProvider.get(BalanceKey(walletId, "BRL")) } returns BigDecimal("250.00")

      val result = suite.getBalance("BRL")

      assertEquals(BigDecimal("250.00"), result)

      verify(exactly = 0) { walletRepository.computeBalance(any(), any()) }
    }

    @Test
    fun `should compute from ledger and cache on miss`() {
      every { balanceProvider.get(BalanceKey(walletId, "BRL")) } returns null
      every { walletRepository.computeBalance(walletId, "BRL") } returns BigDecimal("500.00")

      val result = suite.getBalance("BRL")

      assertEquals(BigDecimal("500.00"), result)

      verify { balanceProvider.set(BalanceKey(walletId, "BRL"), BigDecimal("500.00")) }
    }

    @Test
    fun `should return zero when no transactions exist`() {
      every { balanceProvider.get(BalanceKey(walletId, "USD")) } returns null
      every { walletRepository.computeBalance(walletId, "USD") } returns BigDecimal.ZERO

      val result = suite.getBalance("USD")

      assertEquals(BigDecimal.ZERO, result)

      verify { balanceProvider.set(BalanceKey(walletId, "USD"), BigDecimal.ZERO) }
    }
  }

  @Nested
  inner class GetHistoricalBalance {
    @Test
    fun `should return balance at given point in time`() {
      val at = ZonedDateTime.now().minusDays(5)

      every { walletRepository.computeBalanceAt(walletId, "BRL", at) } returns BigDecimal("300.00")

      val result = suite.getHistoricalBalance("BRL", at)

      assertEquals(BigDecimal("300.00"), result)
    }

    @Test
    fun `should return zero when no transactions before timestamp`() {
      val at = ZonedDateTime.now().minusYears(1)

      every { walletRepository.computeBalanceAt(walletId, "BRL", at) } returns BigDecimal.ZERO

      val result = suite.getHistoricalBalance("BRL", at)

      assertEquals(BigDecimal.ZERO, result)
    }
  }

  @Nested
  inner class Deposit {
    private val newDeposit =
      NewDeposit(
        currency = "BRL",
        amount = BigDecimal("100.00"),
        idempotencyKey = "dep-001",
        description = "Test deposit"
      )

    @Test
    fun `should create transaction and evict cache`() {
      every {
        walletRepository.getTransactionOrNull(walletId, "dep-001", any())
      } returns null
      every {
        walletRepository.newTransaction(walletId, any())
      } answers { secondArg() }
      every {
        lockProvider.withLock(walletId, "BRL", any<() -> Any>())
      } answers { thirdArg<() -> Any>().invoke() }

      val result = suite.deposit(newDeposit)

      assertEquals(walletId, result.walletId)
      assertEquals("BRL", result.currency)
      assertEquals(BigDecimal("100.00"), result.amount)
      assertEquals(TransactionType.DEPOSIT, result.type)
      assertEquals("dep-001", result.idempotencyKey)

      verify { balanceProvider.removeKey(BalanceKey(walletId, "BRL")) }
      verify { walletRepository.newTransaction(walletId, any()) }
    }

    @Test
    fun `should throw when idempotent request detected`() {
      val transaction = mockk<Transaction>()

      every { transaction.walletId } returns walletId
      every { walletRepository.getTransactionOrNull(walletId, "dep-001", any()) } returns transaction

      assertThrows<IdempotentRequestException> {
        suite.deposit(newDeposit)
      }

      verify(exactly = 0) { walletRepository.newTransaction(any(), any()) }
    }
  }

  @Nested
  inner class Withdraw {
    private val newWithdrawal =
      NewWithdrawal(
        currency = "BRL",
        amount = BigDecimal("50.00"),
        idempotencyKey = "wd-001",
        description = "Test withdrawal"
      )

    @Test
    fun `should create withdrawal transaction with negative amount`() {
      every {
        walletRepository.getTransactionOrNull(walletId, "wd-001", any())
      } returns null
      every {
        lockProvider.withLock(walletId, "BRL", any<() -> Any>())
      } answers { thirdArg<() -> Any>().invoke() }
      every {
        walletRepository.computeBalance(walletId, "BRL")
      } returns BigDecimal("100.00")
      every {
        walletRepository.newTransaction(walletId, any())
      } answers { secondArg() }

      val result = suite.withdraw(newWithdrawal)

      assertEquals(walletId, result.walletId)
      assertEquals("BRL", result.currency)
      assertEquals(BigDecimal("-50.00"), result.amount)
      assertEquals(TransactionType.WITHDRAW, result.type)

      verify { balanceProvider.removeKey(BalanceKey(walletId, "BRL")) }
    }

    @Test
    fun `should throw when insufficient funds`() {
      every {
        walletRepository.getTransactionOrNull(walletId, "wd-001", any())
      } returns null
      every {
        lockProvider.withLock(walletId, "BRL", any<() -> Any>())
      } answers { thirdArg<() -> Any>().invoke() }
      every {
        walletRepository.computeBalance(walletId, "BRL")
      } returns BigDecimal("10.00")

      assertThrows<InsufficientFundsException> {
        suite.withdraw(newWithdrawal)
      }

      verify(exactly = 0) { walletRepository.newTransaction(any(), any()) }
    }

    @Test
    fun `should throw when idempotent request detected`() {
      val transaction = mockk<Transaction>()

      every { transaction.walletId } returns walletId
      every { walletRepository.getTransactionOrNull(walletId, "wd-001", any()) } returns transaction

      assertThrows<IdempotentRequestException> {
        suite.withdraw(newWithdrawal)
      }
    }
  }
}
