package com.assignment.wallet.domain.exception

import java.util.UUID

class InsufficientFundsException(
  walletId: UUID,
  currency: String
) : RuntimeException("Insufficient funds in wallet=$walletId currency=$currency")
