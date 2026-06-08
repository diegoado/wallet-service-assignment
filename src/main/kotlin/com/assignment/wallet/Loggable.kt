package com.assignment.wallet

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import kotlin.reflect.full.companionObject

interface Loggable {
  private companion object {
    val factory: LoggableFactory = LoggableFactory()
  }

  val logger: Logger
    get() = factory { logger() }
}

class LoggableFactory : (Supplier<Logger>) -> Logger {
  private lateinit var logger: Logger

  override fun invoke(supplier: Supplier<Logger>): Logger {
    if (!::logger.isInitialized) {
      logger = supplier.get()
    }

    return logger
  }
}

inline fun <reified T : Loggable> T.logger() = LoggerFactory.getLogger(getClassForLogging(T::class.java))

fun <T> getClassForLogging(clazz: Class<T>): Class<*> =
  clazz.enclosingClass?.takeIf {
    it.kotlin.companionObject?.java == clazz
  } ?: clazz

fun asKeyValue(
  key: String,
  value: Any?
): StructuredArgument = StructuredArguments.kv(key, value)
