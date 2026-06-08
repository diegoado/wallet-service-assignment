package com.assignment.wallet.domain.exception

class SameWalletTransferException : RuntimeException("Cannot transfer to the same wallet")
