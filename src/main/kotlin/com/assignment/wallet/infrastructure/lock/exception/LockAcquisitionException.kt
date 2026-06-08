package com.assignment.wallet.infrastructure.lock.exception

class LockAcquisitionException(
  key: String
) : RuntimeException("Failed to acquire lock for key=$key")
