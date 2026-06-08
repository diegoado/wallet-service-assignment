package com.assignment.wallet.infrastructure.lock

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class DistributedLockProviderTest {
  private val redisLockService = mockk<RedisLockService>()

  private val suite = DistributedLockProvider(redisLockService)

  @Nested
  inner class WithLock {
    @Test
    fun `should delegate with formatted key`() {
      val walletId = UUID.randomUUID()

      every { redisLockService.executeWithLock("lock:wallet:$walletId:BRL", any<() -> String>()) } returns "result"

      val result = suite.withLock(walletId, "BRL") { "result" }

      assertEquals("result", result)
    }
  }

  @Nested
  inner class WithLocks {
    private val idA = UUID.fromString("00000000-0000-0000-0000-000000000002")

    private val idB = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @Test
    fun `should sort wallet ids and build keys before delegating`() {
      val keysSlot = slot<List<String>>()

      every { redisLockService.executeWithLocks(capture(keysSlot), any<() -> String>()) } returns "done"

      val result = suite.withLocks(listOf(idA, idB), "BRL") { "done" }

      assertEquals("done", result)

      val capturedKeys = keysSlot.captured

      assertEquals("lock:wallet:$idB:BRL", capturedKeys[0])
      assertEquals("lock:wallet:$idA:BRL", capturedKeys[1])
    }
  }
}
