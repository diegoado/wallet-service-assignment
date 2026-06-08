package com.assignment.wallet.domain.exception

class IdempotentRequestException(
  idempotencyKey: String
) : RuntimeException("Idempotent request already processed: $idempotencyKey")
