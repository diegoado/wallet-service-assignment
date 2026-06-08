Feature: Create Wallet

  Scenario: Create successfully
    When I create a wallet with document "36368251000"
    Then The response status should be 201
    And The response should contain wallet id and document "36368251000"
    And The wallet should exist in the database with document "36368251000"

  Scenario: Create with same document
    Given A wallet exists with document "77143932035"
    When I create a wallet with document "77143932035"
    Then The response status should be 201
    And The response should contain wallet id and document "77143932035"

  Scenario: Create with invalid document
    When I create a wallet with document "123"
    Then The response status should be 400
    And The error response should have field error on "document" with code "REGEX_PATTERN_VALIDATION_FAILED" and message "must be a valid CPF (11 digits) or CNPJ (14 digits)"
