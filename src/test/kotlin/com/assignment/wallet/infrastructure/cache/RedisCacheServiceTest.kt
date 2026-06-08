package com.assignment.wallet.infrastructure.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient

class RedisCacheServiceTest {
  private val redisson = mockk<RedissonClient>()

  private val suite = RedisCacheService(redisson)

  private val key = "test:key"

  @Nested
  inner class Get {
    @Test
    fun `should return value when present`() {
      val bucket = mockk<RBucket<String>>()

      every { redisson.getBucket<String>(key) } returns bucket
      every { bucket.get() } returns "value"

      val result = suite.get<String>(key)

      assertEquals("value", result)
    }

    @Test
    fun `should return null on cache miss`() {
      val bucket = mockk<RBucket<String>>()

      every { redisson.getBucket<String>(key) } returns bucket
      every { bucket.get() } returns null

      val result = suite.get<String>(key)

      assertNull(result)
    }

    @Test
    fun `should return null on redis failure`() {
      every { redisson.getBucket<String>(key) } throws RuntimeException("connection refused")

      val result = suite.get<String>(key)

      assertNull(result)
    }
  }

  @Nested
  inner class Set {
    @Test
    fun `should store value in redis`() {
      val bucket = mockk<RBucket<String>>(relaxed = true)

      every { redisson.getBucket<String>(key) } returns bucket

      suite.set(key, "value")

      verify { bucket.set("value") }
    }

    @Test
    fun `should not throw on redis failure`() {
      val bucket = mockk<RBucket<String>>()

      every { redisson.getBucket<String>(key) } returns bucket
      every { bucket.set(any()) } throws RuntimeException("connection refused")

      assertDoesNotThrow { suite.set(key, "value") }
    }
  }

  @Nested
  inner class Evict {
    @Test
    fun `should delete key from redis`() {
      val bucket = mockk<RBucket<Any>>()

      every { redisson.getBucket<Any>(key) } returns bucket
      every { bucket.delete() } returns true

      suite.evict(key)

      verify { bucket.delete() }
    }

    @Test
    fun `should not throw on redis failure`() {
      val bucket = mockk<RBucket<Any>>()

      every { redisson.getBucket<Any>(key) } returns bucket
      every { bucket.delete() } throws RuntimeException("connection refused")

      assertDoesNotThrow { suite.evict(key) }
    }
  }
}
