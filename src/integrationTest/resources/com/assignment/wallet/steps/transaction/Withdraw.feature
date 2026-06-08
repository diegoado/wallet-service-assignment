Feature: Withdraw Funds

  Scenario: Withdraw successfully
    Given A wallet exists with document "87791202026"
    And A deposit of "200.00" "BRL" exists with idempotency key "setup-wd-01"
    When I withdraw "50.00" "BRL" with idempotency key "wd-001"
    Then The response status should be 200
    And The transaction response should have type "WITHDRAW" and amount "-50.00"
    And A transaction with idempotency key "wd-001" should exist in the db with type "WITHDRAW" and amount "-50.00"

  Scenario: Withdraw insufficient funds
    Given A wallet exists with document "64030046037"
    When I withdraw "100.00" "BRL" with idempotency key "wd-no-funds"
    Then The response status should be 400
    And The error response message should contain "Insufficient funds"

  Scenario: Withdraw idempotent rejection
    Given A wallet exists with document "53864804051"
    And A deposit of "500.00" "BRL" exists with idempotency key "setup-wd-dup"
    When I withdraw "50.00" "BRL" with idempotency key "wd-dup"
    And I withdraw "50.00" "BRL" with idempotency key "wd-dup"
    Then The response status should be 400
    And The error response message should contain "Idempotent request already processed"

  Scenario: Withdraw from non-existent wallet
    When I withdraw "50.00" "BRL" from wallet "00000000-0000-0000-0000-000000000000" with idempotency key "wd-404"
    Then The response status should be 404
    And The error response message should contain "Wallet not found"

  Scenario: Withdraw without idempotency key
    Given A wallet exists with document "15893224093"
    When I withdraw "50.00" "BRL" without idempotency key
    Then The response status should be 400

  Scenario: Withdraw with invalid currency
    Given A wallet exists with document "74185107008"
    And A deposit of "100.00" "BRL" exists with idempotency key "setup-wd-curr"
    When I withdraw "50.00" "brl" with idempotency key "wd-currency"
    Then The response status should be 400
    And The error response should have field error on "currency" with code "REGEX_PATTERN_VALIDATION_FAILED" and message "must be a valid ISO 4217 currency code"
