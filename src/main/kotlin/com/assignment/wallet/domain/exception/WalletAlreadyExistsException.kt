package com.assignment.wallet.domain.exception

class WalletAlreadyExistsException(
  document: String
) : RuntimeException("Wallet already exists for document: $document")
