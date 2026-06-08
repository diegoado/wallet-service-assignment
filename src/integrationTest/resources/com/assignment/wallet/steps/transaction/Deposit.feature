Feature: Deposit Funds

  Scenario: Deposit successfully
    Given A wallet exists with document "09189399099"
    When I deposit "100.50" "BRL" with idempotency key "dep-001"
    Then The response status should be 200
    And The transaction response should have type "DEPOSIT" and amount "100.50"
    And A transaction with idempotency key "dep-001" should exist in the db with type "DEPOSIT" and amount "100.50"

  Scenario: Deposit idempotent rejection
    Given A wallet exists with document "38058130079"
    When I deposit "50.00" "BRL" with idempotency key "dep-dup"
    And I deposit "50.00" "BRL" with idempotency key "dep-dup"
    Then The response status should be 400
    And The error response message should contain "Idempotent request already processed"

  Scenario: Deposit to non-existent wallet
    When I deposit "100.00" "BRL" to wallet "00000000-0000-0000-0000-000000000000" with idempotency key "dep-404"
    Then The response status should be 404
    And The error response message should contain "Wallet not found"

  Scenario: Deposit with invalid amount
    Given A wallet exists with document "61953710093"
    When I deposit "0.00" "BRL" with idempotency key "dep-invalid"
    Then The response status should be 400
    And The error response should have field error on "amount" with code "DECIMAL_VALUE_LESS_THAN_MIN" and message "must be greater than or equal to 0.01"

  Scenario: Deposit without idempotency key
    Given A wallet exists with document "56250226079"
    When I deposit "50.00" "BRL" without idempotency key
    Then The response status should be 400

  Scenario: Deposit with invalid currency
    Given A wallet exists with document "93639577051"
    When I deposit "50.00" "brl" with idempotency key "dep-currency"
    Then The response status should be 400
    And The error response should have field error on "currency" with code "REGEX_PATTERN_VALIDATION_FAILED" and message "must be a valid ISO 4217 currency code"
