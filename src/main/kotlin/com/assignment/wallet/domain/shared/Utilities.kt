package com.assignment.wallet.domain.shared

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT)

fun String.asZonedDateTime(): ZonedDateTime = ZonedDateTime.parse(this, formatter)
