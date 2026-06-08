package com.assignment.wallet.domain.exception

import java.util.UUID

class WalletNotFoundException(
  walletId: UUID
) : RuntimeException("Wallet not found: $walletId")
