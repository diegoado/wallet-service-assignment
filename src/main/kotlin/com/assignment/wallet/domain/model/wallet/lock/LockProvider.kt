package com.assignment.wallet.domain.model.wallet.lock

import java.util.UUID

interface LockProvider {
  fun <T> withLock(
    walletId: UUID,
    currency: String,
    action: () -> T
  ): T

  fun <T> withLocks(
    walletIds: List<UUID>,
    currency: String,
    action: () -> T
  ): T
}
