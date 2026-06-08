package com.assignment.wallet.infrastructure.lock

import com.assignment.wallet.infrastructure.lock.exception.LockAcquisitionException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

class RedisLockServiceTest {
  private val redisson = mockk<RedissonClient>()

  private val suite = RedisLockService(redisson)

  private val key = "lock:wallet:abc:BRL"

  @Nested
  inner class ExecuteWithLock {
    @Test
    fun `should acquire lock and execute action`() {
      val lock = mockk<RLock>()

      every { redisson.getLock(key) } returns lock
      every { lock.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock.unlock() } returns Unit

      val result = suite.executeWithLock(key) { "done" }

      assertEquals("done", result)

      verify { lock.unlock() }
    }

    @Test
    fun `should throw when lock not acquired`() {
      val lock = mockk<RLock>()

      every { redisson.getLock(key) } returns lock
      every { lock.tryLock(5L, 10L, TimeUnit.SECONDS) } returns false

      assertThrows<LockAcquisitionException> {
        suite.executeWithLock(key) { "done" }
      }
    }

    @Test
    fun `should release lock even on exception`() {
      val lock = mockk<RLock>()

      every { redisson.getLock(key) } returns lock
      every { lock.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock.unlock() } returns Unit

      assertThrows<RuntimeException> {
        suite.executeWithLock(key) { throw RuntimeException("fail") }
      }

      verify { lock.unlock() }
    }
  }

  @Nested
  inner class ExecuteWithLocks {
    private val key1 = "lock:wallet:aaa:BRL"

    private val key2 = "lock:wallet:bbb:BRL"

    @Test
    fun `should acquire all locks and execute action`() {
      val lock1 = mockk<RLock>()
      val lock2 = mockk<RLock>()

      every { redisson.getLock(key1) } returns lock1
      every { redisson.getLock(key2) } returns lock2
      every { lock1.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock2.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock1.unlock() } returns Unit
      every { lock2.unlock() } returns Unit

      val result = suite.executeWithLocks(listOf(key1, key2)) { "transferred" }

      assertEquals("transferred", result)

      verify { lock1.unlock() }
      verify { lock2.unlock() }
    }

    @Test
    fun `should release acquired locks when subsequent lock fails`() {
      val lock1 = mockk<RLock>()
      val lock2 = mockk<RLock>()

      every { redisson.getLock(key1) } returns lock1
      every { redisson.getLock(key2) } returns lock2
      every { lock1.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock2.tryLock(5L, 10L, TimeUnit.SECONDS) } returns false
      every { lock1.unlock() } returns Unit

      assertThrows<LockAcquisitionException> {
        suite.executeWithLocks(listOf(key1, key2)) { "fail" }
      }

      verify { lock1.unlock() }
      verify(exactly = 0) { lock2.unlock() }
    }

    @Test
    fun `should release all locks even on action exception`() {
      val lock1 = mockk<RLock>()
      val lock2 = mockk<RLock>()

      every { redisson.getLock(key1) } returns lock1
      every { redisson.getLock(key2) } returns lock2
      every { lock1.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock2.tryLock(5L, 10L, TimeUnit.SECONDS) } returns true
      every { lock1.unlock() } returns Unit
      every { lock2.unlock() } returns Unit

      assertThrows<RuntimeException> {
        suite.executeWithLocks(listOf(key1, key2)) { throw RuntimeException("fail") }
      }

      verify { lock1.unlock() }
      verify { lock2.unlock() }
    }
  }
}
