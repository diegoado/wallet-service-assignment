package com.assignment.wallet.infrastructure.repository.impl

import com.assignment.wallet.domain.model.wallet.Wallet
import com.assignment.wallet.domain.model.wallet.WalletProviderFacade
import com.assignment.wallet.domain.model.wallet.WalletRepository
import com.assignment.wallet.domain.model.wallet.balance.BalanceProvider
import com.assignment.wallet.domain.model.wallet.lock.LockProvider
import com.assignment.wallet.domain.model.wallet.transaction.Transaction
import com.assignment.wallet.domain.model.wallet.transaction.TransactionType
import com.assignment.wallet.infrastructure.persistence.entity.TransactionLedgerDbEntity
import com.assignment.wallet.infrastructure.persistence.entity.WalletDbEntity
import com.assignment.wallet.infrastructure.persistence.repository.TransactionLedgerDbRepository
import com.assignment.wallet.infrastructure.persistence.repository.WalletDbRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

class WalletRepositoryImplTest {
  private val walletDbRepository = mockk<WalletDbRepository>()

  private val transactionLedgerDbRepository = mockk<TransactionLedgerDbRepository>()

  private val walletProvider =
    WalletProviderFacade(
      balance = mockk<BalanceProvider>(relaxed = true),
      lock = mockk<LockProvider>()
    )

  private val suite = WalletRepositoryImpl(walletDbRepository, transactionLedgerDbRepository, walletProvider)

  @Nested
  inner class GetById {
    private val walletId = UUID.randomUUID()

    @Test
    fun `should return wallet when found by id`() {
      val entity = WalletDbEntity(id = walletId, document = "12345678901", createdAt = ZonedDateTime.now())

      every { walletDbRepository.findById(walletId) } returns Optional.of(entity)

      val result = suite.getById(walletId)

      assertNotNull(result)
      assertEquals(walletId, result!!.id)
      assertEquals("12345678901", result.document)
    }

    @Test
    fun `should return null when not found by id`() {
      every { walletDbRepository.findById(walletId) } returns Optional.empty()

      val result = suite.getById(walletId)

      assertNull(result)
    }
  }

  @Nested
  inner class GetByDocumentOrNull {
    @Test
    fun `should return wallet when found by document`() {
      val entity = WalletDbEntity(id = UUID.randomUUID(), document = "12345678901", createdAt = ZonedDateTime.now())

      every { walletDbRepository.findByDocument("12345678901") } returns entity

      val result = suite.getByDocumentOrNull("12345678901")

      assertNotNull(result)
      assertEquals(entity.id, result!!.id)
      assertEquals("12345678901", result.document)
    }

    @Test
    fun `should return null when not found by document`() {
      every { walletDbRepository.findByDocument("99999999999") } returns null

      val result = suite.getByDocumentOrNull("99999999999")

      assertNull(result)
    }
  }

  @Nested
  inner class Save {
    private val walletId = UUID.randomUUID()

    @Test
    fun `should persist wallet entity and return domain wallet`() {
      val wallet =
        Wallet
          .Builder()
          .withId(walletId)
          .withDocument("12345678901")
          .withCreatedAt(ZonedDateTime.now())
          .withRepository(mockk<WalletRepository>())
          .withProvider(walletProvider)
          .build()

      val entitySlot = slot<WalletDbEntity>()

      every { walletDbRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

      val result = suite.save(wallet)

      assertEquals(walletId, result.id)
      assertEquals("12345678901", result.document)

      verify { walletDbRepository.save(any()) }

      val saved = entitySlot.captured

      assertEquals(walletId, saved.id)
      assertEquals("12345678901", saved.document)
    }
  }

  @Nested
  inner class ComputeBalance {
    @Test
    fun `should delegate to transaction ledger repository`() {
      val walletId = UUID.randomUUID()

      every { transactionLedgerDbRepository.computeBalance(walletId, "BRL") } returns BigDecimal("350.50")

      val result = suite.computeBalance(walletId, "BRL")

      assertEquals(BigDecimal("350.50"), result)

      verify { transactionLedgerDbRepository.computeBalance(walletId, "BRL") }
    }
  }

  @Nested
  inner class ComputeBalanceAt {
    private val at = ZonedDateTime.now().minusDays(5)

    @Test
    fun `should delegate to transaction ledger repository with timestamp`() {
      val walletId = UUID.randomUUID()

      every { transactionLedgerDbRepository.computeBalanceAt(walletId, "BRL", at) } returns BigDecimal("200.00")

      val result = suite.computeBalanceAt(walletId, "BRL", at)

      assertEquals(BigDecimal("200.00"), result)

      verify { transactionLedgerDbRepository.computeBalanceAt(walletId, "BRL", at) }
    }
  }

  @Nested
  inner class GetTransactionOrNull {
    private val walletId = UUID.randomUUID()

    private val today = LocalDate.now()

    @Test
    fun `should return transaction when found`() {
      val entity =
        TransactionLedgerDbEntity(
          walletId = walletId,
          currency = "BRL",
          amount = BigDecimal("100.00"),
          type = TransactionType.DEPOSIT,
          idempotencyKey = "key-1",
          idempotencyDate = today
        )

      every {
        transactionLedgerDbRepository.findByWalletIdAndIdempotencyKeyAndIdempotencyDate(walletId, "key-1", today)
      } returns entity

      val result = suite.getTransactionOrNull(walletId, "key-1", today)

      assertNotNull(result)
      assertEquals("BRL", result!!.currency)
      assertEquals(BigDecimal("100.00"), result.amount)
      assertEquals(TransactionType.DEPOSIT, result.type)
    }

    @Test
    fun `should return null when not found`() {
      every {
        transactionLedgerDbRepository.findByWalletIdAndIdempotencyKeyAndIdempotencyDate(walletId, "key-x", today)
      } returns null

      val result = suite.getTransactionOrNull(walletId, "key-x", today)

      assertNull(result)
    }
  }

  @Nested
  inner class NewTransaction {
    private val walletId = UUID.randomUUID()

    @Test
    fun `should persist transaction and return it`() {
      val transaction =
        Transaction(
          walletId = walletId,
          currency = "BRL",
          amount = BigDecimal("75.00"),
          type = TransactionType.DEPOSIT,
          idempotencyKey = "dep-key"
        )

      val entitySlot = slot<TransactionLedgerDbEntity>()
      every { transactionLedgerDbRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

      val result = suite.newTransaction(walletId, transaction)

      assertEquals(transaction, result)
      verify { transactionLedgerDbRepository.save(any()) }

      val saved = entitySlot.captured

      assertEquals(walletId, saved.walletId)
      assertEquals("BRL", saved.currency)
      assertEquals(BigDecimal("75.00"), saved.amount)
      assertEquals(TransactionType.DEPOSIT, saved.type)
      assertEquals("dep-key", saved.idempotencyKey)
    }
  }
}
