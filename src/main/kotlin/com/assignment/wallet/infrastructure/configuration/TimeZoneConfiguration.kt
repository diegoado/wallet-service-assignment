package com.assignment.wallet.infrastructure.configuration

import com.assignment.wallet.domain.shared.Constants
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
class TimeZoneConfiguration : InitializingBean {
  override fun afterPropertiesSet() {
    TimeZone.setDefault(TimeZone.getTimeZone(Constants.TIME_ZONE))
  }
}
