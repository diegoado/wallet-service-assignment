package com.assignment.wallet.infrastructure.configuration

import com.assignment.wallet.domain.shared.Constants
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.MapperFeature
import java.util.Locale
import java.util.TimeZone

@Configuration
class JacksonConfiguration {
  @Bean
  fun jsonMapperBuilderCustomizer(): JsonMapperBuilderCustomizer =
    JsonMapperBuilderCustomizer { builder ->
      builder
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .defaultLocale(Locale.ENGLISH)
        .defaultTimeZone(TimeZone.getTimeZone(Constants.TIME_ZONE))
    }
}
