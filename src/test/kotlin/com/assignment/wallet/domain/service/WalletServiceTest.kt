package com.assignment.wallet.domain.service

import com.assignment.wallet.domain.dto.NewDeposit
import com.assignment.wallet.domain.dto.NewWithdrawal
import com.assignment.wallet.domain.exception.WalletNotFoundException
import com.assignment.wallet.domain.model.wallet.Wallet
import com.assignment.wallet.domain.model.wallet.WalletFactory
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
import java.time.ZonedDateTime
import java.util.UUID

class WalletServiceTest {
  private val suite = WalletService()

  @BeforeEach
  fun setUp() {
    mockkObject(WalletFactory.Companion)
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(WalletFactory.Companion)
  }

  @Nested
  inner class Create {
    @Test
    fun `should return existing wallet when document already exists`() {
      val wallet = mockk<Wallet>()

      every { WalletFactory.findByDocument("12345678901") } returns wallet

      val result = suite.create("12345678901")

      assertEquals(wallet, result)

      verify(exactly = 0) { WalletFactory.createNew(any()) }
    }

    @Test
    fun `should create and save new wallet when document does not exist`() {
      val wallet = mockk<Wallet>(relaxed = true)

      every { WalletFactory.findByDocument("12345678901") } returns null
      every { WalletFactory.createNew("12345678901") } returns wallet

      val result = suite.create("12345678901")

      assertEquals(wallet, result)

      verify { wallet.saveIfNeeded() }
    }
  }

  @Nested
  inner class GetBalance {
    private val walletId = UUID.randomUUID()

    @Test
    fun `should return balance from wallet`() {
      val wallet = mockk<Wallet>()

      every { WalletFactory.requireById(walletId) } returns wallet
      every { wallet.getBalance("BRL") } returns BigDecimal("100.00")

      val result = suite.getBalance(walletId, "BRL")

      assertEquals(BigDecimal("100.00"), result)

      verify { wallet.getBalance("BRL") }
    }

    @Test
    fun `should throw when wallet not found`() {
      every { WalletFactory.requireById(walletId) } throws WalletNotFoundException(walletId)

      assertThrows<WalletNotFoundException> {
        suite.getBalance(walletId, "BRL")
      }
    }
  }

  @Nested
  inner class GetHistoricalBalance {
    private val walletId = UUID.randomUUID()

    private val at = ZonedDateTime.now().minusDays(3)

    @Test
    fun `should return historical balance from wallet`() {
      val wallet = mockk<Wallet>()

      every { WalletFactory.requireById(walletId) } returns wallet
      every { wallet.getHistoricalBalance("BRL", at) } returns BigDecimal("750.00")

      val result = suite.getHistoricalBalance(walletId, "BRL", at)

      assertEquals(BigDecimal("750.00"), result)
      verify { wallet.getHistoricalBalance("BRL", at) }
    }

    @Test
    fun `should throw when wallet not found`() {
      every { WalletFactory.requireById(walletId) } throws WalletNotFoundException(walletId)

      assertThrows<WalletNotFoundException> {
        suite.getHistoricalBalance(walletId, "BRL", at)
      }
    }
  }

  @Nested
  inner class Deposit {
    private val walletId = UUID.randomUUID()

    private val deposit = NewDeposit(currency = "BRL", amount = BigDecimal("50.00"), idempotencyKey = "key-1")

    @Test
    fun `should deposit and return transaction`() {
      val wallet = mockk<Wallet>()
      val expectedTransaction = mockk<Transaction>()

      every { WalletFactory.requireById(walletId) } returns wallet
      every { wallet.deposit(deposit) } returns expectedTransaction

      val result = suite.deposit(walletId, deposit)

      assertEquals(expectedTransaction, result)

      verify { wallet.deposit(deposit) }
    }

    @Test
    fun `should throw when wallet not found`() {
      every { WalletFactory.requireById(walletId) } throws WalletNotFoundException(walletId)

      assertThrows<WalletNotFoundException> {
        suite.deposit(walletId, deposit)
      }
    }
  }

  @Nested
  inner class Withdraw {
    private val walletId = UUID.randomUUID()

    private val withdrawal = NewWithdrawal(currency = "BRL", amount = BigDecimal("50.00"), idempotencyKey = "wd-1")

    @Test
    fun `should withdraw and return transaction`() {
      val wallet = mockk<Wallet>()
      val transaction = mockk<Transaction>()

      every { WalletFactory.requireById(walletId) } returns wallet
      every { wallet.withdraw(withdrawal) } returns transaction

      val result = suite.withdraw(walletId, withdrawal)

      assertEquals(transaction, result)
      verify { wallet.withdraw(withdrawal) }
    }

    @Test
    fun `should throw when wallet not found`() {
      every { WalletFactory.requireById(walletId) } throws WalletNotFoundException(walletId)

      assertThrows<WalletNotFoundException> {
        suite.withdraw(walletId, withdrawal)
      }
    }
  }
}
